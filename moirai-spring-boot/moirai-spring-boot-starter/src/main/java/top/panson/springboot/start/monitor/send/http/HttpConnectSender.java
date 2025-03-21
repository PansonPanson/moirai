package top.panson.springboot.start.monitor.send.http;


import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageWrapper;
import top.panson.common.toolkit.MessageConvert;
import top.panson.springboot.start.monitor.send.MessageSender;
import top.panson.springboot.start.remote.HttpAgent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static top.panson.common.constant.Constants.MONITOR_PATH;

/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：运行时信息发送器，这个发送器会把线程池的运行时任务发送给服务端
 */
@Slf4j
@AllArgsConstructor
public class HttpConnectSender implements MessageSender {

    private final HttpAgent httpAgent;

    @Override
    public void send(Message message) {
        try {
            MessageWrapper messageWrapper = MessageConvert.convert(message);
            //因为我还没有实现服务端接收客户端上报信息的接口
            //所以在第五版本代码中，程序运行客户端控制台会一直报错，因为信息发送不过去。但这并不妨碍我们测试其他功能，大家暂时忽略错误即可
            httpAgent.httpPost(MONITOR_PATH, messageWrapper);
        } catch (Throwable ex) {
            log.error("Failed to push dynamic thread pool runtime data.", ex);
        }
    }
}
