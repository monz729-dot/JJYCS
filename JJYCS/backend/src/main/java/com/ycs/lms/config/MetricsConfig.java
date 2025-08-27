package com.ycs.lms.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "ycs-lms", "environment", "production")
            .meterFilter(MeterFilter.deny(id -> {
                String name = id.getName();
                // 불필요한 메트릭 필터링
                return name.startsWith("jvm.threads.daemon") ||
                       name.startsWith("process.files") ||
                       name.startsWith("system.load.average.1m");
            }))
            .meterFilter(MeterFilter.accept());
    }
}