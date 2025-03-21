package top.panson.config.service.biz.impl;

import top.panson.common.monitor.Message;
import top.panson.common.monitor.MessageWrapper;
import top.panson.common.monitor.RuntimeMessage;
import top.panson.common.toolkit.BeanUtil;
import top.panson.common.toolkit.DateUtil;
import top.panson.common.toolkit.GroupKey;
import top.panson.common.toolkit.MessageConvert;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import top.panson.config.config.ServerBootstrapProperties;
import top.panson.config.mapper.HisRunDataMapper;
import top.panson.config.model.HisRunDataInfo;
import top.panson.config.model.biz.monitor.MonitorActiveRespDTO;
import top.panson.config.model.biz.monitor.MonitorQueryReqDTO;
import top.panson.config.model.biz.monitor.MonitorRespDTO;
import top.panson.config.monitor.QueryMonitorExecuteChoose;
import top.panson.config.service.ConfigCacheService;
import top.panson.config.service.biz.HisRunDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static top.panson.common.toolkit.DateUtil.NORM_TIME_PATTERN;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/5/8
 * @方法描述：服务端处理客户端上报的线程池运行时信息的对象
 */
@Service
@AllArgsConstructor
public class HisRunDataServiceImpl extends ServiceImpl<HisRunDataMapper, HisRunDataInfo> implements HisRunDataService {


    private final ServerBootstrapProperties properties;

    //监控信息处理选择器
    private final QueryMonitorExecuteChoose queryMonitorExecuteChoose;

