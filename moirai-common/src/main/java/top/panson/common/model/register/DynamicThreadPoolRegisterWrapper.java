package top.panson.common.model.register;

import top.panson.common.model.notify.DynamicThreadPoolRegisterServerNotifyParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @方法描述：这个动态线程池的包装注册器对象，这个对象中封装了动态线程池中要注册到服务端的重要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicThreadPoolRegisterWrapper {


    //这个对象中封装了动态线程池的核心信息，什么核心线程，存活时间，最大线程等等重要信息，都在这个对象中
    private DynamicThreadPoolRegisterParameter parameter;

    //租户Id
    private String tenantId;

    //项目Id
    private String itemId;

    //下面这四个成员变量是用来判断动态线程池信息更新后是否通知用户的
    //都默认为false，等后面真正用到它们的时候，我再跟大家讲解
    private Boolean updateIfExists = Boolean.FALSE;

    private Boolean notifyUpdateIfExists = Boolean.FALSE;

    private DynamicThreadPoolRegisterServerNotifyParameter serverNotify;

    //private DynamicThreadPoolRegisterCoreNotifyParameter configNotify;
}

