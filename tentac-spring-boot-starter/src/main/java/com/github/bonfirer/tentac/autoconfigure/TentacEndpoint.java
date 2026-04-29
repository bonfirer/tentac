package com.github.bonfirer.tentac.autoconfigure;

import com.github.bonfirer.tentac.core.MarkdownGenerator;
import com.github.bonfirer.tentac.model.EndpointInfo;
import com.github.bonfirer.tentac.probe.LlmProbeGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing Tentac documentation endpoints.
 * Paths are configurable via TentacProperties.
 */
@RestController
@ConditionalOnProperty(prefix = "tentac", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TentacEndpoint {

    private final TentacProperties properties;
    private final MarkdownGenerator generator;
    private final LlmProbeGenerator probeGenerator;
    private final ApplicationContext applicationContext;

    @Autowired
    public TentacEndpoint(TentacProperties properties,
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
    @GetMapping(value = "${tentac.markdown-path:/v2/markdown}",
                produces = "text/markdown;charset=UTF-8")
    public String getMarkdown() {
        return generator.generate(applicationContext);
    }

    /**
     * Serve LLM-optimized probe output.
     */
    @GetMapping(value = "${tentac.llm-probe-path:/v2/llm-probe}",
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
    @GetMapping(value = "${tentac.llm-probe-path:/v2/llm-probe}/json",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointInfo> getLlmProbeJson() {
        return generator.getEndpoints(applicationContext);
    }
}