    //把线程池运行时信息存放到数据库的操作就是由这个执行器执行的，这个执行器定义在了CommonConfig类中
    private final ThreadPoolTaskExecutor monitorThreadPoolTaskExecutor;


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：查询对应的线程池历史运行信息的方法
     */
    @Override
    public List<MonitorRespDTO> query(MonitorQueryReqDTO reqDTO) {
        //既然是查询历史信息，肯定查询的是一个范围，然后展示在前端
        //所以这里就先得到当前时间
        LocalDateTime currentDate = LocalDateTime.now();
        //减去清理周期时间，这样一来，只查询dateTime-当前时间这个范围的数据即可，因为再往前数据应该都被清楚了
        LocalDateTime dateTime = currentDate.plusMinutes(-properties.getCleanHistoryDataPeriod());
        long startTime = DateUtil.getTime(dateTime);
        List<HisRunDataInfo> hisRunDataInfos = this.lambdaQuery()
                .eq(HisRunDataInfo::getTenantId, reqDTO.getTenantId())
                .eq(HisRunDataInfo::getItemId, reqDTO.getItemId())
                .eq(HisRunDataInfo::getTpId, reqDTO.getTpId())
                .eq(HisRunDataInfo::getInstanceId, reqDTO.getInstanceId())
                .between(HisRunDataInfo::getTimestamp, startTime, DateUtil.getTime(currentDate))
                .orderByAsc(HisRunDataInfo::getTimestamp)
                .list();
        return BeanUtil.convert(hisRunDataInfos, MonitorRespDTO.class);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：这个方法我就不添加注释了，看着很长，但是逻辑非常简单，而且前端界面把要获得数据写得很清楚，大家自己简单看看就行
     */
    @Override
    public MonitorActiveRespDTO queryInfoThreadPoolMonitor(MonitorQueryReqDTO reqDTO) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime dateTime = currentDate.plusMinutes(-properties.getCleanHistoryDataPeriod());
        long startTime = DateUtil.getTime(dateTime);
        List<HisRunDataInfo> hisRunDataInfos = this.lambdaQuery()
                .eq(HisRunDataInfo::getTenantId, reqDTO.getTenantId())
                .eq(HisRunDataInfo::getItemId, reqDTO.getItemId())
                .eq(HisRunDataInfo::getTpId, reqDTO.getTpId())
                .eq(HisRunDataInfo::getInstanceId, reqDTO.getInstanceId())
                .between(HisRunDataInfo::getTimestamp, startTime, DateUtil.getTime(currentDate))
                .orderByAsc(HisRunDataInfo::getTimestamp)
                .list();
        List<String> times = new ArrayList<>();
        List<Long> poolSizeList = new ArrayList<>();
        List<Long> activeSizeList = new ArrayList<>();
        List<Long> queueCapacityList = new ArrayList<>();
        List<Long> queueSizeList = new ArrayList<>();
        List<Long> completedTaskCountList = new ArrayList<>();
        List<Long> rejectCountList = new ArrayList<>();
        List<Long> queueRemainingCapacityList = new ArrayList<>();
        List<Long> currentLoadList = new ArrayList<>();
        long countTemp = 0L;
        AtomicBoolean firstFlag = new AtomicBoolean(Boolean.TRUE);
        for (HisRunDataInfo each : hisRunDataInfos) {
            String time = DateUtil.format(new Date(each.getTimestamp()), NORM_TIME_PATTERN);
            times.add(time);
            poolSizeList.add(each.getPoolSize());
            activeSizeList.add(each.getActiveSize());
            queueSizeList.add(each.getQueueSize());
            rejectCountList.add(each.getRejectCount());
            queueRemainingCapacityList.add(each.getQueueRemainingCapacity());
            currentLoadList.add(each.getCurrentLoad());
            queueCapacityList.add(each.getQueueCapacity());
            if (firstFlag.get()) {
                completedTaskCountList.add(0L);
                firstFlag.set(Boolean.FALSE);
                countTemp = each.getCompletedTaskCount();
                continue;
            }
            long completedTaskCount = each.getCompletedTaskCount();
            long countTask = completedTaskCount - countTemp;
            completedTaskCountList.add(countTask);
            countTemp = each.getCompletedTaskCount();
        }
        return new MonitorActiveRespDTO(times, poolSizeList, activeSizeList, queueSizeList, completedTaskCountList, rejectCountList, queueRemainingCapacityList, currentLoadList, queueCapacityList);
    }



    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：得到线程池最新的运行时信息，当然，这个最新也是相对的，肯定是相对于当前时间最新的运行信息
     */
    @Override
    public MonitorRespDTO queryThreadPoolLastTaskCount(MonitorQueryReqDTO reqDTO) {
        //得到当前时间
        LocalDateTime currentDate = LocalDateTime.now();
        //当前时间减去上一次清理的时间，在得到的这个dateTime时间中，线程池的历史信息肯定是都存在的，并且肯定有一条是最新的数据
        LocalDateTime dateTime = currentDate.plusMinutes(-properties.getCleanHistoryDataPeriod());
        //得到起始时间，比如说当前时间减去清理周期后得到的时间是9.30，那么这个起始时间就是9.30，查询数据库的时候，就只查询9.30-当前时间的线程池信息即可
        //然后将查到的信息对象按照创建时间降序排列，排在第一位的创建时间肯定是最大的，也就是最新的数据，把这条数据返回即可
        long startTime = DateUtil.getTime(dateTime);
        HisRunDataInfo hisRunDataInfo = this.lambdaQuery()
                .eq(HisRunDataInfo::getTenantId, reqDTO.getTenantId())
                .eq(HisRunDataInfo::getItemId, reqDTO.getItemId())
                .eq(HisRunDataInfo::getTpId, reqDTO.getTpId())
                .eq(HisRunDataInfo::getInstanceId, reqDTO.getInstanceId())
                //把得到的结果按照创建时间降序排列
                .orderByDesc(HisRunDataInfo::getTimestamp)
                //查询的时间范围
                .between(HisRunDataInfo::getTimestamp, startTime, DateUtil.getTime(currentDate))
                //只返回一条数据，肯定就是最新的那个数据对象
                .last("LIMIT 1")
                //返回单个结果
                .one();
        return BeanUtil.convert(hisRunDataInfo, MonitorRespDTO.class);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：是把客户端上报的线程池运行时信息存储到数据库中的方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Message message) {
        //得到客户端上报的所有线程池的运行时信息
        List<RuntimeMessage> runtimeMessages = message.getMessages();
        List<HisRunDataInfo> hisRunDataInfos = new ArrayList<>();
        //遍历线程池的运行信息
        runtimeMessages.forEach(each -> {
            //将遍历到的RuntimeMessage对象转换为HisRunDataInfo对象
            HisRunDataInfo hisRunDataInfo = BeanUtil.convert(each, HisRunDataInfo.class);
            //得到线程池的key字符串数组
            String[] parseKey = GroupKey.parseKey(each.getGroupKey());
            //判断服务端的缓存信息中是否缓存了线程池的信息
            boolean checkFlag = ConfigCacheService.checkTpId(each.getGroupKey(), parseKey[0], parseKey[3]);
            if (checkFlag) {
                //在这里给hisRunDataInfo对象设置线程池Id、项目Id、租户Id、服务实例Id
                hisRunDataInfo.setTpId(parseKey[0]);
                hisRunDataInfo.setItemId(parseKey[1]);
                hisRunDataInfo.setTenantId(parseKey[2]);
                hisRunDataInfo.setInstanceId(parseKey[3]);
                //把hisRunDataInfo对象添加到集合中
                hisRunDataInfos.add(hisRunDataInfo);
            }
        });
        //批量存储到数据库中
        this.saveBatch(hisRunDataInfos);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/5/8
     * @方法描述：解析客户端上报的线程池运行时数据的方法，在第六版本中，数据解析器会把线程池运行时数据存放到数据库中
     */
    @Override
    public Result<Void> dataCollect(MessageWrapper messageWrapper) {
        //创建一个异步任务
        Runnable task = () -> {
            //把客户端上报的信息转换成Message对象
            Message message = MessageConvert.convert(messageWrapper);
            //选择具体的解析器，然后让解析器处理这些数据
            queryMonitorExecuteChoose.chooseAndExecute(message);
        };
        try {
            //把任务提交给线程池执行
            monitorThreadPoolTaskExecutor.execute(task);
        } catch (Exception ex) {
            log.error("Monitoring data insertion database task overflow.", ex);
        }
        return Results.success();
    }
}
