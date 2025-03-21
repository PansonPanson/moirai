package top.panson.springboot.start.security;

import top.panson.common.constant.Constants;
import top.panson.common.model.TokenInfo;
import top.panson.common.toolkit.JSONUtil;
import top.panson.common.toolkit.StringUtil;
import top.panson.common.toolkit.http.HttpUtil;
import top.panson.common.web.base.Result;
import top.panson.springboot.start.config.BootstrapProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/26
 * @方法描述：安全代理类，这个类就是用来访问服务端，然后从服务端获取token的
 */
@Slf4j
public class SecurityProxy {

    //要访问的客户端的url
    private static final String APPLY_TOKEN_URL = Constants.BASE_PATH + "/auth/users/apply/token";

    //用户名
    private final String username;

    //密码
    private final String password;

    //得到的token
    private String accessToken;

    //客户端获得的token的有效期
    private long tokenTtl;

    //token最新的刷新时间
    private long lastRefreshTime;

    //token的刷新窗口时间
    private long tokenRefreshWindow;

    public SecurityProxy(BootstrapProperties properties) {
        username = properties.getUsername();
        password = properties.getPassword();
    }

    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/4/26
     * @方法描述：这个方法会被提交给ServerHttpAgent类中的定时任务执行器，当作一个定时任务来执行
     */
    public boolean applyToken(List<String> servers) {
        try {//判断token是否还在有效期内
            if ((System.currentTimeMillis() - lastRefreshTime) < TimeUnit.SECONDS.toMillis(tokenTtl - tokenRefreshWindow)) {
                return true;
            }//如果超过有效期了，就再次访问服务端获得最新token
            for (String server : servers) {
                if (applyToken(server)) {
                    //更新token最新的刷新时间
                    lastRefreshTime = System.currentTimeMillis();
                    //只要第一次访问服务端成功了就可以直接退出循环
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/4/26
     * @方法描述：这个方法就是用来访问服务端，并且获得token的方法
     */
    public boolean applyToken(String server) {
        //对用户名和密码判空
        if (StringUtil.isAllNotEmpty(username, password)) {
            //得到完整的要访问的服务端的地址
            String url = server + APPLY_TOKEN_URL;
            Map<String, String> bodyMap = new HashMap(2);
            //把用户名和密码封装到map中
            bodyMap.put("userName", username);
            bodyMap.put("password", password);
            try {//访问服务端，获得结果
                Result result = HttpUtil.post(url, bodyMap, Result.class);
                if (!result.isSuccess()) {
                    log.error("Error getting access token. message: {}", result.getMessage());
                    return false;
                }
                String tokenJsonStr = JSONUtil.toJSONString(result.getData());
                //得到token信息对象
                TokenInfo tokenInfo = JSONUtil.parseObject(tokenJsonStr, TokenInfo.class);
                //得到token
                accessToken = tokenInfo.getAccessToken();
                //得到token的有效期
                tokenTtl = tokenInfo.getTokenTtl();
                //使用token有效期十分之一的时间作为token刷新窗口期
                tokenRefreshWindow = tokenTtl / 10;
            } catch (Throwable ex) {
                log.error("Failed to apply for token. message: {}", ex.getMessage());
                return false;
            }
        }
        return true;
    }

    //得到token的方法
    public String getAccessToken() {
        return accessToken;
    }
}
