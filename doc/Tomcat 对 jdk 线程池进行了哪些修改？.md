

Tomcat 对 JDK 标准线程池（`java.util.concurrent.ThreadPoolExecutor`）进行了定制化修改，主要目的是优化其在处理 HTTP 请求等高并发、短生命周期任务场景下的性能。以下是 Tomcat 线程池的核心修改点：

---

### 1. **任务提交策略的调整**
- **JDK 默认行为**：
  当任务提交时，若当前线程数小于核心线程数，则创建新线程；否则将任务放入队列。只有队列满时才会创建新线程直到达到最大线程数。
- **Tomcat 修改**：
  使用**无界队列**（`TaskQueue`）但调整了入队逻辑。在任务提交时：
    - 如果当前线程数 < 最大线程数，**优先创建新线程**而不是入队。
    - 如果线程数已达最大，才将任务放入队列。
      **效果**：更快响应突发请求，避免队列堆积导致延迟。

---

### 2. **定制化的任务队列（`TaskQueue`）**
Tomcat 实现了一个扩展的 `LinkedBlockingQueue`，名为 `TaskQueue`，关键逻辑如下：
   ```java
   public boolean offer(Runnable o) {
       // 如果线程池当前线程数 < 最大线程数，返回 false 触发创建新线程
       if (parent.getPoolSize() < parent.getMaximumPoolSize()) {
           return false;
       }
       return super.offer(o);
   }
   ```
- **行为**：当队列的 `offer` 方法被调用时，如果线程数未达上限，返回 `false`，让线程池创建新线程；否则任务入队。
- **目的**：优先利用线程资源处理任务，减少队列等待时间。

---

### 3. **线程回收策略**
- **JDK 默认**：核心线程空闲时默认不回收（除非设置 `allowCoreThreadTimeOut`）。
- **Tomcat 调整**：
    - 默认启用**核心线程超时回收**（即使未显式配置 `allowCoreThreadTimeOut`）。
    - 空闲线程（包括核心线程）在超过 `keepAliveTime` 后会被回收，减少资源占用。

---

### 4. **拒绝策略优化**
- **JDK 默认策略**：如 `AbortPolicy` 直接抛出异常。
- **Tomcat 行为**：在无法接受新任务时（队列满且线程数已达最大），会**重试入队**（最多尝试 6 次），若仍失败再抛出 `RejectedExecutionException`。
- **目的**：尽量避免因瞬时高负载导致的任务拒绝，提升容错能力。

---

### 5. **线程命名与监控**
- **线程命名**：Tomcat 线程池中的线程名称包含 `http-nio-{port}-exec-{num}` 等标识，便于问题排查。
- **统计扩展**：通过 JMX 暴露更多运行时指标（如活动线程数、队列大小），方便监控。

---

### 6. **与 Connector 的协同**
Tomcat 的线程池与连接器（如 `NioEndpoint`）深度集成，例如：
- **最大线程数**：由 Connector 的 `maxThreads` 参数配置（默认为 200）。
- **任务队列长度**：由 `acceptCount` 参数控制（默认 100），但实际行为受 `TaskQueue` 逻辑影响。

---

### 总结
Tomcat 通过上述修改，实现了在高并发场景下更高效的请求处理：**优先扩展线程数到最大，减少队列等待**，同时动态回收空闲线程以节省资源。这些优化使其更适合处理 HTTP 请求这类**短时、高频**的任务，避免传统线程池可能导致的请求延迟问题。在配置时，需注意参数如 `maxThreads` 和 `acceptCount` 的调优，以适应实际负载。