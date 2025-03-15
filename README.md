# Moirai
动态线程池

## 目录
+ [为什么Java要设计一个线程池？](https://github.com/PansonPanson/moirai/blob/main/doc/001_Java%E4%B8%BA%E4%BB%80%E4%B9%88%E8%A6%81%E8%AE%BE%E8%AE%A1%E7%BA%BF%E7%A8%8B%E6%B1%A0%EF%BC%9F.md)
+ [万字长文之线程池源码深入分析](https://github.com/PansonPanson/moirai/blob/main/doc/002_%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%BA%90%E7%A0%81%E6%B7%B1%E5%85%A5%E5%88%86%E6%9E%90.md)
+ [线程池源码涉及到的位运算以及相应算法练习](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%BA%90%E7%A0%81%E6%B6%89%E5%8F%8A%E5%88%B0%E7%9A%84%E4%BD%8D%E8%BF%90%E7%AE%97%E4%BB%A5%E5%8F%8A%E7%9B%B8%E5%BA%94%E7%AE%97%E6%B3%95%E7%BB%83%E4%B9%A0.md) 
+ [线程池源码涉及到的链表结构以及相应算法练习](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%BA%90%E7%A0%81%E6%B6%89%E5%8F%8A%E5%88%B0%E7%9A%84%E9%93%BE%E8%A1%A8%E7%BB%93%E6%9E%84%E4%BB%A5%E5%8F%8A%E7%9B%B8%E5%BA%94%E7%AE%97%E6%B3%95%E7%BB%83%E4%B9%A0.md)
+ [LinkedBlockingQueue 原理与源码深度分析](https://github.com/PansonPanson/moirai/blob/main/doc/LinkedBlockingQueue%20%E5%8E%9F%E7%90%86%E4%B8%8E%E6%BA%90%E7%A0%81%E6%B7%B1%E5%BA%A6%E5%88%86%E6%9E%90.md)
+ [核心线程数为0时，线程池如何执行？](https://github.com/PansonPanson/moirai/blob/main/doc/%E6%A0%B8%E5%BF%83%E7%BA%BF%E7%A8%8B%E6%95%B0%E4%B8%BA%200%20%E6%97%B6%EF%BC%8C%E7%BA%BF%E7%A8%8B%E6%B1%A0%E5%A6%82%E6%9E%9C%E6%89%A7%E8%A1%8C%E4%BB%BB%E5%8A%A1%EF%BC%9F.md) 
+ 线程池异常后：销毁还是复用 
+ 线程池的核心线程会被回收吗？ 
+ 线程池提交一个任务占多大内存？ 
+ 服务down机了，线程池中如何保证不丢失数据 
+ 如何设计一个线程池 
+ 线程池如何监控 
+ 线程池10连问 
+ JVM STW 和 Dubbo 线程池耗尽的相关性 
+ 虚拟线程原理及性能分析 
+ ForkJoinPool 
+ Spring 的线程池设计
    a. Spring中Async注解底层异步线程池原理 
+ Dubbo 的线程池设计 
+ RocketMQ 的线程池设计 
+ Tomcat 线程池改造

## 更新 log
### 增加可扩容的阻塞队列 ResizableCapacityLinkedBlockingQueue
主要是增加 capacity 动态更新入口（方法）
+ 将队列容量设置为新容量
+ 如果新的容量大于旧容量，则唤醒阻塞的入队线程

