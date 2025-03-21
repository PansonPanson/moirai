

package top.panson.message.service;

import top.panson.common.api.ThreadPoolConfigChange;
import top.panson.common.toolkit.StringUtil;
import top.panson.core.toolkit.IdentifyUtil;
import top.panson.message.request.ChangeParameterNotifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;


/**
 *
 * @方法描述：在线程池的配置信息动态刷新之后，给用户发送通知消息的类
 */ 
@RequiredArgsConstructor
public class DefaultThreadPoolConfigChangeHandler implements ThreadPoolConfigChange<ChangeParameterNotifyRequest> {

    @Value("${spring.profiles.active:UNKNOWN}")
    private String active;

    @Value("${spring.dynamic.thread-pool.item-id:}")
    private String itemId;

    @Value("${spring.application.name:UNKNOWN}")
    private String applicationName;

    private final Hippo4jSendMessageService hippo4jSendMessageService;



    //该方法会在ServerThreadPoolDynamicRefresh对象中被调用
    @Override
    public void sendPoolConfigChange(ChangeParameterNotifyRequest requestParam) {
        requestParam.setActive(active.toUpperCase());
        String appName = StringUtil.isBlank(itemId) ? applicationName : itemId;
        requestParam.setAppName(appName);
        requestParam.setIdentify(IdentifyUtil.getIdentify());
        hippo4jSendMessageService.sendChangeMessage(requestParam);
    }
}
