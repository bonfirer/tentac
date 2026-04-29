package com.github.tentac.swagger2md.model;

/**
 * Represents a request parameter for an API endpoint.
 */
public class ParameterInfo {

    /** Parameter name */
    private String name;

    /** Parameter location: query, path, header, form, body */
    private String in;

    /** Short description */
    private String description;

    /** Whether this parameter is required */
    private boolean required;

    /** Data type, e.g. string, integer, boolean */
    private String type;

    /** Default value (nullable) */
    private String defaultValue;

    /** Example value (nullable) */
    private String example;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
