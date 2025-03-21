package top.panson.springboot.start.support;

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.constant.Constants;
import top.panson.common.enums.EnableEnum;
import top.panson.common.executor.support.BlockingQueueTypeEnum;
import top.panson.common.executor.support.RejectedPolicyTypeEnum;
import top.panson.common.model.ThreadPoolParameterInfo;
import top.panson.common.model.register.DynamicThreadPoolRegisterParameter;
import top.panson.common.model.register.DynamicThreadPoolRegisterWrapper;
import top.panson.common.toolkit.BooleanUtil;
import top.panson.common.toolkit.JSONUtil;
import top.panson.common.toolkit.ReflectUtil;
import top.panson.common.web.base.Result;
import top.panson.core.executor.DynamicThreadPool;
import top.panson.core.executor.DynamicThreadPoolExecutor;
import top.panson.core.executor.DynamicThreadPoolWrapper;
import top.panson.core.executor.manage.GlobalThreadPoolManage;
import top.panson.core.executor.support.adpter.DynamicThreadPoolAdapterChoose;
import top.panson.core.toolkit.DynamicThreadPoolAnnotationUtil;
import top.panson.message.service.GlobalNotifyAlarmManage;
import top.panson.message.service.ThreadPoolNotifyAlarm;
import top.panson.springboot.start.config.BootstrapProperties;
import top.panson.springboot.start.core.DynamicThreadPoolSubscribeConfig;
import top.panson.springboot.start.remote.HttpAgent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static top.panson.common.constant.Constants.*;


/**
 * @方法描述：这个就是动态线程池对象的处理器，具体逻辑是这样的，大家在创建动态线程池对象的时候，可以看看DynamicThreadPoolConfig类中给出的几个现成例子
 * 用户创建的动态线程池对象既可以以Executor的类型交给spring容器管理，也可以以ThreadPoolExecutor的类型交给spring容器管理，还可以直接以DynamicThreadPoolExecutor的类型交给spring的容器来管理
 * 当然，这些只是表面现象，使用不同的接口或者类型来接收动态线程池对象，实际上在内部，用户创建的就是一个DynamicThreadPoolExecutor对象，当然，这个DynamicThreadPoolExecutor
 * 对象也是继承了jdk的ThreadPoolExecutor对象。而当前的这个bean处理器，就是根据DynamicThreadPoolExecutor不同的返回类型
 * 得到真正创建的DynamicThreadPoolExecutor对象，然后把这个DynamicThreadPoolExecutor对象包装成DynamicThreadPoolWrapper对象，然后把这个对象保存到线程池动态管理器中
 * 并且把要注册对象信息交给DynamicThreadPoolConfigService对象，让DynamicThreadPoolConfigService对象去执行注册操作。简单概括一下，那就是DynamicThreadPoolPostProcessor处理器就是用来得到
 * DynamicThreadPoolExecutor对象，然后把该对象保存到线程池动态管理器中，并且调用DynamicThreadPoolConfigService对象执行注册操作的。
 */
@Slf4j
@AllArgsConstructor
public final class DynamicThreadPoolPostProcessor implements BeanPostProcessor {

    //配置信息对象
    private final BootstrapProperties properties;

    //客户端代理对象
    private final HttpAgent httpAgent;

