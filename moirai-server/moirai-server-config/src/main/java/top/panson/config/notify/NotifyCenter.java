package top.panson.config.notify;

import top.panson.common.toolkit.MapUtil;
import top.panson.config.event.AbstractEvent;
import top.panson.config.event.AbstractSlowEvent;
import top.panson.config.notify.listener.AbstractSmartSubscriber;
import top.panson.config.notify.listener.AbstractSubscriber;
import top.panson.config.toolkit.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：事件通知中心，这个类的代码也是直接把nacos的NotifyCenter的大部分代码搬运过来了，有很多类和判断其实根本用不上，我本来想替作者删除这些无关的代码
 * 但是又想了想，也许作者是给以后的某些功能预留了拓展口，虽然这个可能性并不大，所以就不删除多余的代码了，如果大家先看到的是动态线程池框架，然后再学习nacos
 * 那么nacos的事件通知机制你在动态线程池框架中就能掌握了，学习nacos的时候会简单很多
 */
@Slf4j
public class NotifyCenter {

    //事件通知中心的单例
    private static final NotifyCenter INSTANCE = new NotifyCenter();

    //这个成员变量是给事件发布器使用的，事件工厂创建事件发布器的时候，需要给事件发布器内部的队列定义长度
    //这时候就可以使用这个成员变量来定义。并且这个成员变量是给DefaultPublisher默认事件发布器使用的
    public static int ringBufferSize = 16384;

    //这个成员变量是给DefaultSharePublisher事件发布器使用的
    public static int shareBufferSize = 1024;

    //默认的共享事件发布器
    private DefaultSharePublisher sharePublisher;

    //默认的事件发布器
    private static EventPublisher eventPublisher = new DefaultPublisher();

    //事件发布器的工厂，这个工厂专门创建事件发布器
    private static BiFunction<Class<? extends AbstractEvent>, Integer, EventPublisher> publisherFactory;

    //这个就是事件通知中心最核心的成员变量了，专门用来存储事件和对应的发布器的map
    //key是事件名称，value就是对应的发布器，这个map存放的是DefaultPublisher事件发布器，如果是共享事件就会直接交给DefaultSharePublisher去处理了
    private final Map<String, EventPublisher> publisherMap = new ConcurrentHashMap(16);

