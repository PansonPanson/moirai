package top.panson.config.service.biz.impl;

import top.panson.common.config.ApplicationContextHolder;
import top.panson.common.enums.DelEnum;
import top.panson.common.model.notify.DynamicThreadPoolRegisterServerNotifyParameter;
import top.panson.common.model.register.DynamicThreadPoolRegisterParameter;
import top.panson.common.model.register.DynamicThreadPoolRegisterWrapper;
import top.panson.common.toolkit.*;
import top.panson.common.web.exception.ServiceException;
import top.panson.config.event.LocalDataChangeEvent;
import top.panson.config.mapper.ConfigInfoMapper;
import top.panson.config.mapper.ConfigInstanceMapper;
import top.panson.config.model.ConfigAllInfo;
import top.panson.config.model.ConfigInfoBase;
import top.panson.config.model.ConfigInstanceInfo;
import top.panson.config.model.LogRecordInfo;
import top.panson.config.model.biz.notify.NotifyReqDTO;
import top.panson.config.service.ConfigCacheService;
import top.panson.config.service.ConfigChangePublisher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.panson.config.service.biz.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static top.panson.config.service.ConfigCacheService.getContent;

/**
 *
 * @Description:从数据库中查询线程池配置信息的类
 */
@Slf4j
@Service
@AllArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    //这个mapper查询的就是线程池的配置信息
    private final ConfigInfoMapper configInfoMapper;

    //这个mapper查询的就是线程池实例的配置信息
    private final ConfigInstanceMapper configInstanceMapper;

    //这个是用来记录线程池配置变更操作日志的类，会把日志记录到数据库中
    private final OperationLogService operationLogService;

    private final NotifyService notifyService;


    @Override
    public ConfigAllInfo findConfigAllInfo(String tpId, String itemId, String tenantId) {
        LambdaQueryWrapper<ConfigAllInfo> wrapper = Wrappers.lambdaQuery(ConfigAllInfo.class)
                .eq(StringUtil.isNotBlank(tpId), ConfigAllInfo::getTpId, tpId)
                .eq(StringUtil.isNotBlank(itemId), ConfigAllInfo::getItemId, itemId)
                .eq(StringUtil.isNotBlank(tenantId), ConfigAllInfo::getTenantId, tenantId);
        ConfigAllInfo configAllInfo = configInfoMapper.selectOne(wrapper);
        return configAllInfo;
    }


    //查询线程池配置信息的方法
    @Override
    public ConfigAllInfo findConfigRecentInfo(String... params) {
        //定义一个接收返回结果的变量
        ConfigAllInfo resultConfig;
        //定义一个ConfigAllInfo对象
        ConfigAllInfo configInstance = null;
        //下面就是具体查询的逻辑
        String instanceId = params[3];
        if (StringUtil.isNotBlank(instanceId)) {
            LambdaQueryWrapper<ConfigInstanceInfo> instanceQueryWrapper = Wrappers.lambdaQuery(ConfigInstanceInfo.class)
                    .eq(ConfigInstanceInfo::getTpId, params[0])
                    .eq(ConfigInstanceInfo::getItemId, params[1])
                    .eq(ConfigInstanceInfo::getTenantId, params[2])
                    .eq(ConfigInstanceInfo::getInstanceId, params[3])
                    .orderByDesc(ConfigInstanceInfo::getGmtCreate)
                    .last("LIMIT 1");
            //得到一个线程池实例对象，这个实例对象中封装着线程池的配置信息
            //注意，这里需要先查询一下ConfigInstanceInfo对象，看看数据库中是否有对应的数据
            ConfigInstanceInfo instanceInfo = configInstanceMapper.selectOne(instanceQueryWrapper);
            //判断查询结果是否为空
            if (instanceInfo != null) {
                //不为空则把信息全都交给configInstance对象
                String content = instanceInfo.getContent();
                configInstance = JSONUtil.parseObject(content, ConfigAllInfo.class);
                configInstance.setContent(content);
                configInstance.setGmtCreate(instanceInfo.getGmtCreate());
                //在这里设置了MD5的值，这里设置MD5的值，我要解释一下，这个MD5的值是根据线程池的核心配置信息来定义的
                //所以线程池的核心配置信息一旦改变，这个MD5的值也是要再次更新的
                configInstance.setMd5(Md5Util.getTpContentMd5(configInstance));
            }
        }
        //之后再使用configInfoMapper查询ConfigAllInfo的信息，因为用户也可以在前端页面直接添加线程池配置信息对象到数据库中
        ConfigAllInfo configAllInfo = findConfigAllInfo(params[0], params[1], params[2]);
        if (configAllInfo == null && configInstance == null) {
            throw new ServiceException("Thread pool configuration is not defined");
        } else if (configAllInfo != null && configInstance == null) {
            resultConfig = configAllInfo;
        } else if (configAllInfo == null && configInstance != null) {
            resultConfig = configInstance;
        } else {
            //如果查询出来的两个对象都有值，那就比较一下，谁是最新的，就返回哪个配置
            if (configAllInfo.getGmtModified().before(configInstance.getGmtCreate())) {
                resultConfig = configInstance;
            } else {
                resultConfig = configAllInfo;
            }
        }
        return resultConfig;
    }


    //插入或者更新ConfigAllInfo对象的方法，ConfigAllInfo对象就封装着线程池的核心配置信息
    @Override
    public void insertOrUpdate(String identify, boolean isChangeNotice, ConfigAllInfo configInfo) {
        verification(identify);
        LambdaQueryWrapper<ConfigAllInfo> queryWrapper = Wrappers.lambdaQuery(ConfigAllInfo.class)
                .eq(ConfigAllInfo::getTenantId, configInfo.getTenantId())
                .eq(ConfigInfoBase::getItemId, configInfo.getItemId())
                .eq(ConfigInfoBase::getTpId, configInfo.getTpId());
        ConfigAllInfo existConfig = configInfoMapper.selectOne(queryWrapper);
        ConfigServiceImpl configService = ApplicationContextHolder.getBean(this.getClass());
        configInfo.setCapacity(getQueueCapacityByType(configInfo));
        ConditionUtil.condition(
                existConfig == null,
                () -> configService.addConfigInfo(configInfo),
                () -> configService.updateConfigInfo(identify, isChangeNotice, configInfo));
        if (isChangeNotice) {
            //通知客户端配置变更的操作
            ConfigChangePublisher.notifyConfigChange(new LocalDataChangeEvent(identify, ContentUtil.getGroupKey(configInfo)));
        }
    }


    /**
     *
     * @Description:注册线程池信息到数据库的方法，DynamicThreadPoolRegisterWrapper就是从客户端传过来的对象
     */
    @Override
    public void register(DynamicThreadPoolRegisterWrapper registerWrapper) {
        //解析DynamicThreadPoolRegisterWrapper对象，得到一个ConfigAllInfo对象
        ConfigAllInfo configAllInfo = parseConfigAllInfo(registerWrapper);
        //从ApplicationContext得到TenantService对象
        TenantService tenantService = ApplicationContextHolder.getBean(TenantService.class);
        //从ApplicationContext得到ItemService对象
        ItemService itemService = ApplicationContextHolder.getBean(ItemService.class);
        //下面就是断言租户和项目Id非空的操作
        Assert.isTrue(tenantService.getTenantByTenantId(registerWrapper.getTenantId()) != null, "Tenant does not exist");
        Assert.isTrue(itemService.queryItemById(registerWrapper.getTenantId(), registerWrapper.getItemId()) != null, "Item does not exist");
        //从数据库中查询是否已经存在对应的ConfigAllInfo对象
        ConfigAllInfo existConfigAllInfo = findConfigAllInfo(configAllInfo.getTpId(), registerWrapper.getItemId(), registerWrapper.getTenantId());
        //如果不存在则直接将新的ConfigAllInfo对象插入到数据库中
        if (existConfigAllInfo == null) {
            addConfigInfo(configAllInfo);
        } else if (registerWrapper.getUpdateIfExists()) {
            //如果ConfigAllInfo对象已经存在那就直接更新对象到数据库中
            //作者这是怎么了？为什么要从容器中获得当前类的对象呢？现在的操作本身就在ConfigServiceImpl对象中执行呀
            //直接点用当前对象的updateConfigInfo方法不就完了？？？
            //如果这行代码有别的用意，大家可以告诉我
            ConfigServiceImpl configService = ApplicationContextHolder.getBean(this.getClass());
            configService.updateConfigInfo(null, false, configAllInfo);
        }//这里是把线程池对应的告警通知的配置信息存放到数据库中，如果用户没有在web界面设置，并且用户也没有在客户端程序中配置
        //那么这里的信息就是空，就不会把告警信息写到数据库中
        DynamicThreadPoolRegisterServerNotifyParameter serverNotifyParameter = registerWrapper.getServerNotify();
        if (serverNotifyParameter != null) {
            ArrayList<String> notifyTypes = new ArrayList<>();
            Collections.addAll(notifyTypes, "CONFIG", "ALARM");
            notifyTypes.forEach(each -> {
                NotifyReqDTO notifyReqDTO = new NotifyReqDTO();
                notifyReqDTO.setType(each)
                        .setEnable(1)
                        .setTenantId(registerWrapper.getTenantId())
                        .setItemId(registerWrapper.getItemId())
                        .setTpId(configAllInfo.getTpId())
                        .setPlatform(serverNotifyParameter.getPlatform())
                        .setReceives(serverNotifyParameter.getReceives())
                        .setSecretKey(serverNotifyParameter.getAccessToken());
                if (Objects.equals(each, "ALARM")) {
                    notifyReqDTO.setInterval(serverNotifyParameter.getInterval());
                    notifyReqDTO.setAlarmType(true);
                } else {
                    notifyReqDTO.setConfigType(true);
                }
                notifyService.saveOrUpdate(registerWrapper.getNotifyUpdateIfExists(), notifyReqDTO);
            });
        }
    }

    //解析DynamicThreadPoolRegisterWrapper对象为ConfigAllInfo对象的方法
    private ConfigAllInfo parseConfigAllInfo(DynamicThreadPoolRegisterWrapper registerWrapper) {
        DynamicThreadPoolRegisterParameter registerParameter = registerWrapper.getParameter();
        ConfigAllInfo configAllInfo = JSONUtil.parseObject(JSONUtil.toJSONString(registerParameter), ConfigAllInfo.class);
        configAllInfo.setTenantId(registerWrapper.getTenantId());
        configAllInfo.setItemId(registerWrapper.getItemId());
        configAllInfo.setTpId(registerParameter.getThreadPoolId());
        configAllInfo.setLivenessAlarm(registerParameter.getActiveAlarm());
        configAllInfo.setQueueType(registerParameter.getBlockingQueueType().getType());
        configAllInfo.setRejectedType(registerParameter.getRejectedPolicyType().getType());
        configAllInfo.setAllowCoreThreadTimeOut(registerParameter.getAllowCoreThreadTimeOut());
        return configAllInfo;
    }

    private void verification(String identify) {
        if (StringUtil.isNotBlank(identify)) {
            Map content = getContent(identify);
            Assert.isTrue(CollectionUtil.isNotEmpty(content), "线程池实例不存在, 请尝试页面刷新");
        }
    }


    /**
     *
     * @Description:添加ConfigAllInfo到数据库的方法
     */
    public Long addConfigInfo(ConfigAllInfo config) {
        config.setContent(ContentUtil.getPoolContent(config));
        config.setMd5(Md5Util.getTpContentMd5(config));
        try {
            // Currently it is a single application, and it supports switching distributed locks during cluster deployment in the future.
            synchronized (ConfigService.class) {
                ConfigAllInfo configAllInfo = configInfoMapper.selectOne(
                        Wrappers.lambdaQuery(ConfigAllInfo.class)
                                .eq(ConfigAllInfo::getTpId, config.getTpId())
                                .eq(ConfigAllInfo::getDelFlag, DelEnum.NORMAL.getIntCode()));
                Assert.isNull(configAllInfo, "线程池配置已存在");
                if (SqlHelper.retBool(configInfoMapper.insert(config))) {
                    return config.getId();
                }
            }
        } catch (Exception ex) {
            log.error("[db-error] message: {}", ex.getMessage(), ex);
            throw ex;
        }
        return null;
    }

    /**
     *
     * @Description:更新数据库中ConfigAllInfo的方法
     */
    public void updateConfigInfo(String identify, boolean isChangeNotice, ConfigAllInfo config) {
        LambdaUpdateWrapper<ConfigAllInfo> wrapper = Wrappers.lambdaUpdate(ConfigAllInfo.class)
                .eq(ConfigAllInfo::getTpId, config.getTpId())
                .eq(ConfigAllInfo::getItemId, config.getItemId())
                .eq(ConfigAllInfo::getTenantId, config.getTenantId());
        config.setGmtCreate(null);
        config.setContent(ContentUtil.getPoolContent(config));
        config.setMd5(Md5Util.getTpContentMd5(config));
        //这里记录一下线程池配置变更操作日志
        recordOperationLog(config);
        try {//这里的这个identify就是线程池实例对象的instanceId，这个instanceId马上就会在接下来的版本代码中讲解
            if (StringUtil.isNotBlank(identify)) {
                //如果instanceId不为空，说明更新线程池配置信息的时候，也要把线程池实例的信息也更新一下
                ConfigInstanceInfo instanceInfo = BeanUtil.convert(config, ConfigInstanceInfo.class);
                instanceInfo.setInstanceId(identify);
                //在这里更新
                configInstanceMapper.update(instanceInfo, Wrappers.lambdaQuery(ConfigInstanceInfo.class).eq(ConfigInstanceInfo::getInstanceId, identify));
            } else if (StringUtil.isEmpty(identify) && isChangeNotice) {
                //走到这里就意味着identify为空，接下来就是向数据库中添加一个ConfigInstanceInfo对象的操作
                List<String> identifyList = ConfigCacheService.getIdentifyList(config.getTenantId(), config.getItemId(), config.getTpId());
                if (CollectionUtil.isNotEmpty(identifyList)) {
                    for (String each : identifyList) {
                        ConfigInstanceInfo instanceInfo = BeanUtil.convert(config, ConfigInstanceInfo.class);
                        instanceInfo.setInstanceId(each);
                        configInstanceMapper.insert(instanceInfo);
                    }
                }
            }
            //更新ConfigAllInfo对象的数据库信息
            configInfoMapper.update(config, wrapper);
        } catch (Exception ex) {
            log.error("[db-error] message: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    //记录数据库配置变更操作日志的方法，这里就是把操作日志存放到数据库中了
    private void recordOperationLog(ConfigAllInfo requestParam) {
        LogRecordInfo logRecordInfo = LogRecordInfo.builder()
                .bizKey(requestParam.getItemId() + "_" + requestParam.getTpId())
                .bizNo(requestParam.getItemId() + "_" + requestParam.getTpId())
                .operator(Optional.ofNullable(UserContext.getUserName()).orElse("-"))
                .action(String.format("核心线程: %d, 最大线程: %d, 队列类型: %d, 队列容量: %d, 拒绝策略: %d", requestParam.getCoreSize(), requestParam.getMaxSize(), requestParam.getQueueType(),
                        requestParam.getCapacity(), requestParam.getRejectedType()))
                .category("THREAD_POOL_UPDATE")
                .detail(JSONUtil.toJSONString(requestParam))
                .createTime(new Date())
                .build();
        operationLogService.record(logRecordInfo);
    }


    //根据ConfigAllInfo中的队列类型获取队列容量的方法
    private Integer getQueueCapacityByType(ConfigAllInfo config) {
        int queueCapacity;
        switch (config.getQueueType()) {
            case 5:
                queueCapacity = Integer.MAX_VALUE;
                break;
            default:
                queueCapacity = config.getCapacity();
                break;
        }
        List<Integer> queueTypes = Stream.of(1, 2, 3, 6, 9).collect(Collectors.toList());
        boolean setDefaultFlag = queueTypes.contains(config.getQueueType()) && (config.getCapacity() == null || Objects.equals(config.getCapacity(), 0));
        if (setDefaultFlag) {
            queueCapacity = 1024;
        }
        return queueCapacity;
    }
}