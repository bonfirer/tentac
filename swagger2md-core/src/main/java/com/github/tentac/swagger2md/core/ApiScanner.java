package com.github.tentac.swagger2md.core;

import com.github.tentac.swagger2md.model.EndpointInfo;
import com.github.tentac.swagger2md.model.ParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Scans Spring controllers to discover API endpoints.
 */
public class ApiScanner {

    private static final List<Class<? extends Annotation>> MAPPING_ANNOTATIONS = Arrays.asList(
            RequestMapping.class, GetMapping.class, PostMapping.class,
            PutMapping.class, DeleteMapping.class, PatchMapping.class
    );

    private final JsonExampleGenerator jsonExampleGenerator = new JsonExampleGenerator();

    /**
     * Scan a controller class and extract all endpoint information.
     *
     * @param controllerClass the controller class to scan
     * @param basePath        the base path from class-level @RequestMapping
     * @return list of discovered endpoints
     */
    public List<EndpointInfo> scanController(Class<?> controllerClass, String basePath) {
        List<EndpointInfo> endpoints = new ArrayList<>();

        // Get class-level tags from @Api or @MarkdownApi
        List<String> classTags = extractClassTags(controllerClass);

        // Get class-level description
        String classDescription = extractClassDescription(controllerClass);

        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            EndpointInfo endpoint = extractEndpoint(method, basePath, classTags, classDescription);
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        return endpoints;
    }

    /**
     * Get the base path from a controller class's @RequestMapping annotation.
     */
    public String getBasePath(Class<?> controllerClass) {
        RequestMapping rm = controllerClass.getAnnotation(RequestMapping.class);
        if (rm != null && rm.value().length > 0) {
            String basePath = rm.value()[0];
            // Ensure base path starts with /
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            // Remove trailing slash
            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            return basePath;
        }
        return "";
    }

