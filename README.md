# Moirai
Moirai 是一个参考 Hippo4j 写的动态线程池框架，核心逻辑与 Hippo4j 一致，主要用于个人学习，在亲手造轮子中学习 Hippo4j 源码，以及学习动态线程池的设计原理与编码实践。

除了框架本身以外，本仓库还较为系统性整理了动态线程池相关的知识，欢迎交流。

### 如何设计一个动态线程池框架？
+ [明确监控的线程池信息（直接读取的核心参数、加工后的数据）.md](doc/%E6%98%8E%E7%A1%AE%E7%9B%91%E6%8E%A7%E7%9A%84%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%BF%A1%E6%81%AF%EF%BC%88%E7%9B%B4%E6%8E%A5%E8%AF%BB%E5%8F%96%E7%9A%84%E6%A0%B8%E5%BF%83%E5%8F%82%E6%95%B0%E3%80%81%E5%8A%A0%E5%B7%A5%E5%90%8E%E7%9A%84%E6%95%B0%E6%8D%AE%EF%BC%89.md)
+ [明确可以动态更新的线程池信息.md](doc/%E6%98%8E%E7%A1%AE%E5%8F%AF%E4%BB%A5%E5%8A%A8%E6%80%81%E6%9B%B4%E6%96%B0%E7%9A%84%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%BF%A1%E6%81%AF.md)
+ [自定义可变容量的阻塞队列.md](doc/%E8%87%AA%E5%AE%9A%E4%B9%89%E5%8F%AF%E5%8F%98%E5%AE%B9%E9%87%8F%E7%9A%84%E9%98%BB%E5%A1%9E%E9%98%9F%E5%88%97.md)
+ [动态线程池的扩展点以及插件体系.md](doc/%E5%8A%A8%E6%80%81%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%9A%84%E6%89%A9%E5%B1%95%E7%82%B9%E4%BB%A5%E5%8F%8A%E6%8F%92%E4%BB%B6%E4%BD%93%E7%B3%BB.md)
+ [提供默认插件注册功能.md](doc/%E6%8F%90%E4%BE%9B%E9%BB%98%E8%AE%A4%E6%8F%92%E4%BB%B6%E6%B3%A8%E5%86%8C%E5%8A%9F%E8%83%BD.md)
+ [使用 Builder 模式构建动态线程池.md](doc/%E4%BD%BF%E7%94%A8%20Builder%20%E6%A8%A1%E5%BC%8F%E6%9E%84%E5%BB%BA%E5%8A%A8%E6%80%81%E7%BA%BF%E7%A8%8B%E6%B1%A0.md)
+ [线程池配置信息注册到服务端.md](doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E9%85%8D%E7%BD%AE%E4%BF%A1%E6%81%AF%E6%98%AF%E5%A6%82%E4%BD%95%E6%B3%A8%E5%86%8C%E5%88%B0%E6%9C%8D%E5%8A%A1%E7%AB%AF%E7%9A%84%3F.md)
+ [适配 TTL 和 Spring ThreadPoolTaskExecutor.md](doc/%E9%80%82%E9%85%8D%20TTL%20%E5%92%8C%20Spring%20ThreadPoolTaskExecutor.md)
+ [实现客户端与服务端之间的服务发现.md](doc/%E5%AE%9E%E7%8E%B0%E5%AE%A2%E6%88%B7%E7%AB%AF%E4%B8%8E%E6%9C%8D%E5%8A%A1%E7%AB%AF%E4%B9%8B%E9%97%B4%E7%9A%84%E6%9C%8D%E5%8A%A1%E5%8F%91%E7%8E%B0.md)
+ [实现客户端与服务端之间的心跳检测（续约）.md](doc/%E5%AE%9E%E7%8E%B0%E5%AE%A2%E6%88%B7%E7%AB%AF%E4%B8%8E%E6%9C%8D%E5%8A%A1%E7%AB%AF%E4%B9%8B%E9%97%B4%E7%9A%84%E5%BF%83%E8%B7%B3%E6%A3%80%E6%B5%8B%EF%BC%88%E7%BB%AD%E7%BA%A6%EF%BC%89.md)
+ [实现过期服务移除功能.md](doc/%E5%AE%9E%E7%8E%B0%E8%BF%87%E6%9C%9F%E6%9C%8D%E5%8A%A1%E7%A7%BB%E9%99%A4%E5%8A%9F%E8%83%BD.md)
+ [使用长轮询实现配置信息的动态变更.md](doc/%E4%BD%BF%E7%94%A8%E9%95%BF%E8%BD%AE%E8%AF%A2%E5%AE%9E%E7%8E%B0%E9%85%8D%E7%BD%AE%E4%BF%A1%E6%81%AF%E7%9A%84%E5%8A%A8%E6%80%81%E5%8F%98%E6%9B%B4.md)
+ ……

