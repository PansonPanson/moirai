package top.panson.springboot.start.core;

import top.panson.common.constant.Constants;
import top.panson.common.toolkit.ContentUtil;
import top.panson.common.toolkit.Md5Util;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.springboot.start.wrapper.ManagerListenerWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/7
 * @方法描述：动态线程池在客户端的缓存数据对象
 */
@Slf4j
public class CacheData {

    //动态线程池的md5
    @Getter
    public volatile String md5;

    //动态线程池的核心配置信息
    public volatile String content;

    //租户Id
    public final String tenantId;

    //项目Id
    public final String itemId;

    //线程池Id
    public final String threadPoolId;

    //是否正在初始化，一开始都默认为正在初始化状态，只有当客户端第一次发送长轮询请求给服务端时，服务端发现没有配置变更信息时并不会挂起请求，而是直接返回
    //当然，如果有信息变更也会直接同步返回最新配置信息给客户端，并不会将请求挂起，这么做主要是为了快速响应客户端，让客户端对应的动态线程池尽快结束初始化状态
    //让刚刚启动的客户端尽快得到最完整最新的配置信息
    @Setter
    private volatile boolean isInitializing = true;

    //当前动态线程池对应的监听器集合，用户定义的动态线程池监听器都会存放到这个集合中
    private final CopyOnWriteArrayList<ManagerListenerWrapper> listeners;

    //构造方法
    public CacheData(String tenantId, String itemId, String threadPoolId) {
        this.tenantId = tenantId;
        this.itemId = itemId;
        this.threadPoolId = threadPoolId;
        this.content = ContentUtil.getPoolContent(GlobalThreadPoolManage.getPoolParameter(threadPoolId));
        //在这里设置了md5的值
        this.md5 = getMd5String(content);
        this.listeners = new CopyOnWriteArrayList();
    }

    //添加监听器的方法
    public void addListener(Listener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("Listener is null.");
        }//包装一下监听器，就是把线程池的md5和监听器绑定到一起了
        ManagerListenerWrapper managerListenerWrap = new ManagerListenerWrapper(md5, listener);
        //把监听器放到集合中
        if (listeners.addIfAbsent(managerListenerWrap)) {
            log.info("Add listener status: ok, thread pool id: {}, listeners count: {}", threadPoolId, listeners.size());
        }
    }

    //检查服务端线程池的md5是否和客户端线程池的md5是否不同
    public void checkListenerMd5() {
        for (ManagerListenerWrapper managerListenerWrapper : listeners) {
            //如果不同就意味着在服务端动态线程池的配置信息发生了变化，所以这里就要动态刷新客户端本地线程池的信息
            if (!md5.equals(managerListenerWrapper.getLastCallMd5())) {
                //调用线程池对应的监听器，执行监听器中的方法
                safeNotifyListener(content, md5, managerListenerWrapper);
            }
        }
    }

    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/7
     * @方法描述：执行监听器的回调方法，动态刷新本地线程池信息
     */
    private void safeNotifyListener(String content, String md5, ManagerListenerWrapper managerListenerWrapper) {
        //得到监听器本身
        Listener listener = managerListenerWrapper.getListener();
        //封装一个新的任务，在任务执行监听器的回调方法，并且更新客户端缓存的线程池最新的md5的值
        Runnable runnable = () -> {
            managerListenerWrapper.setLastCallMd5(md5);
            listener.receiveConfigInfo(content);
        };
        try {//在这里得到了监听器持有的执行器，用这个执行器来执行监听器任务
            listener.getExecutor().execute(runnable);
        } catch (Exception ex) {
            log.error("Failed to execute listener. message: {}", ex.getMessage());
        }
    }

    //更新当前CacheData对象缓存的线程池的最新配置信息以及md5
    public void setContent(String content) {
        this.content = content;
        this.md5 = getMd5String(this.content);
    }

    //得到线程池md5的方法
    public static String getMd5String(String config) {
        return (null == config) ? Constants.NULL : Md5Util.md5Hex(config, Constants.ENCODE);
    }

    //判断是否正在初始化
    public boolean isInitializing() {
        return isInitializing;
    }
}
