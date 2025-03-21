package top.panson.springboot.start.toolkit;

import top.panson.core.toolkit.inet.InetUtils;
import lombok.SneakyThrows;
import org.springframework.core.env.PropertyResolver;

/**
 * Cloud common id util.
 */
public class CloudCommonIdUtil {

    /**
     * Splice target information separator
     */
    private static final String SEPARATOR = ":";

    /**
     * Get client ip port.
     *
     * @param resolver  resolver
     * @param inetUtils inet utils
     * @return ip and port
     */
    public static String getClientIpPort(PropertyResolver resolver, InetUtils inetUtils) {
        String hostname = inetUtils.findFirstNonLoopBackHostInfo().getIpAddress();
        String port = resolver.getProperty("server.port", "8080");
        return combineParts(hostname, SEPARATOR, port);
    }

    /**
     * Get default instance id.
     *
     * @param resolver  resolver
     * @param inetUtils inet utils
     * @return default instance id
     */
    @SneakyThrows
    public static String getDefaultInstanceId(PropertyResolver resolver, InetUtils inetUtils) {
        String namePart = getIpApplicationName(resolver, inetUtils);
        String indexPart = resolver.getProperty("spring.application.instance_id", resolver.getProperty("server.port"));
        return combineParts(namePart, SEPARATOR, indexPart);
    }

    /**
     * Get ip application name.
     *
     * @param resolver  resolver
     * @param inetUtils inet utils
     * @return ip application name
     */
    @SneakyThrows
    public static String getIpApplicationName(PropertyResolver resolver, InetUtils inetUtils) {
        String hostname = inetUtils.findFirstNonLoopBackHostInfo().getIpAddress();
        String appName = resolver.getProperty("spring.application.name");
        return combineParts(hostname, SEPARATOR, appName);
    }

    /**
     * Combine parts.
     *
     * @param firstPart  first part
     * @param separator  separator
     * @param secondPart second part
     * @return combined
     */
    public static String combineParts(String firstPart, String separator, String secondPart) {
        String combined = null;
        if (firstPart != null && secondPart != null) {
            combined = firstPart + separator + secondPart;
        } else if (firstPart != null) {
            combined = firstPart;
        } else if (secondPart != null) {
            combined = secondPart;
        }
        return combined;
    }
}

