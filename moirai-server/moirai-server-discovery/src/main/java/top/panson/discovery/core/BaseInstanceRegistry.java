/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.discovery.core;

import top.panson.common.design.builder.ThreadFactoryBuilder;
import top.panson.common.design.observer.AbstractSubjectCenter;
import top.panson.common.model.InstanceInfo;
import top.panson.common.model.InstanceInfo.InstanceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static top.panson.common.constant.Constants.EVICTION_INTERVAL_TIMER_IN_MS;
import static top.panson.common.constant.Constants.SCHEDULED_THREAD_CORE_NUM;



/**
 * @方法描述：服务端的服务发现组件的核心类，这个类提供了注册和缓存客户端服务实例的方法和功能
 */
@Slf4j
@Service
public class BaseInstanceRegistry implements InstanceRegistry<InstanceInfo> {

    //注册表的大小
    private final int containerSize = 1024;

    //这个Map就是服务实例的注册表，所有注册到服务端的服务实例的信息都缓存在这个Map中
    //map的key就是服务实例的appName，而appName其实就是配置文件中spring.dynamic.thread-pool.item-id的值
    private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry = new ConcurrentHashMap<>(containerSize);

    //这个方法就是用来从注册表中获得所有服务实例对象
    @Override
    public List<Lease<InstanceInfo>> listInstance(String appName) {
        Map<String, Lease<InstanceInfo>> appNameLeaseMap = registry.get(appName);
        if (CollectionUtils.isEmpty(appNameLeaseMap)) {
            return new ArrayList<>();
        }
        List<Lease<InstanceInfo>> appNameLeaseList = new ArrayList<>();
        appNameLeaseMap.values().forEach(appNameLeaseList::add);
        return appNameLeaseList;
    }


    //注册服务实例到注册表的方法
    @Override
    public void register(InstanceInfo registrant) {
        //根据服务实例的AppName从注册表中获得对应的value
        Map<String, Lease<InstanceInfo>> registerMap = registry.get(registrant.getAppName());
        //如果value不存在，说明这个item-id下还没有任何服务实例注册到服务端
        if (registerMap == null) {
            //在这里创建一个map当作value
            ConcurrentHashMap<String, Lease<InstanceInfo>> registerNewMap = new ConcurrentHashMap<>(12);
            //以键值对的方式添加到注册表中
            //key就是服务实例的appName，appName其实就是配置文件中spring.dynamic.thread-pool.item-id的值，也就是项目ID
            //value就是一个空的map
            registerMap = registry.putIfAbsent(registrant.getAppName(), registerNewMap);
            if (registerMap == null) {
                //注意，这里其实就是把刚刚创建的map给registerMap赋值了
                //当调用putIfAbsent方法的时候，如果根本没有旧的value，就会返回null
                registerMap = registerNewMap;
            }
        }
        //接下来应该为这个服务实例创建对应的租约对象了，首先从刚才创建的value中获得对应的租约对象
        //这里我解释一下，注册表registry本身就是key-value的形式，key是项目ID，value是一个map，这个map的key是服务实例的Id，value就是封装了服务实例的租约对象
        //假如之前客户端已经注册过服务实例信息了，这里就会得到已经创建好的服务实例租约对象
        //如果这是客户端第一次注册服务实例信息，那这里肯定得到的就是空
        Lease<InstanceInfo> existingLease = registerMap.get(registrant.getInstanceId());
        //判断得到的服务实例租约对象是否为空
        if (existingLease != null && (existingLease.getHolder() != null)) {
            //走到这里就意味着存在旧的租约对象，也就意味着这不是客户端第一次向服务端注册服务实例信息了
            //这个时候就要判断一下，是旧的服务实例的最后修改时间新，还是当前正在注册的服务实例的时间新
            //得到旧服务实例的最后修改时间，这里也可以看到，租约对象封装了服务实例对象
            Long existingLastDirtyTimestamp = existingLease.getHolder().getLastDirtyTimestamp();
            //得到当前正在注册的服务实例对象的最后修改时间
            Long registrationLastDirtyTimestamp = registrant.getLastDirtyTimestamp();
            //比较二者时间大小，如果旧的时间比较大，说明旧的新，那就使用旧的服务实例信息创建租约对象
            if (existingLastDirtyTimestamp > registrationLastDirtyTimestamp) {
                registrant = existingLease.getHolder();
            }
        }
        //为服务实例创建租约对象
        Lease<InstanceInfo> lease = new Lease<>(registrant);
        //这里又判断了一下是否存在旧的租约对象，如果存在，那么新的这个租约对象仍然使用旧的租约对象的serviceUpTimestamp
        //serviceUpTimestamp代表服务实例第一次上线的时间
        if (existingLease != null) {
            lease.setServiceUpTimestamp(existingLease.getServiceUpTimestamp());
        }
        //把创建的租约对象添加到map中
        registerMap.put(registrant.getInstanceId(), lease);
        //服务实例对象的默认状态就是up，也就是上线状态
        if (InstanceStatus.UP.equals(registrant.getStatus())) {
            //所以在这里设置一下服务实例的上线时间
            //当然，假如不是第一次注册服务实例的话，在前面已经调用setServiceUpTimestamp方法设置了服务实例第一次上线时间
            //这里再调用这个serviceUp方法其实会在serviceUp方法内部查看一下，看看租约对象的serviceUpTimestamp是否有值了
            //如果有值就不会再重新赋值了
            lease.serviceUp();
        }
        //设置服务实例为已添加到注册表状态
        registrant.setActionType(InstanceInfo.ActionType.ADDED);
        //设置服务实例最新的更新时间，注意，这个时候服务实例已经被为其创建的租约对象持有了，而租约对象被map缓存了
        registrant.setLastUpdatedTimestamp(System.currentTimeMillis());
    }




