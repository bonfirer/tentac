package com.github.tentac.swagger2md.autoconfigure;

import com.github.tentac.swagger2md.core.MarkdownGenerator;
import com.github.tentac.swagger2md.model.EndpointInfo;
import com.github.tentac.swagger2md.probe.LlmProbeGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing Swagger2md documentation endpoints.
 * Paths are configurable via Swagger2mdProperties.
 */
@RestController
@ConditionalOnProperty(prefix = "swagger2md", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Swagger2mdEndpoint {

    private final Swagger2mdProperties properties;
    private final MarkdownGenerator generator;
    private final LlmProbeGenerator probeGenerator;
    private final ApplicationContext applicationContext;

    @Autowired
    public Swagger2mdEndpoint(Swagger2mdProperties properties,
                              MarkdownGenerator generator,
                              LlmProbeGenerator probeGenerator,
                              ApplicationContext applicationContext) {
        this.properties = properties;
        this.generator = generator;
        this.probeGenerator = probeGenerator;
        this.applicationContext = applicationContext;
    }

    /**
     * Serve full Markdown API documentation.
     */
    @GetMapping(value = "${swagger2md.markdown-path:/v2/markdown}",
                produces = "text/markdown;charset=UTF-8")
    public String getMarkdown() {
        return generator.generate(applicationContext);
    }

    /**
     * Serve LLM-optimized probe output.
     */
    @GetMapping(value = "${swagger2md.llm-probe-path:/v2/llm-probe}",
                produces = "text/markdown;charset=UTF-8")
    public String getLlmProbe() {
        List<EndpointInfo> endpoints = generator.getEndpoints(applicationContext);
        return probeGenerator.generate(
                properties.getTitle(),
                properties.getDescription(),
                properties.getVersion(),
                endpoints);
    }

    /**
     * Serve raw JSON probe output for programmatic consumption by LLMs.
     */
    @GetMapping(value = "${swagger2md.llm-probe-path:/v2/llm-probe}/json",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointInfo> getLlmProbeJson() {
        return generator.getEndpoints(applicationContext);
    }
}
