/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.config.service;

import top.panson.common.toolkit.*;
import top.panson.common.web.base.Results;
import top.panson.config.event.AbstractEvent;
import top.panson.config.event.LocalDataChangeEvent;
import top.panson.config.notify.NotifyCenter;
import top.panson.config.notify.listener.AbstractSubscriber;
import top.panson.config.toolkit.ConfigExecutor;
import top.panson.config.toolkit.Md5ConfigUtil;
import top.panson.config.toolkit.RequestUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static top.panson.common.constant.Constants.GROUP_KEY_DELIMITER;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/7
 * @方法描述：提供处理长轮询请求的服务对象
 */
@Slf4j
@Service
public class LongPollingService {

    //用来计算固定轮询时间间隔的变量
    private static final int FIXED_POLLING_INTERVAL_MS = 10000;
    //这个变量在判断请求是否需要被挂起时会用到
    private static final String TRUE_STR = "true";
    //从请求中获得长轮询超时时间的key，所谓超时时间，其实就是请求被服务端挂起的时间，这个请求会被hold住
    public static final String LONG_POLLING_HEADER = "Long-Pulling-Timeout";
    //长轮询请求不会被挂起的key
    public static final String LONG_POLLING_NO_HANG_UP_HEADER = "Long-Pulling-Timeout-No-Hangup";
    //客户端应用名称，在这个框架中并没有向长轮询请求中设置这个值，所以服务端得到的是value
    public static final String CLIENT_APP_NAME_HEADER = "Client-AppName";
    //存放客户端活跃时间戳的map，key就是客户端的唯一标识符，value就是当前时间戳
    private Map<String, Long> retainIps = new ConcurrentHashMap();

    //构造方法
    public LongPollingService() {
        //长轮询队列，这个队列中存放的都是一个个ClientLongPolling长轮询任务，长轮询请求会被封装到长轮询任务中，然后存放到这个队列中
        allSubs = new ConcurrentLinkedQueue();
        //使用定时器执行定时任务，StatTask是一个定时任务，这个任务会定期记录allSubs队列中长轮询请求的数量到日志中
        //其实就是发送长轮询请求的客户端的数量
        ConfigExecutor.scheduleLongPolling(new StatTask(), 0L, 30L, TimeUnit.SECONDS);
        //向事件通知中心注册LocalDataChangeEvent事件发布器，这个LocalDataChangeEvent事件就代表配置变更事件
        //当用户直接在web界面修改了线程池的配置信息，在ConfigServiceImpl类的insertOrUpdate方法中修改了数据库的数据后，会直接发布一个LocalDataChangeEvent事件
        //注册到事件发布器中的订阅者就会执行它们各自的回调方法了
        NotifyCenter.registerToPublisher(LocalDataChangeEvent.class, NotifyCenter.ringBufferSize);
        //向事件中新注册订阅者，这里注册的订阅者实际上会注册到事件中心内部持有的事件发布器中
        //这里注册的订阅者只关注LocalDataChangeEvent事件，所以这个订阅者会被注册到发布LocalDataChangeEvent事件的事件发布器中
        NotifyCenter.registerSubscriber(new AbstractSubscriber() {

            @Override
            public void onEvent(AbstractEvent event) {
                //当LocalDataChangeEvent事件发布了，就执行DataChangeTask这个任务
                //DataChangeTask这个任务的逻辑就是检查allSubs中是否有对应的线程池信息
                //如果有则向客户端回复长轮询响应，告诉客户端有线程池的信息更新了
                if (!isFixedPolling() && event instanceof LocalDataChangeEvent) {
                    LocalDataChangeEvent evt = (LocalDataChangeEvent) event;
                    ConfigExecutor.executeLongPolling(new DataChangeTask(evt.identify, evt.groupKey));
                }
            }
            //这里得到的就是订阅者关注的事件类型
            @Override
            public Class<? extends AbstractEvent> subscribeType() {
                return LocalDataChangeEvent.class;
            }
        });
    }

    //记录allSubs队列中发送长轮询请求的客户端的数量到日志中的任务
    class StatTask implements Runnable {
        @Override
        public void run() {
            log.info("Dynamic Thread Pool Long pulling client count: {}", allSubs.size());
        }
    }


