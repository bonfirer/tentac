package com.github.bonfirer.tentac.model;

/**
 * Represents a response definition for an API endpoint.
 */
public class ResponseInfo {

    /** HTTP status code as string, e.g. "200", "404", "500" */
    private String statusCode;

    /** Description of this response */
    private String description;

    /** Response body type (nullable for void) */
    private String responseType;

    /** Example JSON (nullable) */
    private String example;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
