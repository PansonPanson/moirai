package top.panson.springboot.start.core;

import top.panson.common.design.builder.ThreadFactoryBuilder;
import top.panson.common.model.ThreadPoolParameterInfo;
import top.panson.common.toolkit.ContentUtil;
import top.panson.common.toolkit.GroupKey;
import top.panson.common.toolkit.IdUtil;
import top.panson.common.toolkit.JSONUtil;
import top.panson.common.web.base.Result;
import top.panson.springboot.start.remote.HttpAgent;
import top.panson.springboot.start.remote.ServerHealthCheck;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.*;

import static top.panson.common.constant.Constants.*;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/7
 * @方法描述：客户端长轮询对象，在这个对象中，开启了定时任务，定时任务会定期向服务端发送长轮询请求，监听服务端的动态线程池配置信息是否有更新
 * 如果更新了客户端就会及时感知到，然后使用服务端的配置信息更新本地线程池。这里我要多说一句，这个框架的长轮询功能其实就是复制了nacos旧版长轮询的代码
 * 代码几乎一模一样，当然，hippo4j的长轮询代码量要少一点。这一点在客户端和服务端的长轮询组件中都很明显。
 */
@Slf4j
public class ClientWorker {
    //长轮询请求超时时间
    private long timeout;
    private final HttpAgent agent;
    //客户端唯一标识
    private final String identify;
    //健康检查器
    private final ServerHealthCheck serverHealthCheck;
    //启动长轮询定时任务的执行器
    private final ScheduledExecutorService executor;
    //这个定时任务执行器就是用来定期执行长轮询任务的
    //这里大家可能会感到疑惑，为什么要定义两个定时任务执行器，这是因为长轮询定时任务的启动要等待
    //springboot上下文刷新完成，也就是容器初始化完毕之后才可以执行，并且启动长轮询任务的操作在ClientWorker构造方法中
    //如果是这样，直接在构造方法中等待springboot上下文刷新，那线程就会在ClientWorker方法中阻塞住，这样一来springboot上下文就永远不会刷新完毕了
    //所以，这里的操作就是在ClientWorker的构造方法中异步等待容器刷新完毕，上面这个executor执行器就是异步操作的发起者
    private final ScheduledExecutorService executorService;
    //等待springboot上下文刷新完毕后，这个awaitApplicationComplete就会调用countDown方法，让程序继续执行下去
    private final CountDownLatch awaitApplicationComplete = new CountDownLatch(1);
    //当cacheMap中存在CacheData后，cacheCondition就会调用它的countDown方法，让程序继续执行下去
    //这个时候就意味着在客户端订阅了某些线程池的动态信息，客户端终于可以发送长轮询请求给服务端监听线程池信息是否发生变更了
    private final CountDownLatch cacheCondition = new CountDownLatch(1);
    //这个map就是用来缓存所有CacheData对象的，每创建一个CacheData对象，就意味着客户端向服务端订阅了一个现成的配置信息
    //CacheData对象中封装了对应的监听器
    private final ConcurrentHashMap<String, CacheData> cacheMap = new ConcurrentHashMap<>(16);

