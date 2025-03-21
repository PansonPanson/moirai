package top.panson.springboot.start.support;

import top.panson.common.model.register.DynamicThreadPoolRegisterParameter;
import top.panson.common.model.register.DynamicThreadPoolRegisterWrapper;
import top.panson.common.toolkit.BooleanUtil;
import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.web.base.Result;
import top.panson.common.web.exception.ServiceException;
import top.panson.core.executor.support.service.AbstractDynamicThreadPoolService;
import top.panson.message.dto.NotifyConfigDTO;
import top.panson.message.service.GlobalNotifyAlarmManage;
import top.panson.message.service.Hippo4jBaseSendMessageService;
import top.panson.message.service.ThreadPoolNotifyAlarm;
import top.panson.springboot.start.config.BootstrapProperties;
import top.panson.springboot.start.notify.ServerNotifyConfigBuilder;
import top.panson.springboot.start.remote.HttpAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static top.panson.common.constant.Constants.REGISTER_DYNAMIC_THREAD_POOL_PATH;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/26
 * @方法描述：这个类的对象是客户端非常重要的一个组件，就是这个类的对象提供了把动态线程池信息注册到服务端的功能
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicThreadPoolConfigService extends AbstractDynamicThreadPoolService {

    //得到访问服务单的http代理
    private final HttpAgent httpAgent;

    //得到配置信息对象
    private final BootstrapProperties properties;

    //告警方式配置信息构建器
    private final ServerNotifyConfigBuilder notifyConfigBuilder;

    //告警信息发送器
    private final Hippo4jBaseSendMessageService hippo4jBaseSendMessageService;

    //注册动态线程池信息到服务端的入口方法
    @Override
    public void registerDynamicThreadPool(DynamicThreadPoolRegisterWrapper registerWrapper) {
        registerExecutor(registerWrapper);
    }

    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/4/26
     * @方法描述：在该方法中把动态线程池的信息注册到服务端，注意，只要执行了注册方法，就意味着服务端之前是没有动态线程池信息的，也就意味着客户端本地的动态线程池的配置信息就是最新的
     * 源码中这个方法中有一个严重的bug，会导致程序无法正常运行，我给修正了一下
     */
    private void registerExecutor(DynamicThreadPoolRegisterWrapper registerWrapper) {
        //从动态线程池的包装注册器中得到要注册到服务端的动态线程池的参数信息对象
        DynamicThreadPoolRegisterParameter registerParameter = registerWrapper.getParameter();
        //检查线程池参数是否合法
        checkThreadPoolParameter(registerParameter);
        //得到动态线程池Id
        String threadPoolId = registerParameter.getThreadPoolId();
        try {//向要注册到服务端的线程池对象中设置租户信息和项目Id
            failDynamicThreadPoolRegisterWrapper(registerWrapper);
            //在这里向服务端注册了动态线程池的信息，注意，这里是直接把registerWrapper这个DynamicThreadPoolRegisterWrapper类型的对象传输给服务端了
            //并且这里使用的是http通信方式，服务端接收这个请求的是ConfigController类的register方法，大家可以直接去服务端看看接收之后的注册逻辑
            Result registerResult = httpAgent.httpPost(REGISTER_DYNAMIC_THREAD_POOL_PATH, registerWrapper);
            //根据返回结果判断是否注册成功了
            if (registerResult == null || !registerResult.isSuccess()) {
                throw new ServiceException("Dynamic thread pool registration returns error."
                        + Optional.ofNullable(registerResult).map(Result::getMessage).orElse(""));
            }//在客户端缓存线程池的告警配置信息
            putNotifyAlarmConfig(registerWrapper);
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
    private void failDynamicThreadPoolRegisterWrapper(DynamicThreadPoolRegisterWrapper registerWrapper) {
        registerWrapper.setTenantId(properties.getNamespace());
        registerWrapper.setItemId(properties.getItemId());
    }


    private void putNotifyAlarmConfig(DynamicThreadPoolRegisterWrapper registerWrapper) {
        DynamicThreadPoolRegisterParameter registerParameter = registerWrapper.getParameter();
        ThreadPoolNotifyAlarm threadPoolNotifyAlarm = new ThreadPoolNotifyAlarm(
                BooleanUtil.toBoolean(String.valueOf(registerParameter.getIsAlarm())),
                registerParameter.getActiveAlarm(),
                registerParameter.getCapacityAlarm());
        GlobalNotifyAlarmManage.put(registerParameter.getThreadPoolId(), threadPoolNotifyAlarm);
        Map<String, List<NotifyConfigDTO>> builderNotify = notifyConfigBuilder.getAndInitNotify(CollectionUtil.newArrayList(registerParameter.getThreadPoolId()));
        hippo4jBaseSendMessageService.putPlatform(builderNotify);
    }
}