    //存放长轮询请求的队列
    final Queue<ClientLongPolling> allSubs;


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：服务端向客户端回复长轮询响应的任务，当服务端的事件通知中心发布了LocalDataChangeEvent事件，这个任务就会被订阅者调用
     */
    class DataChangeTask implements Runnable {
        //变更的线程池的实例Id
        final String identify;
        //这是要给组合键，是线程池Id+项目Id+租户Id拼接到一起组成的键
        final String groupKey;

        DataChangeTask(String identify, String groupKey) {
            this.identify = identify;
            this.groupKey = groupKey;
        }

        public void run() {
            try {//从队列中得到所有的长轮询请求，这里大家一定要理清楚，每一个长轮询请求就对应一个客户端
                //因为客户端向服务端发送长轮询请求的时候，会把客户端本地所有要监控的线程池信息拼接成一个字符串发送给服务器
                //所以一个客户端监听着多个线程池
                for (Iterator<ClientLongPolling> iter = allSubs.iterator(); iter.hasNext();) {
                    //获得客户端的长轮询对象
                    ClientLongPolling clientSub = iter.next();
                    //组合成一个标识符
                    String identity = groupKey + GROUP_KEY_DELIMITER + identify;
                    //这里集合中数据的数量为1
                    List<String> parseMapForFilter = CollectionUtil.newArrayList(identity);
                    if (StringUtil.isBlank(identify)) {
                        parseMapForFilter = MapUtil.parseMapForFilter(clientSub.clientMd5Map, groupKey);
                    }
                    //然后开始遍历当前遍历到的客户端中所有线程的信息，如果parseMapForFilter集合中线程的信息和遍历到的线程信息一致
                    //就把这个现成的组合key信息回复给客户端
                    parseMapForFilter.forEach(each -> {
                        if (clientSub.clientMd5Map.containsKey(each)) {
                            //更新客户端最新活跃时间
                            getRetainIps().put(clientSub.clientIdentify, System.currentTimeMillis());
                            //更新服务端缓存的线程池信息的md5
                            ConfigCacheService.updateMd5(each, clientSub.clientIdentify, ConfigCacheService.getContentMd5(each));
                            //将长轮询对象移出队列，下一次客户端发送长轮询请求过来时会重新向队列中添加长轮询对象
                            iter.remove();
                            //向客户端回复长轮询响应，就把更新的线程池的组合key回复给客户端即可，客户端会根据key主动向服务端查询最新的配置信息
                            clientSub.sendResponse(Arrays.asList(groupKey));
                        }
                    });
                }
            } catch (Exception ex) {
                log.error("Data change error: {}", ex.getMessage(), ex);
            }
        }
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：添加长轮询请求到allSubs队列中的方法
     */
    public void addLongPollingClient(HttpServletRequest req, HttpServletResponse rsp, Map<String, String> clientMd5Map,
                                     int probeRequestSize) {
        //得到长轮询超时时间，其实就是hold请求的事件
        String str = req.getHeader(LONG_POLLING_HEADER);
        //得到客户端应用名称，这里得到的就是null
        String appName = req.getHeader(CLIENT_APP_NAME_HEADER);
        //如果客户端设置了请求不挂起的key，这里就会得到对应的value，看看是true还是false
        //大家应该还记得，如果客户端的要监听的某个线程的CacheData对象是初始化状态，也就是第一次向服务端发送长轮询请求
        //客户端就会设置一个请求不挂起的标志，以便于让客户端快速得到配置信息
        String noHangUpFlag = req.getHeader(LongPollingService.LONG_POLLING_NO_HANG_UP_HEADER);
        //获取固定延时时间，固定延时时间是500ms
        int delayTime = SwitchService.getSwitchInteger(SwitchService.FIXED_DELAY_TIME, 500);
        //计算真正的hold请求的时间，这里会保证hold请求至少10秒
        //当然，大家也会在这里看到，得到的真正的超时时间实际上是用客户端传递过来的时间减去了刚才得到的固定延时时间
        //这就意味着假如客户端设置了30秒超时时间，服务端最多会hold请求29.5s就给客户端响应，这个简单的调整是把网络传输的时间预留出来了
        //以防网络波动，服务端没有及时接收到服务端的消息
        long timeout = Math.max(10000, Long.parseLong(str) - delayTime);
        //这里有一个判断，判断服务端处理长轮询请求是不是固定轮询模式
        //如果是固定轮询模式，那么就按照固定轮询的时间来hold住请求
        if (isFixedPolling()) {
            //这个时间和上面计算出来的timeout是不一样的，这个固定轮询模式在当前框架中默认不开启
            //实际上，这个模式也是把nacos的那一套机制搬运过来了，但是nacos的固定轮询模式给用户暴露了可以自己调控的开关
            //这个框架并没有给用户暴露开关，除非修改源码，否则服务端无法使用固定轮询模式处理请求，简而言之，这个框架在某些方面还是一个半成品
            //接下来，我还要给大家再解释一下，什么是固定轮询模式，所谓固定轮询模式，服务端每一次接收到客户端的长轮询请求
            //都会以固定的时间hold请求，如果这个固定轮询时间设定的是20s，那么服务端每次都会hold请求20秒，然后给客户端回复响应
            //客户端再发送下一个长轮询请求，这样一来，其实就变成了客户端每隔20秒向服务端发送一个长轮询请求，并且，最重要的一点是
            //在固定轮询模式下，就算客户端一开始就检测到服务端的配置信息发生变更了，也不会立刻得到服务端的响应，仍然会等待20s之后
            //才能接收到服务端回复的响应
            timeout = Math.max(10000, getFixedPollingInterval());
        } else {//程序执行到这里就意味着没有使用固定轮询模式处理请求
            //这时候不管客户端传递过来的长轮询请求的超时时间是多少，反正会直接判断一下客户端传递过来的所有线程池的md5
            //和服务端缓存的线程池的md5的信息是否相同，如果不相同说明服务端的线程池更新了，这时候就返回更新的线程池的信息
            //Md5ConfigUtil.compareMd5(req, clientMd5Map)这行代码的作用就是用来比较客户端传递过来的所有线程池的md5和服务端缓存的线程池的md5的信息是否存在差异
            List<String> changedGroups = Md5ConfigUtil.compareMd5(req, clientMd5Map);
            if (!changedGroups.isEmpty()) {
                //如果changedGroups不为空，就立刻回复客户端一个同步响应，告诉客户端有些线程池配置变更了
                generateResponse(rsp, changedGroups);
                return;
            } else if (noHangUpFlag != null && noHangUpFlag.equalsIgnoreCase(TRUE_STR)) {
                //走到这里意味着没有发生配置变更，并且客户端设置了请求不挂起的标志，那就可以也直接返回客户端一个空响应
                log.info("New initializing cacheData added in.");
                return;
            }
        }
        //程序执行到这里就意味着请求要被hold住了，先得到客户端的唯一标识符
        String clientIdentify = RequestUtil.getClientIdentify(req);
        //得到请求的异步上下文，因为现在要hold住请求了，肯定不能再同步等待了，否则其他请求就没办法处理了
        //所以使用异步，到最后回复响应的时候也是回复异步响应
        final AsyncContext asyncContext = req.startAsync();
        //设置异步上下文的超时时间
        asyncContext.setTimeout(0L);
        //在这里把请求封装到ClientLongPolling长轮询任务中，开始hold住请求，其实就是把请求放到了allSubs队列中
        ConfigExecutor.executeLongPolling(new ClientLongPolling(asyncContext, clientMd5Map, clientIdentify, probeRequestSize, timeout - delayTime, appName));
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：这个内部类对象就是一个长轮询任务
     */
    class ClientLongPolling implements Runnable {
        //异步上下文
        final AsyncContext asyncContext;
        //客户端要监听的所有线程池的md5信息
        final Map<String, String> clientMd5Map;
        //长轮询对象创建的时间
        final long createTime;
        //客户端唯一标识
        final String clientIdentify;
        //应用名称
        final String appName;
        //请求内容的字节大小，这个成员变量在框架中并没有用到，在nacos中这个变量也只是在记录日志的时候被用到
        final int probeRequestSize;
        //hold请求的时间
        final long timeoutTime;
        //异步任务返回的future
        Future<?> asyncTimeoutFuture;


        public ClientLongPolling(AsyncContext asyncContext, Map<String, String> clientMd5Map, String clientIdentify, int probeRequestSize, long timeout, String appName) {
            this.asyncContext = asyncContext;
            this.clientMd5Map = clientMd5Map;
            this.clientIdentify = clientIdentify;
            this.probeRequestSize = probeRequestSize;
            this.timeoutTime = timeout;
            this.appName = appName;
            this.createTime = System.currentTimeMillis();
        }


        //任务要执行的方法
        @Override
        public void run() {
            //先使用ConfigExecutor执行器提交了一个定时任务，这个定时任务会在timeoutTime之后执行，这样一来就相当于把请求hold住了timeoutTime时间
            asyncTimeoutFuture = ConfigExecutor.scheduleLongPolling(() -> {
                try {
                    //更新客户端的最后活跃时间，注意，当这个任务执行的时候，就意味着请求已经在服务端被hold住了timeoutTime时间
                    //并且在这期间服务端的事件通知中心也没有发布配置变更事件
                    getRetainIps().put(ClientLongPolling.this.clientIdentify, System.currentTimeMillis());
                    //因为服务端要回复客户端了，不用再hold这个长轮询请求，所以直接从队列中移除即可
                    allSubs.remove(ClientLongPolling.this);
                    //判断是否为固定轮询模式
                    if (isFixedPolling()) {
                        //这里就要再判断一下配置是否发生了变更，为什么这里要再判断一下呢？原因很简单，在固定轮询模式下
                        //从请求进来到请求hold时间结束，都没有主动检查过客户端和服务端线程池的md5是否不同，如果在hold请求期间没有发布配置变更事件
                        //那么固定轮询下的长轮询请求是无法感知服务端是否发生了配置变更了。如果在长轮询结束的时候恰好发生了配置变更，但这个时候长轮询任务已经从队列中移除了
                        //客户端也感知不到了。当不使用固定轮询模式的时候，每一次长轮询请求进来都会主对比一下客户端和服务端线程池的md5是否不同，如果不同则直接返回请求，不再hold请求
                        //但固定轮询模式需要hold住请求，所以就会在hold结束的时候主动对比一下，如果有配置变更，正好可以直接通知客户端
                        List<String> changedGroups = Md5ConfigUtil.compareMd5((HttpServletRequest) asyncContext.getRequest(), clientMd5Map);
                        //判断是否存在配置变更
                        if (!changedGroups.isEmpty()) {
                            //存在配置变更就回复异步响应
                            sendResponse(changedGroups);
                        } else {
                            //没有变更就回复空响应
                            sendResponse(null);
                        }
                    } else {
                        //走动这里就意味着不是固定轮询模式，也直接回复一个空响应即可
                        sendResponse(null);
                    }
                } catch (Exception ex) {
                    log.error("Long polling error: {}", ex.getMessage(), ex);
                }
            }, timeoutTime, TimeUnit.MILLISECONDS);
            //注意，上面是提交的异步任务，这里是ClientLongPolling任务自己的逻辑，那就是把任务本身提交给长轮询队列
            allSubs.add(this);
        }


        //回复客户端响应的方法
        private void sendResponse(List<String> changedGroups) {
            if (null != asyncTimeoutFuture) {
                asyncTimeoutFuture.cancel(false);
            }
            generateResponse(changedGroups);
        }


        //回复异步响应
        private void generateResponse(List<String> changedGroups) {
            HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
            if (null == changedGroups) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                asyncContext.complete();
                return;
            }
            try {
                String respStr = buildRespStr(changedGroups);
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
                response.setHeader("Cache-Control", "no-cache,no-store");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(respStr);
            } catch (Exception ex) {
                log.error("Response client failed to return data.", ex);
            } finally {
                asyncContext.complete();
            }
        }
    }