    //构造方法
    @SuppressWarnings("all")
    public ClientWorker(HttpAgent httpAgent, String identify, ServerHealthCheck serverHealthCheck) {
        this.agent = httpAgent;
        this.identify = identify;
        this.timeout = CONFIG_LONG_POLL_TIMEOUT;
        this.serverHealthCheck = serverHealthCheck;
        //在这里创建了executor执行器
        this.executor = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("client.worker.executor");
            thread.setDaemon(true);
            return thread;
        });
        //创建executorService执行器
        this.executorService = Executors.newSingleThreadScheduledExecutor(
                ThreadFactoryBuilder.builder().prefix("client.long.polling.executor").daemon(true).build());
        log.info("Client identify: {}", identify);
        //提交了一个异步任务，这个异步任务会等待springboot上下文刷新完毕，然后执行提交一个定时任务给executorService执行器
        //让执行器开始执行长轮询任务
        this.executor.schedule(() -> {
            try {
                //这里就是异步等待springboot上下文刷新完毕的操作
                awaitApplicationComplete.await();
                //在这里创建了LongPollingRunnable任务对象，提交给定时器了
                executorService.execute(new LongPollingRunnable(cacheMap.isEmpty(), cacheCondition));
            } catch (Throwable ex) {
                log.error("Sub check rotate check error.", ex);
            }
        }, 1L, TimeUnit.MILLISECONDS);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：这个内部类的对象就是长轮询任务对象
     */
    class LongPollingRunnable implements Runnable {
        //cacheMap中是否为空
        private boolean cacheMapInitEmptyFlag;
        private final CountDownLatch cacheCondition;

        //构造方法
        public LongPollingRunnable(boolean cacheMapInitEmptyFlag, CountDownLatch cacheCondition) {
            this.cacheMapInitEmptyFlag = cacheMapInitEmptyFlag;
            this.cacheCondition = cacheCondition;
        }

        @Override
        @SneakyThrows
        public void run() {
            //这里判断cacheMap中是否为空，如果为空就意味着当前客户端还没有向服务端订阅任何一个线程池的信息
            if (cacheMapInitEmptyFlag) {
                //这就意味着没必要执行长轮询任务，因为就算你请求发过去了，也不知道要监听哪个线程池信息的变化
                //在addTenantListeners方法中，会执行cacheCondition的countDown方法
                //这时候就意味着客户端监听了某些动态线程池的信息了
                cacheCondition.await();
                //更新标志为false，程序继续向下执行
                cacheMapInitEmptyFlag = false;
            }
            //首先检查服务器的健康状态
            serverHealthCheck.isHealthStatus();
            //定义一个集合，这个集合会存放cacheMap中的所有value，也就是所有CacheData对象
            List<CacheData> cacheDataList = new ArrayList();
            //这个是用来判断在所有的CacheData对象中，有没有处于初始化状态的对象，当客户端监听了某个线程池的信息，并且是第一次向服务端发送这个线程池的长轮询请求时
            //这个时候线程池对应的CacheData对象就处于初始化状态
            List<String> inInitializingCacheList = new ArrayList();
            //把cacheMap中的所有value都存放到cacheDataList中
            cacheMap.forEach((key, val) -> cacheDataList.add(val));
            //这里就是检查服务端对应的线程池的信息是否更新了的操作，如果有线程池在服务端更新了，这里就会把线程池的相关信息返回给客户端
            //其实就是每一个更新了的线程池的线程池Id、项目Id、租户信息
            List<String> changedTpIds = checkUpdateDataIds(cacheDataList, inInitializingCacheList);
            //遍历所有更新的线程池信息
            for (String groupKey : changedTpIds) {
                //得到线程池具体的信息
                String[] keys = groupKey.split(GROUP_KEY_DELIMITER_TRANSLATION);
                //线程池Id
                String tpId = keys[0];
                //项目Id
                String itemId = keys[1];
                //命名空间，其实就是租户信息
                String namespace = keys[2];
                try {
                    //根据刚才得到的线程池的相关信息，访问服务端，从服务端获取线程池最新的配置信息
                    String content = getServerConfig(namespace, itemId, tpId, 3000L);
                    //从客户端得到缓存了线程池信息的CacheData对象
                    CacheData cacheData = cacheMap.get(tpId);
                    //然后把从服务端得到的最新信息更新到对应的本地CacheData对象中即可
                    String poolContent = ContentUtil.getPoolContent(JSONUtil.parseObject(content, ThreadPoolParameterInfo.class));
                    //这个操作同时也会更新CacheData对象的md5成员变量
                    cacheData.setContent(poolContent);
                } catch (Exception ignored) {
                    log.error("Failed to get the latest thread pool configuration.", ignored);
                }
            }
            //这里就是把还是初始化状态的CacheData的isInitializing更新为false
            for (CacheData cacheData : cacheDataList) {
                if (!cacheData.isInitializing() || inInitializingCacheList.contains(GroupKey.getKeyTenant(cacheData.threadPoolId, cacheData.itemId, cacheData.tenantId))) {
                    //在这里刷新客户端本地的线程池信息
                    cacheData.checkListenerMd5();
                    cacheData.setInitializing(false);
                }
            }
            //清空集合
            inInitializingCacheList.clear();
            //提交下一个长轮询任务给定时任务执行器
            executorService.execute(this);
        }
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：检查线程池信息是否在服务端更新了的入口方法
     */
    private List<String> checkUpdateDataIds(List<CacheData> cacheDataList, List<String> inInitializingCacheList) {
        StringBuilder sb = new StringBuilder();
        //拼接所有线程池的相关信息，注意，这里是把本地所有监听了服务端配置信息的线程池的相关信息都拼接到一起了
        for (CacheData cacheData : cacheDataList) {
            //拼接线程池ID
            sb.append(cacheData.threadPoolId).append(WORD_SEPARATOR);
            //拼接项目Id
            sb.append(cacheData.itemId).append(WORD_SEPARATOR);
            //拼接租户Id
            sb.append(cacheData.tenantId).append(WORD_SEPARATOR);
            //拼接客户端唯一标识
            sb.append(identify).append(WORD_SEPARATOR);
            //拼接当前线程对应的md5
            sb.append(cacheData.getMd5()).append(LINE_SEPARATOR);
            //这里再判断一下当前正在遍历的cacheData对象是否为初始化状态
            if (cacheData.isInitializing()) {
                //如果是则添加到初始化集合中
                inInitializingCacheList.add(GroupKey.getKeyTenant(cacheData.threadPoolId, cacheData.itemId, cacheData.tenantId));
            }
        }
        //如果初始化集合中有数据，说明有cacheData处于初始化状态，也就意味着有些长轮询请求时第一次向服务端发送
        boolean isInitializingCacheList = !inInitializingCacheList.isEmpty();
        //在下面这个方法中就会发送长轮询请求给服务端
        return checkUpdateTpIds(sb.toString(), isInitializingCacheList);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：检查线程池信息是否在服务端更新了的方法，在这个方法中，客户端向服务端发送了长轮询请求
     */
    public List<String> checkUpdateTpIds(String probeUpdateString, boolean isInitializingCacheList) {
        if (StringUtils.isEmpty(probeUpdateString)) {
            return Collections.emptyList();
        }
        Map<String, String> params = new HashMap<>(2);
        //把客户端要监听的所有线程池信息拼接成的字符串放到map中，key其实就是Listening-Configs，意思为监听的配置
        params.put(PROBE_MODIFY_REQUEST, probeUpdateString);
        //这里设置了一个键值对，value是一个uuid，是为了在服务单去重的，如果发送过来的请求中的uuid一致，说明请求已经发送过了
        //重复发送的请求就可以丢弃
        params.put(WEIGHT_CONFIGS, IdUtil.simpleUUID());
        Map<String, String> headers = new HashMap<>(2);
        //把长轮询请求超时时间设置到请求头中
        headers.put(LONG_PULLING_TIMEOUT, "" + timeout);
        //在请求头中设置客户端唯一标识
        headers.put(LONG_PULLING_CLIENT_IDENTIFICATION, identify);
        //如果存在初始化状态的cacheData对象
        if (isInitializingCacheList) {
            //就在请求头中设置下面的键值对，key其实就是Long-Pulling-Timeout-No-Hangup，服务端在请求中解析出这个键值对之后
            //就会知道有些请求是第一次发送过来，这意味着有些线程池是客户端重启或者是第一次启动，也或者是线程池刚刚订阅了服务端的配置信息
            //总之，这种情况下服务端要尽快回复客户端，如果线程池配置有更新，那就返回更新的线程池Id信息，如果没有更新，也立刻回复客户端
            //不能把请求直接挂起，应该让客户端尽快知道线程池的最新信息，等后面再发送请求过来，就可以直接按照长轮询的操作把请求挂起了
            headers.put(LONG_PULLING_TIMEOUT_NO_HANGUP, "true");
        }
        try {
            //这里计算的是从服务端读取数据的超时时间
            long readTimeoutMs = timeout + (long) Math.round(timeout >> 1);
            //向服务端发送请求，这个请求会被ConfigController的listener方法接收并处理
            Result result = agent.httpPostByConfig(LISTENER_PATH, headers, params, readTimeoutMs);
            //判断请求是否成功，并且是否不为null
            if (result != null && result.isSuccess()) {
                //请求成功的话，就可以解析响应数据了
                return parseUpdateDataIdResponse(result.getData().toString());
            }
        } catch (Exception ex) {
            setHealthServer(false);
            log.error("Check update get changed dataId exception. error message: {}", ex.getMessage());
        }
        return Collections.emptyList();
    }


    //向服务器发送请求，获得线程池最新配置信息的方法，这个方法的操作在第一版本代码中已经见过了，所以就不添加注释了
    public String getServerConfig(String namespace, String itemId, String threadPoolId, long readTimeout) {
        Map<String, String> params = new HashMap<>(3);
        params.put("namespace", namespace);
        params.put("itemId", itemId);
        params.put("tpId", threadPoolId);
        params.put("instanceId", identify);
        Result result = agent.httpGetByConfig(CONFIG_CONTROLLER_PATH, null, params, readTimeout);
        if (result.isSuccess()) {
            return JSONUtil.toJSONString(result.getData());
        }
        log.error("Sub server namespace: {}, itemId: {}, threadPoolId: {}, result code: {}", namespace, itemId, threadPoolId, result.getCode());
        return NULL;
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：解析服务端响应的方法
     */
    public List<String> parseUpdateDataIdResponse(String response) {
        if (StringUtils.isEmpty(response)) {
            return Collections.emptyList();
        }
        try {//对响应解码
            response = URLDecoder.decode(response, "UTF-8");
        } catch (Exception e) {
            log.error("Polling resp decode modifiedDataIdsString error.", e);
        }
        List<String> updateList = new LinkedList<>();
        //下面就是解析响应的方法
        for (String dataIdAndGroup : response.split(LINE_SEPARATOR)) {
            if (!StringUtils.isEmpty(dataIdAndGroup)) {
                //得到封装每一个线程池信息的字符串数组
                String[] keyArr = dataIdAndGroup.split(WORD_SEPARATOR);
                //得到线程池Id
                String dataId = keyArr[0];
                //得到项目Id
                String group = keyArr[1];
                //得到租户Id
                if (keyArr.length == 3) {
                    String tenant = keyArr[2];
                    //把线程池的信息存放到上面创建的集合中
                    updateList.add(GroupKey.getKeyTenant(dataId, group, tenant));
                    log.info("[{}] Refresh thread pool changed.", dataId);
                } else {
                    log.error("[{}] Polling resp invalid dataIdAndGroup error.", dataIdAndGroup);
                }
            }
        }
        return updateList;
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：为对应的线程池添加监听器的方法
     */
    public void addTenantListeners(String namespace, String itemId, String threadPoolId, List<? extends Listener> listeners) {
        //从cacheMap中得到线程池对应的CacheData对象，在addCacheDataIfAbsent方法中，如果CacheData不存在，就创建新的CacheData对象
        CacheData cacheData = addCacheDataIfAbsent(namespace, itemId, threadPoolId);
        //遍历用户要添加的监听器
        for (Listener listener : listeners) {
            //添加到CacheData对象的监听器集合中
            cacheData.addListener(listener);
        }
        //在这里执行了cacheCondition.countDown()方法，因为cacheMap中有数据了，程序就可以继续执行下去了
        if (awaitApplicationComplete.getCount() == 0L) {
            cacheCondition.countDown();
        }
    }



    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：为要监听的线程池创建对应的CacheData对象的方法
     */
    public CacheData addCacheDataIfAbsent(String namespace, String itemId, String threadPoolId) {
        CacheData cacheData = cacheMap.get(threadPoolId);
        if (cacheData != null) {
            return cacheData;
        }
        //如果CacheData为null，创建一个CacheData对象
        cacheData = new CacheData(namespace, itemId, threadPoolId);
        //把新的CacheData放到cacheMap中
        CacheData lastCacheData = cacheMap.putIfAbsent(threadPoolId, cacheData);
        //cacheMap.putIfAbsent如果返回null，说明以前map中没有对应的数据
        //也就意味着这个线程池还没被监听过
        if (lastCacheData == null) {
            String serverConfig;
            try {
                //这时候有一个操作，那就是直接访问服务端，从服务端获取一次最新的配置信息
                serverConfig = getServerConfig(namespace, itemId, threadPoolId, 3000L);
                //把从服务端返回的信息转换为ThreadPoolParameterInfo对象
                ThreadPoolParameterInfo poolInfo = JSONUtil.parseObject(serverConfig, ThreadPoolParameterInfo.class);
                //更新本地cacheData对象缓存的线程池配置信息
                cacheData.setContent(ContentUtil.getPoolContent(poolInfo));
            } catch (Exception ex) {
                log.error("Cache Data Error. Service Unavailable: {}", ex.getMessage());
            }
            lastCacheData = cacheData;
        }
        return lastCacheData;
    }

    private void setHealthServer(boolean isHealthServer) {
        this.serverHealthCheck.setHealthStatus(isHealthServer);
    }

    public void notifyApplicationComplete() {
        awaitApplicationComplete.countDown();
    }
}