    /**
     * Check if a class is a REST controller.
     */
    public boolean isRestController(Class<?> clazz) {
        RestController restController = clazz.getAnnotation(RestController.class);
        if (restController != null) {
            return true;
        }
        Controller controller = clazz.getAnnotation(Controller.class);
        if (controller != null) {
            // Check if any method has @ResponseBody
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getAnnotation(ResponseBody.class) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> extractClassTags(Class<?> controllerClass) {
        List<String> tags = new ArrayList<>();

        // Try Swagger2 @Api annotation via reflection
        try {
            Class<?> apiAnnotation = Class.forName("io.swagger.annotations.Api");
            Annotation api = controllerClass.getAnnotation((Class<Annotation>) apiAnnotation);
            if (api != null) {
                Method tagsMethod = apiAnnotation.getMethod("tags");
                String[] swaggerTags = (String[]) tagsMethod.invoke(api);
                if (swaggerTags != null && swaggerTags.length > 0) {
                    for (String tag : swaggerTags) {
                        if (tag != null && !tag.isEmpty()) {
                            tags.add(tag);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Swagger annotations not present
        }

        // Try custom @MarkdownApi annotation
        com.github.tentac.swagger2md.annotation.MarkdownApi mdApi =
                controllerClass.getAnnotation(com.github.tentac.swagger2md.annotation.MarkdownApi.class);
        if (mdApi != null && mdApi.tags().length > 0) {
            for (String tag : mdApi.tags()) {
                if (tag != null && !tag.isEmpty() && !tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }

        // Default: use class simple name
        if (tags.isEmpty()) {
            tags.add(controllerClass.getSimpleName());
        }

        return tags;
    }

    private String extractClassDescription(Class<?> controllerClass) {
        // Try Swagger2 @Api
        try {
            Class<?> apiAnnotation = Class.forName("io.swagger.annotations.Api");
            Annotation api = controllerClass.getAnnotation((Class<Annotation>) apiAnnotation);
            if (api != null) {
                Method descMethod = apiAnnotation.getMethod("description");
                String desc = (String) descMethod.invoke(api);
                if (desc != null && !desc.isEmpty()) {
                    return desc;
                }
            }
        } catch (Exception ignored) {
        }

        // Try custom @MarkdownApi
        com.github.tentac.swagger2md.annotation.MarkdownApi mdApi =
                controllerClass.getAnnotation(com.github.tentac.swagger2md.annotation.MarkdownApi.class);
        if (mdApi != null && !mdApi.description().isEmpty()) {
            return mdApi.description();
        }

        return "";
    }

    private EndpointInfo extractEndpoint(Method method, String basePath, List<String> classTags, String classDescription) {
        String httpMethod = null;
        String[] paths = null;
        String[] consumes = null;
        String[] produces = null;

        // Detect HTTP method and path from Spring mapping annotations
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        if (getMapping != null) {
            httpMethod = "GET";
            paths = resolveMappingPath(getMapping.value(), getMapping.path());
            consumes = getMapping.consumes();
            produces = getMapping.produces();
        } else if (postMapping != null) {
            httpMethod = "POST";
            paths = resolveMappingPath(postMapping.value(), postMapping.path());
            consumes = postMapping.consumes();
            produces = postMapping.produces();
        } else if (putMapping != null) {
            httpMethod = "PUT";
            paths = resolveMappingPath(putMapping.value(), putMapping.path());
            consumes = putMapping.consumes();
            produces = putMapping.produces();
        } else if (deleteMapping != null) {
            httpMethod = "DELETE";
            paths = resolveMappingPath(deleteMapping.value(), deleteMapping.path());
            consumes = deleteMapping.consumes();
            produces = deleteMapping.produces();
        } else if (patchMapping != null) {
            httpMethod = "PATCH";
            paths = resolveMappingPath(patchMapping.value(), patchMapping.path());
            consumes = patchMapping.consumes();
            produces = patchMapping.produces();
        } else if (requestMapping != null) {
            // Try to infer HTTP method from RequestMapping method field
            RequestMethod[] methods = requestMapping.method();
            if (methods.length > 0) {
                httpMethod = methods[0].name();
            } else {
                httpMethod = "GET"; // Default
            }
            paths = resolveMappingPath(requestMapping.value(), requestMapping.path());
            consumes = requestMapping.consumes();
            produces = requestMapping.produces();
        }

        if (httpMethod == null || paths == null || paths.length == 0) {
            return null;
        }

        // Build full path
        String fullPath = basePath + normalizePath(paths[0]);

        EndpointInfo endpoint = new EndpointInfo();
        endpoint.setHttpMethod(httpMethod);
        endpoint.setPath(fullPath);
        endpoint.setTags(new ArrayList<>(classTags));
        endpoint.setOperationId(method.getName());

        if (consumes != null && consumes.length > 0) {
            endpoint.setConsumes(Arrays.asList(consumes));
        }
        if (produces != null && produces.length > 0) {
            endpoint.setProduces(Arrays.asList(produces));
        }

        // Extract parameters
        Parameter[] methodParams = method.getParameters();
        List<ParameterInfo> bodyParams = new ArrayList<>();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            ParameterInfo paramInfo = extractParameter(param, i);
            if (paramInfo != null) {
                if ("body".equals(paramInfo.getIn())) {
                    bodyParams.add(paramInfo);
                } else {
                    endpoint.getParameters().add(paramInfo);
                }
            }
        }

        // Generate JSON example for request body (take first body param)
        if (!bodyParams.isEmpty()) {
            ParameterInfo bodyParam = bodyParams.get(0);
            endpoint.setRequestBodyType(bodyParam.getType());
            // Try to find the actual parameter type from the method
            Class<?> bodyClass = findBodyParamClass(method);
            if (bodyClass != null) {
                String jsonExample = jsonExampleGenerator.generateJsonExample(bodyClass);
                if (jsonExample != null) {
                    endpoint.setRequestBodyExample(jsonExample);
                }
            }
        }

        // Generate JSON example for response body
        Class<?> returnType = method.getReturnType();
        if (returnType != void.class && returnType != Void.class) {
            String responseTypeName = getReturnTypeName(method);
            endpoint.setResponseType(responseTypeName);
            String jsonExample = jsonExampleGenerator.generateJsonExample(method.getGenericReturnType());
            if (jsonExample != null) {
                endpoint.setResponseExample(jsonExample);
            }
        }

        return endpoint;
    }

    private ParameterInfo extractParameter(Parameter param, int index) {
        ParameterInfo info = new ParameterInfo();
        info.setName(param.getName());

        // Determine parameter location from annotations
        RequestParam requestParam = param.getAnnotation(RequestParam.class);
        PathVariable pathVariable = param.getAnnotation(PathVariable.class);
        RequestHeader requestHeader = param.getAnnotation(RequestHeader.class);
        RequestBody requestBody = param.getAnnotation(RequestBody.class);

        if (requestParam != null) {
            info.setIn("query");
            if (!requestParam.value().isEmpty()) {
                info.setName(requestParam.value());
            } else if (!requestParam.name().isEmpty()) {
                info.setName(requestParam.name());
            }
            info.setRequired(requestParam.required());
            if (!requestParam.defaultValue().equals("\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n")) {
                info.setDefaultValue(requestParam.defaultValue());
            }
        } else if (pathVariable != null) {
            info.setIn("path");
            if (!pathVariable.value().isEmpty()) {
                info.setName(pathVariable.value());
            } else if (!pathVariable.name().isEmpty()) {
                info.setName(pathVariable.name());
            }
            info.setRequired(true);
        } else if (requestHeader != null) {
            info.setIn("header");
            if (!requestHeader.value().isEmpty()) {
                info.setName(requestHeader.value());
            } else if (!requestHeader.name().isEmpty()) {
                info.setName(requestHeader.name());
            }
            info.setRequired(requestHeader.required());
            if (!requestHeader.defaultValue().equals("\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n")) {
                info.setDefaultValue(requestHeader.defaultValue());
            }
        } else if (requestBody != null) {
            info.setIn("body");
            info.setRequired(requestBody.required());
        } else {
            // No Spring annotation, treat as request body if no mapping annotation is present
            info.setIn("query");
        }

        // Get parameter type
        info.setType(param.getType().getSimpleName());

        return info;
    }

    /**
     * Resolve mapping path, handling the case where both value() and path()
     * return empty arrays (meaning the method uses the class-level base path only).
     */
    private String[] resolveMappingPath(String[] value, String[] path) {
        String[] result = value.length > 0 ? value : path;
        // If both are empty, default to empty string (base path only)
        if (result.length == 0) {
            return new String[]{""};
        }
        return result;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        // Ensure path starts with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    /**
     * Find the class of the @RequestBody parameter in the method.
     */
    private Class<?> findBodyParamClass(Method method) {
        for (Parameter param : method.getParameters()) {
            if (param.getAnnotation(RequestBody.class) != null) {
                return param.getType();
            }
        }
        return null;
    }

    /**
     * Get a human-readable return type name, handling generics like List&lt;User&gt;.
     */
    private String getReturnTypeName(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType paramType) {
            Type rawType = paramType.getRawType();
            Type[] typeArgs = paramType.getActualTypeArguments();
            StringBuilder sb = new StringBuilder();
            if (rawType instanceof Class<?> rawClass) {
                sb.append(rawClass.getSimpleName());
            } else {
                sb.append(rawType.getTypeName());
            }
            if (typeArgs.length > 0) {
                sb.append("<");
                for (int i = 0; i < typeArgs.length; i++) {
                    if (i > 0) sb.append(", ");
                    if (typeArgs[i] instanceof Class<?> argClass) {
                        sb.append(argClass.getSimpleName());
                    } else {
                        sb.append(typeArgs[i].getTypeName());
                    }
                }
                sb.append(">");
            }
            return sb.toString();
        }
        return method.getReturnType().getSimpleName();
    }
}