## 一、模块依赖关系
### moirai-server-bootstrap maven 依赖关系（project module）

```
moirai-server-bootstrap 
└── moirai-server-console 
    ├── moirai-server-config
    │   ├── moirai-common 
    │   └── moirai-adapter 
    │       └── moirai-core 
    │           └── moirai-server-monitor-base 
    ├── moirai-server-discovery 
    └── moirai-server-auth 
```


1. 根节点是 `moirai-server-bootstrap`
2. 直接依赖是 `moirai-server-console`（编译作用域）
3. 二级依赖包含三个模块： 
   - 配置模块 `moirai-server-config`
   - 服务发现模块 `moirai-server-discovery`
   - 认证模块 `moirai-server-auth`
4. 配置模块延伸出基础技术栈：
   - 公共模块 → 适配器 → 核心模块 → 监控基础模块


```mermaid
graph TD
    A[moirai-server-bootstrap] --> B[moirai-server-console]
    B --> C[moirai-server-config]
    B --> D[moirai-server-discovery]
    B --> E[moirai-server-auth]
    C --> F[moirai-common]
    C --> G[moirai-adapter]
    G --> H[moirai-core]
    H --> I[moirai-server-monitor-base]
```


### moirai-example maven 依赖关系（project module）
依赖关系图：

```mermaid
graph TD
    A[moirai-example] --> B[moirai-spring-boot-starter]
    B --> C[moirai-core]
    C --> D[moirai-common]
    B --> E[moirai-server-monitor-base]
    B --> F[moirai-adapter]
    B --> G[moirai-message]
```


**层级结构**：
    - Level 0: 根模块 `moirai-example`
    - Level 1: 核心依赖 `moirai-spring-boot-starter`
    - Level 2: Starter 的四个直接子模块
    - Level 3: `moirai-core` 的唯一子模块 `moirai-common`

## 二、插件体系

