package top.panson.core.enable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Marker configuration.
 */
@Configuration
public class MarkerConfiguration {

    @Bean
    public Marker dynamicThreadPoolMarkerBean() {
        return new Marker();
    }

    public class Marker {

    }
}