    public Map<String, Long> getRetainIps() {
        return retainIps;
    }


    //同步回复客户端响应
    private void generateResponse(HttpServletResponse response, List<String> changedGroups) {
        if (CollectionUtil.isNotEmpty(changedGroups)) {
            try {
                String respStr = buildRespStr(changedGroups);
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
                response.setHeader("Cache-Control", "no-cache,no-store");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(respStr);
            } catch (Exception ex) {
                log.error("Response client failed to return data.", ex);
            }
        }
    }


    //构建响应内容
    @SneakyThrows
    private String buildRespStr(List<String> changedGroups) {
        String changedGroupStr = Md5Util.compareMd5ResultString(changedGroups);
        String respStr = JSONUtil.toJSONString(Results.success(changedGroupStr));
        return respStr;
    }


    //是否支持长轮询
    public static boolean isSupportLongPolling(HttpServletRequest request) {
        return request.getHeader(LONG_POLLING_HEADER) != null;
    }



    //是否为固定轮询模式，默认为false
    private static boolean isFixedPolling() {
        return SwitchService.getSwitchBoolean(SwitchService.FIXED_POLLING, false);
    }


    //得到固定轮询时间
    private static int getFixedPollingInterval() {
        return SwitchService.getSwitchInteger(SwitchService.FIXED_POLLING_INTERVAL, FIXED_POLLING_INTERVAL_MS);
    }
}
