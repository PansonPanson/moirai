package top.panson.common.toolkit;

import top.panson.common.monitor.AbstractMessage;
import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageWrapper;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 * @方法描述：消息转换器
 */
public class MessageConvert {

    /**
     * {@link Message} to {@link MessageWrapper}.
     *
     * @param message
     * @return
     */
    public static MessageWrapper convert(Message message) {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseClass(message.getClass());
        wrapper.setMessageType(message.getMessageType());
        List<Map<String, Object>> messageMapList = new ArrayList<>();
        List<Message> messages = message.getMessages();
        messages.forEach(each -> {
            String eachVal = JSONUtil.toJSONString(each);
            Map mapObj = JSONUtil.parseObject(eachVal, Map.class);
            messageMapList.add(mapObj);
        });
        wrapper.setContentParams(messageMapList);
        return wrapper;
    }

    /**
     * {@link MessageWrapper} to {@link Message}.
     *
     * @param messageWrapper
     * @return
     */
    @SneakyThrows
    public static Message convert(MessageWrapper messageWrapper) {
        AbstractMessage message = (AbstractMessage) messageWrapper.getResponseClass().getDeclaredConstructor().newInstance();
        List<Map<String, Object>> contentParams = messageWrapper.getContentParams();
        List<Message> messages = new ArrayList();
        contentParams.forEach(each -> {
            String eachVal = JSONUtil.toJSONString(each);
            Message messageObj = JSONUtil.parseObject(eachVal, messageWrapper.getResponseClass());
            messages.add(messageObj);
        });
        message.setMessages(messages);
        message.setMessageType(messageWrapper.getMessageType());
        return message;
    }
}
