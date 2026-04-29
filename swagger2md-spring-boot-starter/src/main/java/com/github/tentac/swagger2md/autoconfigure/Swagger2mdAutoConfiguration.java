package com.github.tentac.swagger2md.autoconfigure;

import com.github.tentac.swagger2md.core.MarkdownGenerator;
import com.github.tentac.swagger2md.filter.IpAccessFilter;
import com.github.tentac.swagger2md.probe.LlmProbeGenerator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

/**
 * Auto-configuration for Swagger2md.
 * Registers all necessary beans when swagger2md.enabled=true.
 */
@AutoConfiguration
@EnableConfigurationProperties(Swagger2mdProperties.class)
@ConditionalOnProperty(prefix = "swagger2md", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Swagger2mdAutoConfiguration {

    @Bean
    public MarkdownGenerator markdownGenerator(Swagger2mdProperties properties) {
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
    public Swagger2mdEndpoint swagger2mdEndpoint(Swagger2mdProperties properties,
                                                  MarkdownGenerator generator,
                                                  LlmProbeGenerator probeGenerator,
                                                  org.springframework.context.ApplicationContext applicationContext) {
        return new Swagger2mdEndpoint(properties, generator, probeGenerator, applicationContext);
    }

    /**
     * Register IP access filter for Swagger2md endpoints.
     * Only registered when whitelist or blacklist is configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "swagger2md", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<IpAccessFilter> ipAccessFilter(Swagger2mdProperties properties) {
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

        // Apply to swagger2md documentation paths
        registration.addUrlPatterns(
                properties.getMarkdownPath(),
                properties.getMarkdownPath() + "/*",
                properties.getLlmProbePath(),
                properties.getLlmProbePath() + "/*"
        );

        return registration;
    }
}
