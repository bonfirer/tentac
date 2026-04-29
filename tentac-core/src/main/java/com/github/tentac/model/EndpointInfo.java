package com.github.tentac.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single API endpoint with its metadata.
 */
public class EndpointInfo {

    /** HTTP method: GET, POST, PUT, DELETE, PATCH, etc. */
    private String httpMethod;

    /** Full request path, e.g. /api/users/{id} */
    private String path;

    /** Short summary of what this endpoint does */
    private String summary;

    /** Detailed description */
    private String description;

    /** Tags for grouping (from @Api tags or controller name) */
    private List<String> tags = new ArrayList<>();

    /** Consumes media types, e.g. application/json */
    private List<String> consumes = new ArrayList<>();

    /** Produces media types, e.g. application/json */
    private List<String> produces = new ArrayList<>();

    /** Request parameters */
    private List<ParameterInfo> parameters = new ArrayList<>();

    /** Request body info (nullable) */
    private String requestBodyType;

    /** Request body example JSON (nullable) */
    private String requestBodyExample;

    /** Response type (nullable for void) */
    private String responseType;

    /** Response example JSON (nullable) */
    private String responseExample;

    /** Whether this endpoint is deprecated */
    private boolean deprecated;

    /** Operation ID for unique identification */
    private String operationId;

    /** Field descriptions for request body model (from @ApiModelProperty) */
    private List<ParameterInfo> requestBodyFields = new ArrayList<>();

    /** Field descriptions for response body model (from @ApiModelProperty) */
    private List<ParameterInfo> responseBodyFields = new ArrayList<>();

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public String getRequestBodyType() {
        return requestBodyType;
    }

    public void setRequestBodyType(String requestBodyType) {
        this.requestBodyType = requestBodyType;
    }

    public String getRequestBodyExample() {
        return requestBodyExample;
    }

    public void setRequestBodyExample(String requestBodyExample) {
        this.requestBodyExample = requestBodyExample;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public List<ParameterInfo> getRequestBodyFields() {
        return requestBodyFields;
    }

    public void setRequestBodyFields(List<ParameterInfo> requestBodyFields) {
        this.requestBodyFields = requestBodyFields;
    }

    public List<ParameterInfo> getResponseBodyFields() {
        return responseBodyFields;
    }

    public void setResponseBodyFields(List<ParameterInfo> responseBodyFields) {
        this.responseBodyFields = responseBodyFields;
    }
}