    //在这里创建了事件发布器工厂，这个工厂就是专门创建事件发布器的
    //cls就是事件发布器关注的事件类型，buffer就是事件发布器中事件队列的大小
    static {
        publisherFactory = (cls, buffer) -> {
            try {//这里需要注意一下，并不是每注册一个订阅者，就要为这个订阅者创建一个事件发布器
                //而是每个事件类型，就要定义一个对应的事件发布器。如果多个订阅者都订阅的是服务实例变更事件
                //那么只需要一个发布器即可，这些订阅者会被存放到发布器内部的set集合中
                EventPublisher publisher = eventPublisher;
                publisher.init(cls, buffer);
                return publisher;
            } catch (Throwable ex) {
                log.error("Service class newInstance has error: {}", ex);
                throw new RuntimeException(ex);
            }
        };//在这里创建了默认的共享事件发布器
        INSTANCE.sharePublisher = new DefaultSharePublisher();
        INSTANCE.sharePublisher.init(AbstractSlowEvent.class, shareBufferSize);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：注册订阅者到事件通知中心的方法
     */
    public static void registerSubscriber(final AbstractSubscriber consumer) {
        //这里判断一下订阅者是不是SmartSubscriber类型的，这个SmartSubscriber类型提供了订阅者可以处理多个事件的功能
        //普通的订阅者只能订阅一种事件，但是SmartSubscriber类型的订阅者可以订阅多个事件，这也是nacos中的一个重要组件，hippo4j的作者直接把nacos中的代码复制过来了
        //实际上在当前的框架中根本用不到这些类，除非作者以后会扩展其他的功能
        if (consumer instanceof AbstractSmartSubscriber) {
            //如果是处理多个事件的订阅者
            for (Class<? extends AbstractEvent> subscribeType : ((AbstractSmartSubscriber) consumer).subscribeTypes()) {
                //在判断一下这个订阅者关注的事件有没有慢事件，如果有就把它添加到共享事件发布器中
                if (ClassUtil.isAssignableFrom(AbstractSlowEvent.class, subscribeType)) {
                    INSTANCE.sharePublisher.addSubscriber(consumer, subscribeType);
                } else {//走到这里意味着没有慢事件，那就直接添加到publisherMap中
                    addSubscriber(consumer, subscribeType);
                }
            }
            return;
        }//走到这里意味着订阅者订阅的就是单一事件，获得订阅者订阅的事件
        final Class<? extends AbstractEvent> subscribeType = consumer.subscribeType();
        //判断是否为慢事件，如果是慢事件就添加到共享事件通知器中
        if (ClassUtil.isAssignableFrom(AbstractSlowEvent.class, subscribeType)) {
            INSTANCE.sharePublisher.addSubscriber(consumer, subscribeType);
            return;
        }//如果不是慢事件就添加到publisherMap中
        addSubscriber(consumer, subscribeType);
    }

    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：把订阅者添加到通知中心的方法
     */
    private static void addSubscriber(final AbstractSubscriber consumer, Class<? extends AbstractEvent> subscribeType) {
        //根据订阅者订阅的事件类型获得一个topic，其实就是订阅事件的字符串
        final String topic = ClassUtil.getCanonicalName(subscribeType);
        synchronized (NotifyCenter.class) {
            //判断publisherMap中是否存在了对应的事件发布器，如果不存在，就以topic为key事件工厂创建的事件发布器为map为value
            //把键值对放到publisherMap中，到这里大家可以明白了，所有的订阅者其实都会存放到事件发布器中，而事件发布器又会存放到事件通知中心的publisherMap中
            //所以时间通知中心一旦发布事件，就可以从publisherMap中获得对应的事件发布器，事件发布器再进一步执行内部存放的订阅者的回调方法即可
            MapUtil.computeIfAbsent(INSTANCE.publisherMap, topic, publisherFactory, subscribeType, ringBufferSize);
        }//得到事件对应的事件发布器
        EventPublisher publisher = INSTANCE.publisherMap.get(topic);
        //添加订阅者到事件发布器中
        publisher.addSubscriber(consumer);
    }

    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：事件通知中心发布事件的方法
     */
    public static boolean publishEvent(final AbstractEvent event) {
        try {//发布事件类型
            return publishEvent(event.getClass(), event);
        } catch (Throwable ex) {
            log.error("There was an exception to the message publishing: {}", ex);
            return false;
        }
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：事件通知中心发布事件的方法
     */
    private static boolean publishEvent(final Class<? extends AbstractEvent> eventType, final AbstractEvent event) {
        //判断发布的事件是否为慢事件
        if (ClassUtil.isAssignableFrom(AbstractSlowEvent.class, eventType)) {
            //如果是慢事件直接交给共享事件发布器来发布
            return INSTANCE.sharePublisher.publish(event);
        }//得到事件的topic名称
        final String topic = ClassUtil.getCanonicalName(eventType);
        //从publisherMap中得到对应的事件发布器
        EventPublisher publisher = INSTANCE.publisherMap.get(topic);
        if (publisher != null) {
            //发布事件
            return publisher.publish(event);
        }
        log.warn("There are no [{}] publishers for this event, please register", topic);
        return false;
    }

    //注册事件发布器到事件中心的方法
    public static EventPublisher registerToPublisher(final Class<? extends AbstractEvent> eventType, final int queueMaxSize) {
        if (ClassUtil.isAssignableFrom(AbstractSlowEvent.class, eventType)) {
            return INSTANCE.sharePublisher;
        }
        final String topic = ClassUtil.getCanonicalName(eventType);
        synchronized (NotifyCenter.class) {
            MapUtil.computeIfAbsent(INSTANCE.publisherMap, topic, publisherFactory, eventType, queueMaxSize);
        }
        return INSTANCE.publisherMap.get(topic);
    }
}