```mermaid
classDiagram
direction BT
class AbstractTaskTimerPlugin {
  + AbstractTaskTimerPlugin() 
  + beforeExecute(Thread, Runnable) void
  # currentTime() long
  # processTaskTime(long) void
  + afterExecute(Runnable, Throwable) void
}
class ExecuteAwarePlugin {
<<Interface>>
  + beforeExecute(Thread, Runnable) void
  + afterExecute(Runnable, Throwable) void
}
class RejectedAwarePlugin {
<<Interface>>
  + beforeRejectedExecution(Runnable, ThreadPoolExecutor) void
}
class ShutdownAwarePlugin {
<<Interface>>
  + afterShutdown(ThreadPoolExecutor, List~Runnable~) void
  + afterTerminated(ExtensibleThreadPoolExecutor) void
  + beforeShutdown(ThreadPoolExecutor) void
}
class Summary {
  + Summary(long, long, long, long) 
  - long taskCount
  - long minTaskTimeMillis
  - long totalTaskTimeMillis
  - long maxTaskTimeMillis
   long maxTaskTimeMillis
   long taskCount
   long minTaskTimeMillis
   long totalTaskTimeMillis
   long avgTaskTimeMillis
}
class TaskAwarePlugin {
<<Interface>>
  + beforeTaskCreate(ThreadPoolExecutor, Runnable, V) Runnable
  + beforeTaskCreate(ThreadPoolExecutor, Callable~V~) Callable~V~
  + beforeTaskExecute(Runnable) Runnable
}
class TaskDecoratorPlugin {
  + TaskDecoratorPlugin() 
  - List~TaskDecorator~ decorators
  + removeDecorator(TaskDecorator) void
  + clearDecorators() void
  + beforeTaskExecute(Runnable) Runnable
  + addDecorator(TaskDecorator) void
   PluginRuntime pluginRuntime
   String id
   List~TaskDecorator~ decorators
}
class TaskRejectCountRecordPlugin {
  + TaskRejectCountRecordPlugin() 
  - AtomicLong rejectCount
  + beforeRejectedExecution(Runnable, ThreadPoolExecutor) void
   PluginRuntime pluginRuntime
   Long rejectCountNum
   AtomicLong rejectCount
   String id
}
class TaskRejectNotifyAlarmPlugin {
  + TaskRejectNotifyAlarmPlugin() 
  + beforeRejectedExecution(Runnable, ThreadPoolExecutor) void
   String id
}
class TaskTimeRecordPlugin {
  + TaskTimeRecordPlugin() 
  + summarize() Summary
  # processTaskTime(long) void
   PluginRuntime pluginRuntime
   String id
}
class TaskTimeoutNotifyAlarmPlugin {
  + TaskTimeoutNotifyAlarmPlugin(String, Long, ThreadPoolExecutor) 
  - Long executeTimeOut
  # processTaskTime(long) void
   String id
   Long executeTimeOut
}
class ThreadPoolExecutorShutdownPlugin {
  + ThreadPoolExecutorShutdownPlugin(long) 
  + long awaitTerminationMillis
  - awaitTerminationIfNecessary(ExtensibleThreadPoolExecutor) void
  + beforeShutdown(ThreadPoolExecutor) void
  # cancelRemainingTask(Runnable) void
  + afterShutdown(ThreadPoolExecutor, List~Runnable~) void
   PluginRuntime pluginRuntime
   String id
   long awaitTerminationMillis
}
class ThreadPoolPlugin {
<<Interface>>
  + start() void
  + stop() void
   PluginRuntime pluginRuntime
   String id
}

AbstractTaskTimerPlugin  ..>  ExecuteAwarePlugin 
ExecuteAwarePlugin  -->  ThreadPoolPlugin 
RejectedAwarePlugin  -->  ThreadPoolPlugin 
ShutdownAwarePlugin  -->  ThreadPoolPlugin 
TaskTimeRecordPlugin  -->  Summary 
TaskAwarePlugin  -->  ThreadPoolPlugin 
TaskDecoratorPlugin  ..>  TaskAwarePlugin 
TaskRejectCountRecordPlugin  ..>  RejectedAwarePlugin 
TaskRejectNotifyAlarmPlugin  ..>  RejectedAwarePlugin 
TaskTimeRecordPlugin  -->  AbstractTaskTimerPlugin 
TaskTimeoutNotifyAlarmPlugin  -->  AbstractTaskTimerPlugin 
ThreadPoolExecutorShutdownPlugin  ..>  ShutdownAwarePlugin 

```


## 三、线程池配置信息是如何注册到服务端的?

