package top.panson.config.monitor;

import top.panson.common.monitor.Message;


/**
 *
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