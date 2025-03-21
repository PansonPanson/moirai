package top.panson.springboot.start.remote;

/**
 * Server health check.
 */
public interface ServerHealthCheck {

    /**
     * Is health status.
     *
     * @return
     */
    boolean isHealthStatus();

    /**
     * Set health status.
     *
     * @param healthStatus
     */
    void setHealthStatus(boolean healthStatus);
}