```mermaid
graph TD
A[开始: postProcessAfterInitialization] --> B{Bean是DynamicThreadPoolExecutor<br/>或适配器匹配?}
B -->|是| C{检查DynamicThreadPool注解}
B -->|否| D{Bean是DynamicThreadPoolWrapper?}
D -->|是| E[调用registerAndSubscribe]
D -->|否| Z[返回bean]

    C -->|找到注解| F[提取DynamicThreadPoolExecutor实例]
    C -->|未找到注解| Z
    
    F --> G[创建DynamicThreadPoolWrapper]
    G --> H[调用fillPoolAndRegister]
    
    subgraph fillPoolAndRegister流程
        H --> I[构建查询参数]
        I --> J[HTTP GET查询服务端配置]
        J --> K{服务端存在配置?}
        K -->|是| L[更新本地线程池参数]
        K -->|否| M[构建DynamicThreadPoolRegisterParameter]
        L --> N[注册到GlobalThreadPoolManage]
        M --> O[调用GlobalThreadPoolManage.dynamicRegister]
        O --> P[触发DynamicThreadPoolConfigService注册]
        P --> Q[HTTP POST注册到服务端]
        Q --> R{注册成功?}
        R -->|是| S[缓存告警配置]
        R -->|否| T[记录错误日志]
        S --> N
    end
    
    H --> U[替换第三方线程池执行器]
    U --> V[订阅配置更新]
    V --> W[返回处理后的bean]
    
    E --> X[调用registerAndSubscribe]
    X --> Y[同fillPoolAndRegister流程]
    
    subgraph GlobalThreadPoolManage
        N --> AA[存入EXECUTOR_MAP]
        N --> AB[存入POOL_PARAMETER]
    end
    
    subgraph DynamicThreadPoolConfigService
        P --> BA[参数校验]
        BA --> BB[填充租户/项目信息]
        BB --> BC[发送HTTP请求]
        BC --> BD[处理告警配置]
    end
    
    style A fill:#f9f,stroke:#333
    style Z fill:#f9f,stroke:#333
    style H fill:#bbf,stroke:#666
    style N fill:#bfb,stroke:#666
    style P fill:#fbb,stroke:#666
    
```


## 四、目录



