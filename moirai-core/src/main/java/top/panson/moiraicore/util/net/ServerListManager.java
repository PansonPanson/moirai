package top.panson.moiraicore.util.net;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.panson.moiraicore.BootstrapProperties;
import top.panson.moiraicore.util.StringUtil;

import java.util.*;



@Slf4j
public class ServerListManager {

    //访问服务端使用的协议
    private static final String HTTPS = "https://";

    private static final String HTTP = "http://";

    //http服务端地址
    private String serverAddrsStr;

    //netty提供的服务端的端口号
    private String nettyServerPort;

    //封装服务端地址的集合，用户可以在配置文件中定义多个服务端地址
    //这些地址会被封装到一个集合中
    @Getter
    volatile List<String> serverUrls = new ArrayList();

    //当前访问的服务端的地址
    private volatile String currentServerAddr;

    //服务端地址列表的迭代器对象
    private Iterator<String> iterator;

    //配置信息对象
    private final BootstrapProperties properties;

    //构造方法
    public ServerListManager(BootstrapProperties dynamicThreadPoolProperties) {
        this.properties = dynamicThreadPoolProperties;
        //得到用户在配置文件中定义的服务器地址信息
        serverAddrsStr = properties.getServerAddr();
        //得到用户在配置文件中定义的netty构建的服务器的端口号
        nettyServerPort = properties.getNettyServerPort();
        if (!StringUtils.isEmpty(serverAddrsStr)) {
            //
            List<String> serverAddrList = new ArrayList();
            //得到服务地址列表信息
            String[] serverAddrListArr = this.serverAddrsStr.split(",");
            for (String serverAddr : serverAddrListArr) {
                //判断使用的协议，spring.dynamic.thread-pool.server-addr=http://localhost:6691，这个就是用户定义在配置文件中的服务端的地址
                //这里就是判断用户在配置文件中定义的服务地址是不是既没有使用http和https开头
                boolean whetherJoint = StringUtil.isNotBlank(serverAddr)
                        && !serverAddr.startsWith(HTTPS) && !serverAddr.startsWith(HTTP);
                if (whetherJoint) {
                    //如果都没有使用，就默认使用http协议
                    serverAddr = HTTP + serverAddr;
                }//给当前使用的服务地址赋值
                currentServerAddr = serverAddr;
                //把服务地址添加到集合中
                serverAddrList.add(serverAddr);
            }//给serverUrls赋值
            this.serverUrls = serverAddrList;
        }
    }

    //得到当前使用的服务地址
    public String getCurrentServerAddr() {
        if (StringUtils.isEmpty(currentServerAddr)) {
            iterator = iterator();
            currentServerAddr = iterator.next();
        }
        return currentServerAddr;
    }

    public String getNettyServerPort() {
        return nettyServerPort;
    }

    //得到服务地址列表迭代器对象
    Iterator<String> iterator() {
        return new ServerAddressIterator(serverUrls);
    }

    //这个服务地址列表的迭代器，逻辑非常简单，就不添加注释了
    private static class ServerAddressIterator implements Iterator<String> {

        final List<RandomizedServerAddress> sorted;

        final Iterator<RandomizedServerAddress> iter;

        public ServerAddressIterator(List<String> source) {
            sorted = new ArrayList();
            for (String address : source) {
                sorted.add(new RandomizedServerAddress(address));
            }
            Collections.sort(sorted);
            iter = sorted.iterator();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }

        static class RandomizedServerAddress implements Comparable<RandomizedServerAddress> {

            static Random random = new Random();

            String serverIp;

            int priority = 0;

            int seed;

            public RandomizedServerAddress(String ip) {
                try {
                    this.serverIp = ip;
                    /*
                     * change random scope from 32 to Integer.MAX_VALUE to fix load balance issue
                     */
                    this.seed = random.nextInt(Integer.MAX_VALUE);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public int compareTo(RandomizedServerAddress other) {
                if (this.priority != other.priority) {
                    return other.priority - this.priority;
                } else {
                    return other.seed - this.seed;
                }
            }
        }
    }
}