    /**
     * @方法描述：为服务实例续约的方法
     */
    @Override
    public boolean renew(InstanceInfo.InstanceRenew instanceRenew) {
        //得到服务实例的项目Id
        String appName = instanceRenew.getAppName();
        //得到服务实例的Id
        String instanceId = instanceRenew.getInstanceId();
        //从注册表中获得封装了服务实例租约对象的map
        Map<String, Lease<InstanceInfo>> registryMap = registry.get(appName);
        Lease<InstanceInfo> leaseToRenew;
        //得到封装服务实例的租约对象，并且判断对象是否为null
        if (registryMap == null || (leaseToRenew = registryMap.get(instanceId)) == null) {
            //为空则返回falsue
            return false;
        }
        //如果租约对象不为空则执行续约操作
        leaseToRenew.renew();
        return true;
    }



    /**
     * @方法描述：从注册表中移除对应服务实例的方法
     */
    @Override
    public void remove(InstanceInfo info) {
        //得到服务实例的项目Id
        String appName = info.getAppName();
        //得到服务实例的Id
        String instanceId = info.getInstanceId();
        //从注册表中获得封装了服务实例租约对象的map
        Map<String, Lease<InstanceInfo>> leaseMap = registry.get(appName);
        if (CollectionUtils.isEmpty(leaseMap)) {
            log.warn("Failed to remove unhealthy node, no application found: {}", appName);
            return;
        }
        //移除对应的服务实例信息
        Lease<InstanceInfo> remove = leaseMap.remove(instanceId);
        if (remove == null) {
            log.warn("Failed to remove unhealthy node, no instance found: {}", instanceId);
            return;
        }
        log.info("Remove unhealthy node, node ID: {}", instanceId);
    }


    /**
     * @方法描述：判断服务实例是否过期，并且移除过期服务实例的方法
     */
    public void evict(long additionalLeaseMs) {
        //存放所有过期的租约对象的方法
        List<Lease<InstanceInfo>> expiredLeases = new ArrayList();
        //遍历注册表中的所有租约对象，其实遍历的就是服务实例对象
        for (Map.Entry<String, Map<String, Lease<InstanceInfo>>> groupEntry : registry.entrySet()) {
            Map<String, Lease<InstanceInfo>> leaseMap = groupEntry.getValue();
            if (leaseMap != null) {
                for (Map.Entry<String, Lease<InstanceInfo>> leaseEntry : leaseMap.entrySet()) {
                    Lease<InstanceInfo> lease = leaseEntry.getValue();
                    //判断租约对象是否过期
                    if (lease.isExpired(additionalLeaseMs) && lease.getHolder() != null) {
                        //过期则添加到过期集合中
                        expiredLeases.add(lease);
                    }
                }
            }
        }
        //接下来就是移除过期实例的操作
        for (Lease<InstanceInfo> expiredLease : expiredLeases) {
            String appName = expiredLease.getHolder().getAppName();
            String id = expiredLease.getHolder().getInstanceId();
            String identify = expiredLease.getHolder().getIdentify();
            //从注册表中移除过期服务实例
            internalCancel(appName, id, identify);
        }
    }



