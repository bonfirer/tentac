package com.github.bonfirer.tentac.probe;

import com.github.bonfirer.tentac.model.EndpointInfo;
import com.github.bonfirer.tentac.model.ParameterInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates LLM-optimized structured output for API probing.
 * The output is a Markdown document enhanced with structured sections
 * that LLMs can easily parse and understand.
 */
public class LlmProbeGenerator {

    /**
     * Generate an LLM-optimized Markdown document describing the API.
     *
     * @param title       the API title
     * @param description the API description
     * @param version     the API version
     * @param endpoints   all endpoints
     * @return LLM-optimized Markdown
     */
    public String generate(String title, String description, String version,
                           List<EndpointInfo> endpoints) {
        StringBuilder sb = new StringBuilder();

        // === HEADER SECTION ===
        sb.append("# API Capability Manifest\n\n");
        sb.append("> **Purpose:** This document describes all available API endpoints ")
                .append("in a format optimized for LLM consumption.\n\n");

        sb.append("**API:** ").append(title != null ? title : "API Documentation").append("\n");
        sb.append("**Version:** ").append(version != null ? version : "1.0.0").append("\n");
        if (description != null && !description.isEmpty()) {
            sb.append("**Description:** ").append(description).append("\n");
        }
        sb.append("**Total Endpoints:** ").append(endpoints.size()).append("\n\n");

        // === CAPABILITY SUMMARY ===
        sb.append("---\n\n");
        sb.append("## Capability Summary\n\n");
        sb.append("| Method | Path | Operation ID | Summary |\n");
        sb.append("|--------|------|-------------|--------|\n");

        for (EndpointInfo endpoint : endpoints) {
            sb.append("| `").append(endpoint.getHttpMethod()).append("` | `")
                    .append(endpoint.getPath()).append("` | `")
                    .append(endpoint.getOperationId()).append("` | ")
                    .append(endpoint.getSummary() != null ? endpoint.getSummary() : "")
                    .append(" |\n");
        }
        sb.append("\n");

        // === CAPABILITY DETAILS ===
        sb.append("---\n\n");
        sb.append("## Capability Details\n\n");

        // Group by path for cleaner output
        Map<String, List<EndpointInfo>> grouped = endpoints.stream()
                .collect(Collectors.groupingBy(EndpointInfo::getPath));

        for (Map.Entry<String, List<EndpointInfo>> entry : grouped.entrySet()) {
            String path = entry.getKey();
            List<EndpointInfo> pathEndpoints = entry.getValue();

            sb.append("### ").append(path).append("\n\n");

            for (EndpointInfo endpoint : pathEndpoints) {
                sb.append("#### `").append(endpoint.getHttpMethod()).append("` ");
                if (endpoint.getSummary() != null && !endpoint.getSummary().isEmpty()) {
                    sb.append(endpoint.getSummary());
                } else {
                    sb.append(path);
                }
                sb.append("\n\n");

                // Operation ID
                sb.append("- **Operation ID:** `").append(endpoint.getOperationId()).append("`\n");

                // Description
                if (endpoint.getDescription() != null && !endpoint.getDescription().isEmpty()) {
                    sb.append("- **Description:** ").append(endpoint.getDescription()).append("\n");
                }

                // Deprecated
                if (endpoint.isDeprecated()) {
                    sb.append("- **Status:** DEPRECATED\n");
                }

                // Parameters (compact format for LLM)
                if (!endpoint.getParameters().isEmpty()) {
                    sb.append("- **Parameters:**\n");
                    for (ParameterInfo param : endpoint.getParameters()) {
                        sb.append("  - `").append(param.getName()).append("`")
                                .append(" (").append(param.getIn()).append(", ")
                                .append(param.getType()).append(")")
                                .append(param.isRequired() ? " [required]" : "");
                        if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                            sb.append(" - ").append(param.getDescription());
                        }
                        if (param.getExample() != null && !param.getExample().isEmpty()) {
                            sb.append(" (e.g. `").append(param.getExample()).append("`)");
                        }
                        sb.append("\n");
                    }
                }

                // Request Body (shown as JSON with field descriptions)
                if (endpoint.getRequestBodyType() != null && !endpoint.getRequestBodyType().isEmpty()) {
                    sb.append("- **Request Body:** `").append(endpoint.getRequestBodyType()).append("`\n");
                    if (endpoint.getRequestBodyExample() != null && !endpoint.getRequestBodyExample().isEmpty()) {
                        String indentedJson = indentJson(endpoint.getRequestBodyExample(), "    ");
                        sb.append("  ```json\n").append(indentedJson).append("\n  ```\n");
                    }
                    // Field descriptions
                    if (endpoint.getRequestBodyFields() != null && !endpoint.getRequestBodyFields().isEmpty()) {
                        sb.append("  **Fields:**\n");
                        for (ParameterInfo field : endpoint.getRequestBodyFields()) {
                            sb.append("  - `").append(field.getName()).append("`")
                                    .append(" (").append(field.getType() != null ? field.getType() : "?").append(")");
                            if (field.getDescription() != null && !field.getDescription().isEmpty()) {
                                sb.append(" - ").append(field.getDescription());
                            }
                            if (field.getExample() != null && !field.getExample().isEmpty()) {
                                sb.append(" (e.g. `").append(field.getExample()).append("`)");
                            }
                            sb.append("\n");
                        }
                    }
                }

                // Response Body (shown as JSON with field descriptions)
                if (endpoint.getResponseType() != null && !endpoint.getResponseType().isEmpty()) {
                    sb.append("- **Response:** `").append(endpoint.getResponseType()).append("`\n");
                    if (endpoint.getResponseExample() != null && !endpoint.getResponseExample().isEmpty()) {
                        String indentedJson = indentJson(endpoint.getResponseExample(), "    ");
                        sb.append("  ```json\n").append(indentedJson).append("\n  ```\n");
                    }
                    // Field descriptions
                    if (endpoint.getResponseBodyFields() != null && !endpoint.getResponseBodyFields().isEmpty()) {
                        sb.append("  **Fields:**\n");
                        for (ParameterInfo field : endpoint.getResponseBodyFields()) {
                            sb.append("  - `").append(field.getName()).append("`")
                                    .append(" (").append(field.getType() != null ? field.getType() : "?").append(")");
                            if (field.getDescription() != null && !field.getDescription().isEmpty()) {
                                sb.append(" - ").append(field.getDescription());
                            }
                            if (field.getExample() != null && !field.getExample().isEmpty()) {
                                sb.append(" (e.g. `").append(field.getExample()).append("`)");
                            }
                            sb.append("\n");
                        }
                    }
                }

                sb.append("\n");
            }
        }

        // === USAGE INSTRUCTIONS FOR LLM ===
        sb.append("---\n\n");
        sb.append("## LLM Usage Instructions\n\n");
        sb.append("When using this API, follow these rules:\n\n");
        sb.append("1. Always include required parameters marked with `[required]`\n");
        sb.append("2. Use `application/json` as Content-Type for POST/PUT/PATCH requests\n");
        sb.append("3. Path parameters (e.g. `{id}`) must be replaced with actual values\n");
        sb.append("4. Check Operation ID for the canonical method name\n");
        sb.append("5. Deprecated endpoints should be avoided when possible\n\n");

        sb.append("*Generated by Tentac LLM Probe*\n");

        return sb.toString();
    }

    /**
     * Indent each line of a multi-line JSON string.
     */
    private String indentJson(String json, String prefix) {
        if (json == null || json.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] lines = json.split("\n");
        for (String line : lines) {
            sb.append(prefix).append(line).append("\n");
        }
        return sb.toString();
    }
}
