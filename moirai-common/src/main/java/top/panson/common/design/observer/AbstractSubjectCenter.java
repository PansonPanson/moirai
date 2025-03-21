package top.panson.common.design.observer;

import top.panson.common.toolkit.CollectionUtil;
import top.panson.common.toolkit.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class AbstractSubjectCenter {


    private static final Map<String, List<Observer>> OBSERVERS_MAP = new ConcurrentHashMap<>();


    public static void register(Observer observer) {
        register(SubjectType.SPRING_CONTENT_REFRESHED.name(), observer);
    }


    public static void register(SubjectType subjectType, Observer observer) {
        register(subjectType.name(), observer);
    }


    public static void register(String subject, Observer observer) {
        if (StringUtil.isBlank(subject) || observer == null) {
            log.warn("注册观察者失败。传入的主题为空或观察者对象为空。");
            return;
        }
        List<Observer> observers = OBSERVERS_MAP.get(subject);
        if (CollectionUtil.isEmpty(observers)) {
            observers = new ArrayList<>();
        }
        observers.add(observer);
        OBSERVERS_MAP.put(subject, observers);
    }


    public static void remove(Observer observer) {
        remove(SubjectType.SPRING_CONTENT_REFRESHED.name(), observer);
    }


    public static void remove(String subject, Observer observer) {
        List<Observer> observers = OBSERVERS_MAP.get(subject);
        if (StringUtil.isBlank(subject) || CollectionUtil.isEmpty(observers) || observer == null) {
            log.warn("移除观察者失败。传入的主题为空或观察者对象为空。");
            return;
        }
        observers.remove(observer);
    }


    public static void notify(SubjectType subjectType, ObserverMessage observerMessage) {
        notify(subjectType.name(), observerMessage);
    }


    public static void notify(String subject, ObserverMessage observerMessage) {
        List<Observer> observers = OBSERVERS_MAP.get(subject);
        if (CollectionUtil.isEmpty(observers)) {
            log.warn("Under the subject, there is no observer group.");
            return;
        }
        observers.parallelStream().forEach(each -> {
            try {
                each.accept(observerMessage);
            } catch (Exception ex) {
                log.error("Notification subject: {} observer exception", subject);
            }
        });
    }



    public enum SubjectType {

        //spring容器刷新事件
        SPRING_CONTENT_REFRESHED,

        //缓存清理事件
        CLEAR_CONFIG_CACHE
    }
}
