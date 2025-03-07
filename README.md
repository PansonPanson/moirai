# Moirai
动态线程池

## 更新 log
### 增加可扩容的阻塞队列 ResizableCapacityLinkedBlockingQueue
主要是增加 capacity 动态更新入口（方法）
+ 将队列容量设置为新容量
+ 如果新的容量大于旧容量，则唤醒阻塞的入队线程

