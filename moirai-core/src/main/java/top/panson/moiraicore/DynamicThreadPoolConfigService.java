package top.panson.moiraicore;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.panson.moiraicore.constant.Constants;
import top.panson.moiraicore.model.BootstrapProperties;
import top.panson.moiraicore.model.DynamicThreadPoolRegisterParameter;
import top.panson.moiraicore.model.Result;
import top.panson.moiraicore.util.net.HttpAgent;
import top.panson.moiraicore.exception.ServiceException;

import java.util.Optional;




@Slf4j
@RequiredArgsConstructor
public class DynamicThreadPoolConfigService extends AbstractDynamicThreadPoolService {

    //得到访问服务单的http代理
    private final HttpAgent httpAgent;

    //得到配置信息对象
    private final BootstrapProperties properties;

    //注册动态线程池信息到服务端的入口方法
    @Override
    public void registerDynamicThreadPool(DynamicThreadPoolRegisterWrapper registerWrapper) {
        registerExecutor(registerWrapper);
    }


    private void registerExecutor(DynamicThreadPoolRegisterWrapper registerWrapper) {
        //从动态线程池的包装注册器中得到要注册到服务端的动态线程池的参数信息对象
        DynamicThreadPoolRegisterParameter registerParameter = registerWrapper.getParameter();
        //检查线程池参数是否合法
        checkThreadPoolParameter(registerParameter);
        //得到动态线程池Id
        String threadPoolId = registerParameter.getThreadPoolId();
        try {//向要注册到服务端的线程池对象中设置租户信息和项目Id
            fillDynamicThreadPoolRegisterWrapper(registerWrapper);
            //在这里向服务端注册了动态线程池的信息，注意，这里是直接把registerWrapper这个DynamicThreadPoolRegisterWrapper类型的对象传输给服务端了
            //并且这里使用的是http通信方式，服务端接收这个请求的是ConfigController类的register方法，大家可以直接去服务端看看接收之后的注册逻辑
            Result registerResult = httpAgent.httpPost(Constants.REGISTER_DYNAMIC_THREAD_POOL_PATH, registerWrapper);
            //根据返回结果判断是否注册成功了
            if (registerResult == null || !registerResult.isSuccess()) {
                throw new ServiceException("Dynamic thread pool registration returns error."
                        + Optional.ofNullable(registerResult).map(Result::getMessage).orElse(""));
            }
        } catch (Throwable ex) {
            log.error("Dynamic thread pool registration execution error: {}", threadPoolId, ex);
            throw ex;
        }
    }


    //判断线程池参数是否合法的方法
    private void checkThreadPoolParameter(DynamicThreadPoolRegisterParameter registerParameter) {
        //判断线程池Id中是否不包含+号，如果包含就意味着有敏感字符
        Assert.isTrue(!registerParameter.getThreadPoolId().contains("+"), "The thread pool contains sensitive characters.");
    }

    //向要注册到服务端的线程池对象中设置租户信息和项目Id的方法
    private void fillDynamicThreadPoolRegisterWrapper(DynamicThreadPoolRegisterWrapper registerWrapper) {
        registerWrapper.setTenantId(properties.getNamespace());
        registerWrapper.setItemId(properties.getItemId());
    }
}
