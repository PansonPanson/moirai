package top.panson.config.monitor;

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageTypeEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @方法描述：监控数据处理策略选择器，实现了CommandLineRunner这个springboot扩展接口
 */
@Component
public class QueryMonitorExecuteChoose implements CommandLineRunner {


    //key就是解析器解析的类型，value就是解析器本身
    private Map<String, AbstractMonitorDataExecuteStrategy> monitorDataExecuteStrategyChooseMap = new HashMap<>();


    public AbstractMonitorDataExecuteStrategy choose(MessageTypeEnum messageTypeEnum) {
        return choose(messageTypeEnum.name());
    }


    public AbstractMonitorDataExecuteStrategy choose(String markType) {
        AbstractMonitorDataExecuteStrategy executeStrategy = monitorDataExecuteStrategyChooseMap.get(markType);
        if (executeStrategy == null) {
            executeStrategy = monitorDataExecuteStrategyChooseMap.get(MessageTypeEnum.DEFAULT.name());
        }
        return executeStrategy;
    }


    public void chooseAndExecute(Message message) {
        MessageTypeEnum messageType = message.getMessageType();
        AbstractMonitorDataExecuteStrategy executeStrategy = choose(messageType);
        executeStrategy.execute(message);
    }


    //该方法是CommandLineRunner接口中的方法，被调用的时候会收集springboot容器中的所有线程池信息解析器，把这些解析器放到monitorDataExecuteStrategyChooseMap中
    @Override
    public void run(String... args) throws Exception {
        Map<String, AbstractMonitorDataExecuteStrategy> monitorDataExecuteStrategyMap =
                ApplicationContextHolder.getBeansOfType(AbstractMonitorDataExecuteStrategy.class);
        monitorDataExecuteStrategyMap.values().forEach(each -> monitorDataExecuteStrategyChooseMap.put(each.mark(), each));
    }
}

