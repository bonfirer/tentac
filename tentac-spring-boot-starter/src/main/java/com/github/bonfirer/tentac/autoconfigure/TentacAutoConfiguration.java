package com.github.bonfirer.tentac.autoconfigure;

import com.github.bonfirer.tentac.core.MarkdownGenerator;
import com.github.bonfirer.tentac.filter.IpAccessFilter;
import com.github.bonfirer.tentac.probe.LlmProbeGenerator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

/**
 * Auto-configuration for Tentac.
 * Registers all necessary beans when tentac.enabled=true.
 */
@AutoConfiguration
@EnableConfigurationProperties(TentacProperties.class)
@ConditionalOnProperty(prefix = "tentac", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TentacAutoConfiguration {

    @Bean
    public MarkdownGenerator markdownGenerator(TentacProperties properties) {
        MarkdownGenerator generator = new MarkdownGenerator();
        generator.setTitle(properties.getTitle());
        generator.setDescription(properties.getDescription());
        generator.setVersion(properties.getVersion());
        generator.setBasePackage(properties.getBasePackage());
        return generator;
    }

    @Bean
    public LlmProbeGenerator llmProbeGenerator() {
        return new LlmProbeGenerator();
    }

    @Bean
    public TentacEndpoint tentacEndpoint(TentacProperties properties,
                                                  MarkdownGenerator generator,
                                                  LlmProbeGenerator probeGenerator,
                                                  org.springframework.context.ApplicationContext applicationContext) {
        return new TentacEndpoint(properties, generator, probeGenerator, applicationContext);
    }

    /**
     * Register IP access filter for Tentac endpoints.
     * Only registered when whitelist or blacklist is configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "tentac", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<IpAccessFilter> ipAccessFilter(TentacProperties properties) {
        FilterRegistrationBean<IpAccessFilter> registration = new FilterRegistrationBean<>();

        List<String> paths = Arrays.asList(
                properties.getMarkdownPath(),
                properties.getLlmProbePath(),
                properties.getLlmProbePath() + "/json"
        );

        IpAccessFilter filter = new IpAccessFilter(
                paths,
                properties.getIpWhitelist(),
                properties.getIpBlacklist()
        );
        registration.setFilter(filter);
        registration.setOrder(1);

        // Apply to tentac documentation paths
        registration.addUrlPatterns(
                properties.getMarkdownPath(),
                properties.getMarkdownPath() + "/*",
                properties.getLlmProbePath(),
                properties.getLlmProbePath() + "/*"
        );

        return registration;
    }
}