    //从注册表中移除过期实例的方法
    protected boolean internalCancel(String appName, String id, String identify) {
        Map<String, Lease<InstanceInfo>> registerMap = registry.get(appName);
        if (!CollectionUtils.isEmpty(registerMap)) {
            //从注册表中移除过期的服务实例对象
            registerMap.remove(id);
            //订阅中心发布服务实例移除事件
            AbstractSubjectCenter.notify(AbstractSubjectCenter.SubjectType.CLEAR_CONFIG_CACHE, () -> identify);
            log.info("Clean up unhealthy nodes. Node id: {}", id);
        }
        return true;
    }



    /**
     * @方法描述：这个内部类是一个定时任务，定时任务的逻辑就是服务端定期检查哪个服务实例租约过期了，然后移除过期的服务实例信息
     */
    public class EvictionTask extends TimerTask {

        //上一次执行定时任务的时间
        private final AtomicLong lastExecutionNanosRef = new AtomicLong(0L);


        //定时任务要执行的方法
        @Override
        public void run() {
            try {
                //得到服务实例过期时间
                long compensationTimeMs = getCompensationTimeMs();
                log.info("Running the evict task with compensationTime {} ms", compensationTimeMs);
                //判断服务实例是否过期，如果过期就从注册表中移除
                evict(compensationTimeMs);
            } catch (Throwable e) {
                log.error("Could not run the evict task", e);
            }
        }


        /**
         * @方法描述：得到服务实例过期时间的方法，这里我的注释可能有点模糊，让我再来为大家解释解释一下，判断一个服务实例是否过期的方式很简单
         * 因为客户端每一次续约都会在服务端更新服务实例的最新一次的更新时间，而目前这个方法得到的就是服务实例过期时间，比如说这个过期时间是5秒
         * 只要再每次定时任务中，让当前时间减去服务实例最后一次更新时间，只要超过5秒了，就意味着这个服务实例过期了，如果没有超过5秒
         * 说明服务实例一直在续约，那就没有过期
         */
        long getCompensationTimeMs() {
            //先获得当前时间
            long currNanos = getCurrentTimeNano();
            //获得服务实例上一次被检查的时间
            long lastNanos = lastExecutionNanosRef.getAndSet(currNanos);
            //假如这是定时任务第一次执行，那么这个时间肯定为0
            //这个时候就会用
            if (lastNanos == 0L) {
                return 0L;
            }
            //计算时间间隔
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(currNanos - lastNanos);
            //得到过期时间
            long compensationTime = elapsedMs - EVICTION_INTERVAL_TIMER_IN_MS;
            //返回过期时间
            return compensationTime <= 0L ? 0L : compensationTime;
        }

        //得到当前时间
        long getCurrentTimeNano() {
            return System.nanoTime();
        }
    }

    //创建一个定时任务执行器，这个执行器就是用来定期检查并移除过期服务实例的
    private final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(
                    SCHEDULED_THREAD_CORE_NUM,
                    new ThreadFactoryBuilder()
                            .prefix("registry-eviction")
                            .daemon(true)
                            .build());


    //移除过期服务实例任务的原子引用
    private final AtomicReference<EvictionTask> evictionTaskRef = new AtomicReference();

    //该方法会在RegistryConfiguration类中被调用
    public void postInit() {
        //设置移除过期服务实例的任务
        evictionTaskRef.set(new EvictionTask());
        //把任务提交给定时任务执行器
        scheduledExecutorService.scheduleWithFixedDelay(evictionTaskRef.get(),
                EVICTION_INTERVAL_TIMER_IN_MS, EVICTION_INTERVAL_TIMER_IN_MS, TimeUnit.MILLISECONDS);
    }
}
