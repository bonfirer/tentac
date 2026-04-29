package com.github.tentac.swagger2md.core;

import com.github.tentac.swagger2md.model.EndpointInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

/**
 * Main generation engine that orchestrates scanning, parsing, and formatting
 * to produce Markdown API documentation.
 */
public class MarkdownGenerator {

    private final ApiScanner scanner;
    private final AnnotationParser parser;
    private final MarkdownFormatter formatter;

    private String title = "API Documentation";
    private String description = "";
    private String version = "1.0.0";
    private String basePackage = "";

    public MarkdownGenerator() {
        this.scanner = new ApiScanner();
        this.parser = new AnnotationParser();
        this.formatter = new MarkdownFormatter();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Generate Markdown documentation by scanning Spring application context.
     *
     * @param applicationContext the Spring application context
     * @return Markdown formatted API documentation
     */
    public String generate(ApplicationContext applicationContext) {
        List<EndpointInfo> allEndpoints = new ArrayList<>();

        // Get all beans annotated with @RestController
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(
                org.springframework.web.bind.annotation.RestController.class);

        for (String beanName : beanNames) {
            Class<?> controllerClass = applicationContext.getType(beanName);
            if (controllerClass == null) {
                continue;
            }

            // Filter by base package if configured
            if (!basePackage.isEmpty() && !controllerClass.getPackageName().startsWith(basePackage)) {
                continue;
            }

            // Check if hidden via @MarkdownApi
            com.github.tentac.swagger2md.annotation.MarkdownApi mdApi =
                    controllerClass.getAnnotation(com.github.tentac.swagger2md.annotation.MarkdownApi.class);
            if (mdApi != null && mdApi.hidden()) {
                continue;
            }

            // Get base path
            String basePath = scanner.getBasePath(controllerClass);

            // Scan endpoints
            List<EndpointInfo> endpoints = scanner.scanController(controllerClass, basePath);

            // Enrich each endpoint with annotations
            for (EndpointInfo endpoint : endpoints) {
                try {
                    Method method = controllerClass.getDeclaredMethod(endpoint.getOperationId(),
                            getParameterTypes(controllerClass, endpoint.getOperationId()));
                    parser.enrichEndpoint(endpoint, method);
                } catch (NoSuchMethodException ignored) {
                }
            }

            allEndpoints.addAll(endpoints);
        }

        return formatter.format(title, description, version, allEndpoints);
    }

    /**
     * Generate Markdown documentation from a pre-scanned list of endpoints.
     *
     * @param endpoints the list of endpoints
     * @return Markdown formatted API documentation
     */
    public String generate(List<EndpointInfo> endpoints) {
        return formatter.format(title, description, version, endpoints);
    }

    public List<EndpointInfo> getEndpoints(ApplicationContext applicationContext) {
        List<EndpointInfo> allEndpoints = new ArrayList<>();

        String[] beanNames = applicationContext.getBeanNamesForAnnotation(
                org.springframework.web.bind.annotation.RestController.class);

        for (String beanName : beanNames) {
            Class<?> controllerClass = applicationContext.getType(beanName);
            if (controllerClass == null) continue;

            if (!basePackage.isEmpty() && !controllerClass.getPackageName().startsWith(basePackage)) {
                continue;
            }

            com.github.tentac.swagger2md.annotation.MarkdownApi mdApi =
                    controllerClass.getAnnotation(com.github.tentac.swagger2md.annotation.MarkdownApi.class);
            if (mdApi != null && mdApi.hidden()) continue;

            String basePath = scanner.getBasePath(controllerClass);
            List<EndpointInfo> endpoints = scanner.scanController(controllerClass, basePath);

            for (EndpointInfo endpoint : endpoints) {
                try {
                    Method method = controllerClass.getDeclaredMethod(endpoint.getOperationId(),
                            getParameterTypes(controllerClass, endpoint.getOperationId()));
                    parser.enrichEndpoint(endpoint, method);
                } catch (NoSuchMethodException ignored) {
                }
            }

            allEndpoints.addAll(endpoints);
        }

        return allEndpoints;
    }

    private Class<?>[] getParameterTypes(Class<?> controllerClass, String methodName) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getParameterTypes();
            }
        }
        return new Class<?>[0];
    }
}
