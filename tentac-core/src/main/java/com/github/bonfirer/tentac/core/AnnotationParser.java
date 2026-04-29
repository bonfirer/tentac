package com.github.tentac.core;

import com.github.tentac.annotation.MarkdownApi;
import com.github.tentac.annotation.MarkdownApiOperation;
import com.github.tentac.annotation.MarkdownApiParam;
import com.github.tentac.model.EndpointInfo;
import com.github.tentac.model.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Parses Swagger2 and custom annotations to enrich EndpointInfo
 * with additional metadata.
 */
public class AnnotationParser {

    /**
     * Enrich an EndpointInfo using annotations from the controller method.
     *
     * @param endpoint  the endpoint to enrich
     * @param method    the controller method
     */
    public void enrichEndpoint(EndpointInfo endpoint, Method method) {
        // Try Swagger2 @ApiOperation
        enrichFromSwaggerAnnotations(endpoint, method);

        // Try custom @MarkdownApiOperation
        enrichFromMarkdownAnnotations(endpoint, method);

        // Enrich parameters
        enrichParameters(endpoint, method);
    }

    private void enrichFromSwaggerAnnotations(EndpointInfo endpoint, Method method) {
        try {
            Class<?> apiOperationClass = Class.forName("io.swagger.annotations.ApiOperation");
            Annotation apiOp = method.getAnnotation((Class<Annotation>) apiOperationClass);
            if (apiOp != null) {
                Method valueMethod = apiOperationClass.getMethod("value");
                String value = (String) valueMethod.invoke(apiOp);
                if (value != null && !value.isEmpty()) {
                    endpoint.setSummary(value);
                }

                Method notesMethod = apiOperationClass.getMethod("notes");
                String notes = (String) notesMethod.invoke(apiOp);
                if (notes != null && !notes.isEmpty()) {
                    endpoint.setDescription(notes);
                }

                Method tagsMethod = apiOperationClass.getMethod("tags");
                String[] swaggerTags = (String[]) tagsMethod.invoke(apiOp);
                if (swaggerTags != null && swaggerTags.length > 0 && !isEmptyTags(swaggerTags)) {
                    // Replace class-level tags with method-level tags if specified
                    endpoint.setTags(Arrays.asList(swaggerTags));
                }

                try {
                    Method nicknameMethod = apiOperationClass.getMethod("nickname");
                    String nickname = (String) nicknameMethod.invoke(apiOp);
                    if (nickname != null && !nickname.isEmpty()) {
                        endpoint.setOperationId(nickname);
                    }
                } catch (NoSuchMethodException ignored) {
                }

                try {
                    Method deprecatedMethod = apiOperationClass.getMethod("hidden");
                    Boolean hidden = (Boolean) deprecatedMethod.invoke(apiOp);
                    if (hidden != null && hidden) {
                        endpoint.setDeprecated(true);
                    }
                } catch (NoSuchMethodException ignored) {
                }

                try {
                    Method httpMethod = apiOperationClass.getMethod("httpMethod");
                    String http = (String) httpMethod.invoke(apiOp);
                    if (http != null && !http.isEmpty()) {
                        endpoint.setHttpMethod(http);
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (Exception ignored) {
            // Swagger annotations not present
        }
    }

    private void enrichFromMarkdownAnnotations(EndpointInfo endpoint, Method method) {
        MarkdownApiOperation mdOp = method.getAnnotation(MarkdownApiOperation.class);
        if (mdOp != null) {
            if (!mdOp.value().isEmpty()) {
                endpoint.setSummary(mdOp.value());
            }
            if (!mdOp.notes().isEmpty()) {
                endpoint.setDescription(mdOp.notes());
            }
            if (mdOp.tags().length > 0 && !isEmptyTags(mdOp.tags())) {
                endpoint.setTags(Arrays.asList(mdOp.tags()));
            }
            if (!mdOp.httpMethod().isEmpty()) {
                endpoint.setHttpMethod(mdOp.httpMethod());
            }
        }
    }

    private void enrichParameters(EndpointInfo endpoint, Method method) {
        Parameter[] methodParams = method.getParameters();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            ParameterInfo paramInfo = findParameterInfo(endpoint, param.getName(), i);
            if (paramInfo != null) {
                enrichParameterFromSwagger(paramInfo, param);
                enrichParameterFromMarkdown(paramInfo, param);
            }
        }
    }

    private ParameterInfo findParameterInfo(EndpointInfo endpoint, String name, int index) {
        for (ParameterInfo pi : endpoint.getParameters()) {
            if (pi.getName().equals(name)) {
                return pi;
            }
        }
        // Fallback by index
        if (index < endpoint.getParameters().size()) {
            return endpoint.getParameters().get(index);
        }
        return null;
    }

    private void enrichParameterFromSwagger(ParameterInfo paramInfo, Parameter param) {
        try {
            Class<?> apiParamClass = Class.forName("io.swagger.annotations.ApiParam");
            Annotation apiParam = param.getAnnotation((Class<Annotation>) apiParamClass);
            if (apiParam != null) {
                Method valueMethod = apiParamClass.getMethod("value");
                String value = (String) valueMethod.invoke(apiParam);
                if (value != null && !value.isEmpty()) {
                    paramInfo.setDescription(value);
                }

                Method nameMethod = apiParamClass.getMethod("name");
                String name = (String) nameMethod.invoke(apiParam);
                if (name != null && !name.isEmpty()) {
                    paramInfo.setName(name);
                }

                Method requiredMethod = apiParamClass.getMethod("required");
                Boolean required = (Boolean) requiredMethod.invoke(apiParam);
                if (required != null) {
                    paramInfo.setRequired(required);
                }

                Method defaultValueMethod = apiParamClass.getMethod("defaultValue");
                String defaultValue = (String) defaultValueMethod.invoke(apiParam);
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    paramInfo.setDefaultValue(defaultValue);
                }

                Method exampleMethod = apiParamClass.getMethod("example");
                String example = (String) exampleMethod.invoke(apiParam);
                if (example != null && !example.isEmpty()) {
                    paramInfo.setExample(example);
                }
            }
        } catch (Exception ignored) {
            // Swagger annotations not present
        }
    }

    /**
     * Check if a string array contains only empty strings (default annotation values).
     */
    private boolean isEmptyTags(String[] tags) {
        if (tags == null || tags.length == 0) return true;
        for (String tag : tags) {
            if (tag != null && !tag.isEmpty()) return false;
        }
        return true;
    }

    private void enrichParameterFromMarkdown(ParameterInfo paramInfo, Parameter param) {
        MarkdownApiParam mdParam = param.getAnnotation(MarkdownApiParam.class);
        if (mdParam != null) {
            if (!mdParam.value().isEmpty()) {
                paramInfo.setDescription(mdParam.value());
            }
            if (!mdParam.name().isEmpty()) {
                paramInfo.setName(mdParam.name());
            }
            if (mdParam.required()) {
                paramInfo.setRequired(true);
            }
            if (!mdParam.defaultValue().isEmpty()) {
                paramInfo.setDefaultValue(mdParam.defaultValue());
            }
            if (!mdParam.example().isEmpty()) {
                paramInfo.setExample(mdParam.example());
            }
            if (!mdParam.in().isEmpty()) {
                paramInfo.setIn(mdParam.in());
            }
        }
    }
}
