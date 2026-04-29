package com.github.tentac.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Tentac.
 * Prefix: tentac
 */
@ConfigurationProperties(prefix = "tentac")
public class TentacProperties {

    /** Enable or disable Tentac */
    private boolean enabled = true;

    /** API title for documentation header */
    private String title = "API Documentation";

    /** API description */
    private String description = "";

    /** API version */
    private String version = "1.0.0";

    /** Base package to scan for controllers (empty = scan all) */
    private String basePackage = "";

    /** Markdown endpoint path */
    private String markdownPath = "/v2/markdown";

    /** LLM probe endpoint path */
    private String llmProbePath = "/v2/llm-probe";

    /** Whether LLM probe endpoint is enabled */
    private boolean llmProbeEnabled = true;

    /** IP whitelist in CIDR notation (e.g. 192.168.1.0/24, 10.0.0.1/32) */
    private List<String> ipWhitelist = new ArrayList<>();

    /** IP blacklist in CIDR notation */
    private List<String> ipBlacklist = new ArrayList<>();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getMarkdownPath() {
        return markdownPath;
    }

    public void setMarkdownPath(String markdownPath) {
        this.markdownPath = markdownPath;
    }

    public String getLlmProbePath() {
        return llmProbePath;
    }

    public void setLlmProbePath(String llmProbePath) {
        this.llmProbePath = llmProbePath;
    }

    public boolean isLlmProbeEnabled() {
        return llmProbeEnabled;
    }

    public void setLlmProbeEnabled(boolean llmProbeEnabled) {
        this.llmProbeEnabled = llmProbeEnabled;
    }

    public List<String> getIpWhitelist() {
        return ipWhitelist;
    }

    public void setIpWhitelist(List<String> ipWhitelist) {
        this.ipWhitelist = ipWhitelist;
    }

    public List<String> getIpBlacklist() {
        return ipBlacklist;
    }

    public void setIpBlacklist(List<String> ipBlacklist) {
        this.ipBlacklist = ipBlacklist;
    }
}