### 线程池相关
+ [为什么Java要设计一个线程池？](https://github.com/PansonPanson/moirai/blob/main/doc/001_Java%E4%B8%BA%E4%BB%80%E4%B9%88%E8%A6%81%E8%AE%BE%E8%AE%A1%E7%BA%BF%E7%A8%8B%E6%B1%A0%EF%BC%9F.md)
+ [万字长文之线程池源码深入分析](https://github.com/PansonPanson/moirai/blob/main/doc/002_%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%BA%90%E7%A0%81%E6%B7%B1%E5%85%A5%E5%88%86%E6%9E%90.md)
+ [线程池源码涉及到的位运算以及相应算法练习](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%BA%90%E7%A0%81%E6%B6%89%E5%8F%8A%E5%88%B0%E7%9A%84%E4%BD%8D%E8%BF%90%E7%AE%97%E4%BB%A5%E5%8F%8A%E7%9B%B8%E5%BA%94%E7%AE%97%E6%B3%95%E7%BB%83%E4%B9%A0.md) 
+ [线程池源码涉及到的链表结构以及相应算法练习](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%BA%90%E7%A0%81%E6%B6%89%E5%8F%8A%E5%88%B0%E7%9A%84%E9%93%BE%E8%A1%A8%E7%BB%93%E6%9E%84%E4%BB%A5%E5%8F%8A%E7%9B%B8%E5%BA%94%E7%AE%97%E6%B3%95%E7%BB%83%E4%B9%A0.md)
+ [LinkedBlockingQueue 原理与源码深度分析](https://github.com/PansonPanson/moirai/blob/main/doc/LinkedBlockingQueue%20%E5%8E%9F%E7%90%86%E4%B8%8E%E6%BA%90%E7%A0%81%E6%B7%B1%E5%BA%A6%E5%88%86%E6%9E%90.md)
+ [核心线程数为0时，线程池如何执行？](https://github.com/PansonPanson/moirai/blob/main/doc/%E6%A0%B8%E5%BF%83%E7%BA%BF%E7%A8%8B%E6%95%B0%E4%B8%BA%200%20%E6%97%B6%EF%BC%8C%E7%BA%BF%E7%A8%8B%E6%B1%A0%E5%A6%82%E6%9E%9C%E6%89%A7%E8%A1%8C%E4%BB%BB%E5%8A%A1%EF%BC%9F.md) 
+ [线程池中那些信息是可以被监控的？](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%B8%AD%E5%93%AA%E4%BA%9B%E4%BF%A1%E6%81%AF%E6%98%AF%E5%8F%AF%E4%BB%A5%E8%A2%AB%E7%9B%91%E6%8E%A7%E7%9A%84%EF%BC%9F.md)
+ [线程池异常后：销毁还是复用](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E5%BC%82%E5%B8%B8%E5%90%8E%E6%98%AF%E8%A2%AB%E9%94%80%E6%AF%81%E8%BF%98%E6%98%AF%E5%A4%8D%E7%94%A8%EF%BC%9F.md) 
+ [线程池的核心线程会被回收吗？](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%9A%84%E6%A0%B8%E5%BF%83%E7%BA%BF%E7%A8%8B%E4%BC%9A%E8%A2%AB%E5%9B%9E%E6%94%B6%E5%90%97%EF%BC%9F.md)
+ [线程池提交一个任务占多大内存？](https://github.com/PansonPanson/moirai/blob/main/doc/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%8F%90%E4%BA%A4%E4%B8%80%E4%B8%AA%E4%BB%BB%E5%8A%A1%E5%8D%A0%E5%A4%9A%E5%A4%A7%E5%86%85%E5%AD%98%EF%BC%9F.md) 
+ [如何保证服务器宕机后线程池不丢失数据?](https://github.com/PansonPanson/moirai/blob/main/doc/%E5%A6%82%E4%BD%95%E4%BF%9D%E8%AF%81%E6%9C%8D%E5%8A%A1%E5%99%A8%E5%AE%95%E6%9C%BA%E5%90%8E%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%B8%8D%E4%B8%A2%E5%A4%B1%E6%95%B0%E6%8D%AE%EF%BC%9F.md)
+ [如何设计一个线程池](https://github.com/PansonPanson/moirai/blob/main/doc/%E5%A6%82%E4%BD%95%E4%BF%9D%E8%AF%81%E6%9C%8D%E5%8A%A1%E5%99%A8%E5%AE%95%E6%9C%BA%E5%90%8E%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%B8%8D%E4%B8%A2%E5%A4%B1%E6%95%B0%E6%8D%AE%EF%BC%9F.md)
+ [FutureTask源码深入分析？.md](doc/FutureTask%E6%BA%90%E7%A0%81%E6%B7%B1%E5%85%A5%E5%88%86%E6%9E%90%EF%BC%9F.md)
+ [虚拟线程原理及性能分析.md](doc/%E8%99%9A%E6%8B%9F%E7%BA%BF%E7%A8%8B%E5%8E%9F%E7%90%86%E5%8F%8A%E6%80%A7%E8%83%BD%E5%88%86%E6%9E%90.md) 
+ [ForkJoinPool 源码深入分析？.md](doc/ForkJoinPool%20%E6%BA%90%E7%A0%81%E6%B7%B1%E5%85%A5%E5%88%86%E6%9E%90%EF%BC%9F.md)
+ Spring 的线程池设计 
  + Spring中Async注解底层异步线程池原理 
+ Dubbo 的线程池设计 
+ RocketMQ 的线程池设计 
+ 时间轮
+ [Tomcat 对 jdk 线程池进行了哪些修改？.md](doc/Tomcat%20%E5%AF%B9%20jdk%20%E7%BA%BF%E7%A8%8B%E6%B1%A0%E8%BF%9B%E8%A1%8C%E4%BA%86%E5%93%AA%E4%BA%9B%E4%BF%AE%E6%94%B9%EF%BC%9F.md)

### Spring 相关
+ [DisposableBean 作用与原理](https://github.com/PansonPanson/moirai/blob/main/doc/DisposableBean%E4%BD%9C%E7%94%A8%E4%B8%8E%E5%8E%9F%E7%90%86.md)
+ [如何自定义一个 SpringBoot starter ?.md](doc/%E5%A6%82%E4%BD%95%E8%87%AA%E5%AE%9A%E4%B9%89%E4%B8%80%E4%B8%AA%20SpringBoot%20starter%3F.md)
+ [SpringBoot starter 原理.md](doc/SpringBoot%20starter%20%E5%8E%9F%E7%90%86.md)

### 框架涉及到的设计模式
+ 




