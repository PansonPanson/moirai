package top.panson.config.monitor;

import top.panson.common.monitor.Message;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：抽象的线程池信息解析策略器，专门用来解析线程池信息的
 */
public abstract class AbstractMonitorDataExecuteStrategy<T extends Message> {

    /**
     * Mark.
     *
     * @return mark
     */
    public abstract String mark();

    /**
     * Execute.
     *
     * @param message message
     */
    public abstract void execute(T message);
}