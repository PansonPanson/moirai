package top.panson.common.api;

import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Client network service.
 */
public interface ClientNetworkService {

    /**
     * Get network ip port. return as an array 127.0.0.1,8080
     *
     * @param environment environment
     * @return network ip port
     */
    String[] getNetworkIpPort(ConfigurableEnvironment environment);
}
