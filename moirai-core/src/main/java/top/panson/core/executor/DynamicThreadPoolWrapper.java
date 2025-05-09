package top.panson.core.executor;

import top.panson.core.provider.CommonDynamicThreadPoolProviderFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.*;


/**
 * @方法描述：动态线程池包装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicThreadPoolWrapper implements DisposableBean {


    private String tenantId, itemId, threadPoolId;


    private boolean subscribeFlag, initFlag;


    private ThreadPoolExecutor executor;


    public DynamicThreadPoolWrapper(String threadPoolId) {
        this(threadPoolId, CommonDynamicThreadPoolProviderFactory.getInstance(threadPoolId));
    }


    public DynamicThreadPoolWrapper(String threadPoolId, ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolId = threadPoolId;
        this.executor = threadPoolExecutor;
        this.subscribeFlag = true;
    }


    public void execute(Runnable command) {
        executor.execute(command);
    }


    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }


    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }



    @Override
    public void destroy() throws Exception {
        if (executor instanceof DynamicThreadPoolExecutor) {
            ((DynamicThreadPoolExecutor) executor).destroy();
        }
    }
}
