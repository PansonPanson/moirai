package top.panson.common.design.observer;

/**
 * Message notifying observer.
 */
public interface ObserverMessage<T> {

    T message();
}