    //订阅服务端动态线程池配置信息的对象
    private final DynamicThreadPoolSubscribeConfig dynamicThreadPoolSubscribeConfig;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }


    /**
     *
     * @方法描述：这个方法就是本类最核心的方法，用来处理DynamicThreadPoolExecutor对象
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //这里会先判断一下传进来的bean是否属于DynamicThreadPoolExecutor类型，如果大家看了我在DynamicThreadPoolConfig类提供的几个例子
        //就会发现我创建动态线程池对象最终是以Executor或者ThreadPoolExecutor形式返回的，如果是以Executor形式返回的，这个Executor接收的还并不是一个DynamicThreadPoolExecutor对象
        //而是一个ExecutorTtlWrapper对象，这个ExecutorTtlWrapper对象的作用我已经在DynamicThreadPoolConfig类中解释了，这时候，ExecutorTtlWrapper对象肯定就不属于DynamicThreadPoolExecutor类型了
        //但是先别急，虽然ExecutorTtlWrapper对象不属于DynamicThreadPoolExecutor类型，但是后面的DynamicThreadPoolAdapterChoose.match(bean)这个条件还是可以通过的，所以仍然可以进入下面的分支
        //那为什么要执行DynamicThreadPoolAdapterChoose.match(bean)这行代码呢？原因也很简单，因为有时候用户可能会使用spring本身的线程池，或者其他第三方形式的线程池，比如ExecutorTtl，比如spring的ThreadPoolTaskExecutor
        //该动态线程池框架也想收集这些线程池的信息，所以就会在DynamicThreadPoolAdapterChoose.match(bean)中判断程序内是否有这些第三方线程池的适配器，如果有，就可以使用这些适配器把这些第三方线程池转换成DynamicThreadPoolExecutor对象
        //之后的逻辑就和处理真正的DynamicThreadPoolExecutor对象一样了，无非就是把线程池信息注册到服务端，然后把线程池保存在线程池全局管理器中
        //DynamicThreadPoolAdapterChoose.match(bean)就是判断bean的类型是否为ThreadPoolTaskExecutor、ExecutorTtlWrapper、ExecutorServiceTtlWrapper中的一个，这些都是第三方的线程池
        if (bean instanceof DynamicThreadPoolExecutor || DynamicThreadPoolAdapterChoose.match(bean)) {
            DynamicThreadPool dynamicThreadPool;
            try {//判断该线程池对象上是否存在DynamicThreadPool注解
                dynamicThreadPool = ApplicationContextHolder.findAnnotationOnBean(beanName, DynamicThreadPool.class);
                //如果找不到该注解，就进入下面这个分支
                if (Objects.isNull(dynamicThreadPool)) {
                    //Adapt to lower versions of SpringBoot.
                    //这里就是为了适配SpringBoot低版本，使用DynamicThreadPoolAnnotationUtil工具再次查找注解
                    dynamicThreadPool = DynamicThreadPoolAnnotationUtil.findAnnotationOnBean(beanName, DynamicThreadPool.class);
                    if (Objects.isNull(dynamicThreadPool)) {
                        //还是找不到则直接返回bean即可
                        return bean;
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to create dynamic thread pool in annotation mode.", ex);
                return bean;
            }//走到这里意味着当前的bean上有DynamicThreadPool，也就意味着是一个动态线程池，下面就要收集动态线程池信息了
            DynamicThreadPoolExecutor dynamicThreadPoolExecutor;
            //这里bean的类型为ExecutorTtlWrapper，所以会在DynamicThreadPoolAdapterChoose.unwrap方法中
            //将bean转换为dynamicThreadPoolExecutor类型，确切地说不是把当前要交给容器的这个bean转换成dynamicThreadPoolExecutor对象
            //实际上ExecutorTtlWrapper只是持有了dynamicThreadPoolExecutor的引用，这里只不过是直接利用反射从ExecutorTtlWrapper把dynamicThreadPoolExecutor对象取出来了
            if ((dynamicThreadPoolExecutor = DynamicThreadPoolAdapterChoose.unwrap(bean)) == null) {
                dynamicThreadPoolExecutor = (DynamicThreadPoolExecutor) bean;
            }//将刚刚得到的dynamicThreadPoolExecutor对象包装成一个DynamicThreadPoolWrapper对象，这个对象会被交给线程池全局管理器来管理
            //之后收集线程池运行信息时都要用到这个对象
            DynamicThreadPoolWrapper dynamicThreadPoolWrapper = new DynamicThreadPoolWrapper(dynamicThreadPoolExecutor.getThreadPoolId(), dynamicThreadPoolExecutor);
            //在这里把动态线程池的信息注册给服务端了
            ThreadPoolExecutor remoteThreadPoolExecutor = fillPoolAndRegister(dynamicThreadPoolWrapper);
            //这里还做了一个操作，如果这个bean真的是第三方线程池，那就将第三方线程池中的持有的真正的线程池对象替换成从服务端得到的线程池对象
            //因服务端也许也配置了动态线程池的信息，一切都已服务端为准，所以从fillPoolAndRegister方法返回的就是以服务端为准的动态线程池
            //这时候把它替换到第三方线程池中即可，这样程序中的线程池就会以最新的配置参数来运行了
            DynamicThreadPoolAdapterChoose.replace(bean, remoteThreadPoolExecutor);
            //这里还有一个操作，就是订阅服务端线程池的信息，订阅之后，服务端线程池信息一旦更新，就会通知客户端动态更新线程池信息
            subscribeConfig(dynamicThreadPoolWrapper);
            return DynamicThreadPoolAdapterChoose.match(bean) ? bean : remoteThreadPoolExecutor;
        }//这里就是判断一下，如果在配置类中创建动态线程池时就被包装成DynamicThreadPoolWrapper对象交给spring容器了，这里就直接注册信息到服务端即可
        if (bean instanceof DynamicThreadPoolWrapper) {
            DynamicThreadPoolWrapper dynamicThreadPoolWrapper = (DynamicThreadPoolWrapper) bean;
            registerAndSubscribe(dynamicThreadPoolWrapper);
        }
        return bean;
    }


    //注册线程池信息到服务端的方法，这个方法也会订阅服务端的线程池信息，但这里我把订阅的功能省略了
    protected void registerAndSubscribe(DynamicThreadPoolWrapper dynamicThreadPoolWrapper) {
        fillPoolAndRegister(dynamicThreadPoolWrapper);
        subscribeConfig(dynamicThreadPoolWrapper);
    }




    /**
     *
     * @方法描述：注册线程池信息到服务端的方法
     */
    protected ThreadPoolExecutor fillPoolAndRegister(DynamicThreadPoolWrapper dynamicThreadPoolWrapper) {
        //先得到要注册的线程池的Id
        String threadPoolId = dynamicThreadPoolWrapper.getThreadPoolId();
        //得到真正的DynamicThreadPoolExecutor动态线程池
        ThreadPoolExecutor executor = dynamicThreadPoolWrapper.getExecutor();
        //封装线程池Id，命名空间，项目Id信息
        Map<String, String> queryStrMap = new HashMap(3);
        queryStrMap.put(TP_ID, threadPoolId);
        queryStrMap.put(ITEM_ID, properties.getItemId());
        queryStrMap.put(NAMESPACE, properties.getNamespace());
        //创建封装线程池参数信息的对象
        ThreadPoolParameterInfo threadPoolParameterInfo;
        try {//这里做了一个访问服务端的操作，这是因为也许用户通过web界面，已经实现在服务端定义好了线程池的配置信息
            //所以要以服务端的信息为主，因此在这里先访问服务端，看看服务端有没有设置好的动态线程池信息，其实就是去服务端查询数据库而已
            //这里访问的就是服务端的ConfigController类的detailConfigInfo方法
            Result result = httpAgent.httpGetByConfig(Constants.CONFIG_CONTROLLER_PATH, null, queryStrMap, 5000L);
            //判断返回的结果中是否存在最新的线程池配置信息
            if (result.isSuccess() && result.getData() != null) {
                //如果存在就获取信息，然后转换成threadPoolParameterInfo对象
                String resultJsonStr = JSONUtil.toJSONString(result.getData());
                if ((threadPoolParameterInfo = JSONUtil.parseObject(resultJsonStr, ThreadPoolParameterInfo.class)) != null) {
                    //在这里刷新本地动态线程池的信息
                    threadPoolParamReplace(executor, threadPoolParameterInfo);
                    //在客户端缓存线程池的告警配置信息
                    registerNotifyAlarm(threadPoolParameterInfo);
                    //然后把动态线程池交给本地的全局线程池管理器管理即可
                    //注意，这个时候从服务端返回了最新的动态线程池信息，就意味着动态线程池在之前注册到服务端了，并且信息已经存放到数据库了
                    //所以就不用再重复注册了，至于后面线程池的信息怎么动态刷新，这个后面再为大家实现
                    GlobalThreadPoolManage.register(dynamicThreadPoolWrapper.getThreadPoolId(), threadPoolParameterInfo, dynamicThreadPoolWrapper);
                }
            } else {
                //源码中这个分支有一个严重的bug，会导致程序无法正常运行，我给修正了一下
                //如果走到这里就意味着服务端没有当前动态线程池的任何信息，那就要在客户端构建一个DynamicThreadPoolRegisterWrapper对象，然后把这个对象直接发送给服务端，进行注册即可
                //这里创建的这个DynamicThreadPoolRegisterParameter对象封装了动态线程池的核心参数信息
                DynamicThreadPoolRegisterParameter parameterInfo = DynamicThreadPoolRegisterParameter.builder()
                        //线程Id
                        .threadPoolId(threadPoolId)
                        //核心线程
                        .corePoolSize(executor.getCorePoolSize())
                        //最大线程数
                        .maximumPoolSize(executor.getMaximumPoolSize())
                        //阻塞队列的类型
                        .blockingQueueType(BlockingQueueTypeEnum.getBlockingQueueTypeEnumByName(executor.getQueue().getClass().getSimpleName()))
                        //队列容量
                        .capacity(executor.getQueue().remainingCapacity())
                        //是否允许核心线程超过空闲时间后终止
                        .allowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut())
                        //线程存活时间
                        .keepAliveTime(executor.getKeepAliveTime(TimeUnit.MILLISECONDS))
                        //是否启动告警，这里是默认不启用，但是在web界面可以修改是否启动的功能
                        .isAlarm(false)
                        //告警的活跃线程数量阈值
                        .activeAlarm(80)
                        //告警的队列容量阈值
                        .capacityAlarm(80)
                        //拒绝策略类型
                        .rejectedPolicyType(RejectedPolicyTypeEnum.getRejectedPolicyTypeEnumByName(executor.getRejectedExecutionHandler().getClass().getSimpleName()))
                        //构建DynamicThreadPoolRegisterParameter对象，其实这里面其实还有几个成员变量没有赋值，比如线程名称前缀，线程池执行任务的超时时间
                        .build();
                //在这里使用parameterInfo创建了DynamicThreadPoolRegisterWrapper对象，这个对象要发送给服务端进行注册
                DynamicThreadPoolRegisterWrapper registerWrapper = DynamicThreadPoolRegisterWrapper.builder()
                        .parameter(parameterInfo)
                        .build();
                //将线程池信息注册到服务端，这里是通过线程池全局管理器来注册的
                GlobalThreadPoolManage.dynamicRegister(registerWrapper);
                //再次得到parameter信息
                ThreadPoolParameterInfo parameter = JSONUtil.parseObject(JSONUtil.toJSONString(parameterInfo), ThreadPoolParameterInfo.class);
                //注册服务端成功之后，再把动态线程池注册到本地的线程池全局管理器中,这里只需要把DynamicThreadPoolWrapper注册到线程池全局管理器中即可
                //因为注册线程池的动作并没有更新本地动态线程池，使用的就是客户端默认配置的线程池参数创建的动态线程池，所以直接存放DynamicThreadPoolWrapper对象即可
                GlobalThreadPoolManage.register(dynamicThreadPoolWrapper.getThreadPoolId(), parameter, dynamicThreadPoolWrapper);
            }
        } catch (Exception ex) {
            log.error("Failed to initialize thread pool configuration. error message: {}", ex.getMessage());
        }
        //返回executor，如果服务端存在配置信息，那么这里返回的就是经过刷新的动态线程池对象
        return executor;
    }


    //使用服务端传回来的动态线程池的配置信息刷新本地线程池信息
    private void threadPoolParamReplace(ThreadPoolExecutor executor, ThreadPoolParameterInfo threadPoolParameterInfo) {
        BlockingQueue workQueue = BlockingQueueTypeEnum.createBlockingQueue(threadPoolParameterInfo.getQueueType(), threadPoolParameterInfo.getCapacity());
        //利用反射设置队列
        ReflectUtil.setFieldValue(executor, "workQueue", workQueue);
        //下面的逻辑很简单，就不添加注释了
        executor.setCorePoolSize(threadPoolParameterInfo.corePoolSizeAdapt());
        executor.setMaximumPoolSize(threadPoolParameterInfo.maximumPoolSizeAdapt());
        executor.setKeepAliveTime(threadPoolParameterInfo.getKeepAliveTime(), TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(EnableEnum.getBool(threadPoolParameterInfo.getAllowCoreThreadTimeOut()));
        executor.setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(threadPoolParameterInfo.getRejectedType()));
        if (executor instanceof DynamicThreadPoolExecutor) {
            Optional.ofNullable(threadPoolParameterInfo.getExecuteTimeOut())
                    .ifPresent(executeTimeOut -> ((DynamicThreadPoolExecutor) executor).setExecuteTimeOut(executeTimeOut));
        }
    }

    //在客户端缓存线程池的告警配置信息的方法
    private void registerNotifyAlarm(ThreadPoolParameterInfo threadPoolParameterInfo) {
        ThreadPoolNotifyAlarm threadPoolNotifyAlarm = new ThreadPoolNotifyAlarm(
                BooleanUtil.toBoolean(threadPoolParameterInfo.getIsAlarm().toString()),
                threadPoolParameterInfo.getLivenessAlarm(),
                threadPoolParameterInfo.getCapacityAlarm());
        GlobalNotifyAlarmManage.put(threadPoolParameterInfo.getTpId(), threadPoolNotifyAlarm);
    }


    //订阅服务端动态线程池配置信息的方法
    protected void subscribeConfig(DynamicThreadPoolWrapper dynamicThreadPoolWrapper) {
        if (dynamicThreadPoolWrapper.isSubscribeFlag()) {
            dynamicThreadPoolSubscribeConfig.subscribeConfig(dynamicThreadPoolWrapper.getThreadPoolId());
        }
    }

}
