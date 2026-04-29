package com.github.tentac.core;

import com.github.tentac.model.ParameterInfo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Generates JSON example strings from Java classes using reflection.
 * Handles primitives, wrappers, String, Date/Time, collections, maps, and nested objects.
 * Supports Jackson @JsonProperty annotation for custom field names.
 */
public class JsonExampleGenerator {

    private static final int MAX_DEPTH = 3;
    private static final int INDENT = 2;

    private final Set<Class<?>> seenClasses = new HashSet<>();

    /**
     * Generate a pretty-printed JSON example from a Java class.
     */
    public String generateJsonExample(Class<?> clazz) {
        if (clazz == null || clazz == void.class || clazz == Void.class) return null;
        seenClasses.clear();
        return toJsonValue(clazz, 0, false);
    }

    /**
     * Generate a JSON example for the given type, handling generics like List&lt;User&gt;.
     */
    public String generateJsonExample(Type type) {
        if (type == null) return null;
        seenClasses.clear();
        return toJsonValueForType(type, 0, false);
    }

    // ---- Internal recursive generation ----

    private String toJsonValue(Class<?> clazz, int depth, boolean inArray) {
        if (clazz == null) return "null";

        // Primitives and simple types
        if (isSimpleType(clazz)) {
            return simpleValue(clazz);
        }

        // Collection / array
        if (Collection.class.isAssignableFrom(clazz)) {
            return "[]";
        }
        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            if (componentType != null && !isSimpleType(componentType)) {
                return "[\n" + indent(depth + 1) + toJsonValue(componentType, depth + 1, true)
                        + "\n" + indent(depth) + "]";
            }
            return "[\"" + simpleValue(componentType != null ? componentType : String.class) + "\"]";
        }

        // Map
        if (Map.class.isAssignableFrom(clazz)) {
            return "{}";
        }

        // Stop recursion at max depth to avoid infinite loops
        if (depth >= MAX_DEPTH) {
            return isSimpleType(clazz) ? simpleValue(clazz) : "{}";
        }

        // Detect circular references
        if (seenClasses.contains(clazz)) {
            return "{ \"$ref\": \"...\" }";
        }
        seenClasses.add(clazz);

        // Complex object - read fields
        return generateObjectJson(clazz, depth);
    }

    private String toJsonValueForType(Type type, int depth, boolean inArray) {
        if (type instanceof Class<?> clazz) {
            // Check if it's a parameterized collection
            if (Collection.class.isAssignableFrom(clazz)) {
                return "[]";
            }
            if (Map.class.isAssignableFrom(clazz)) {
                return "{}";
            }
            return toJsonValue(clazz, depth, inArray);
        }

        if (type instanceof ParameterizedType paramType) {
            Type rawType = paramType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                // List<User> -> generate array with one User element
                if (Collection.class.isAssignableFrom(rawClass)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> elementClass) {
                        String elementJson = toJsonValue(elementClass, depth + 1, true);
                        return "[\n" + indent(depth + 1) + elementJson
                                + "\n" + indent(depth) + "]";
                    }
                    return "[]";
                }
                // Map<K,V>
                if (Map.class.isAssignableFrom(rawClass)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length >= 2 && typeArgs[0] instanceof Class<?> keyClass
                            && typeArgs[1] instanceof Class<?> valClass) {
                        String key = simpleValue(keyClass);
                        String val = toJsonValue(valClass, depth + 1, true);
                        return "{\n" + indent(depth + 1) + "\"" + key + "\": " + val
                                + "\n" + indent(depth) + "}";
                    }
                    return "{}";
                }
                return toJsonValue(rawClass, depth, inArray);
            }
        }

        return "null";
    }

    private String generateObjectJson(Class<?> clazz, int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        List<Field> fields = getAllFields(clazz);
        List<String> fieldEntries = new ArrayList<>();

        for (Field field : fields) {
            // Skip static, transient, synthetic fields
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())
                    || field.isSynthetic()) {
                continue;
            }

            String fieldName = getFieldName(field);
            Class<?> fieldType = field.getType();
            Type genericType = field.getGenericType();
            String value;

            if (genericType instanceof ParameterizedType paramType
                    && paramType.getRawType() instanceof Class<?> rawClass) {
                if (Collection.class.isAssignableFrom(rawClass)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> elementClass
                            && !isSimpleType(elementClass) && depth + 1 < MAX_DEPTH) {
                        String elem = toJsonValue(elementClass, depth + 2, true);
                        value = "[\n" + indent(depth + 2) + elem + "\n" + indent(depth + 1) + "]";
                    } else {
                        value = "[]";
                    }
                } else if (Map.class.isAssignableFrom(rawClass)) {
                    value = "{}";
                } else {
                    value = toJsonValue(fieldType, depth + 1, false);
                }
            } else {
                value = toJsonValue(fieldType, depth + 1, false);
            }

            fieldEntries.add(indent(depth + 1) + "\"" + fieldName + "\": " + value);
        }

        if (!fieldEntries.isEmpty()) {
            sb.append("\n");
            sb.append(String.join(",\n", fieldEntries));
            sb.append("\n").append(indent(depth));
        }
        sb.append("}");

        return sb.toString();
    }

    // ---- Helpers ----

    private boolean isSimpleType(Class<?> clazz) {
        if (clazz == null) return true;
        return clazz.isPrimitive()
                || clazz == String.class
                || clazz == Integer.class || clazz == int.class
                || clazz == Long.class || clazz == long.class
                || clazz == Double.class || clazz == double.class
                || clazz == Float.class || clazz == float.class
                || clazz == Boolean.class || clazz == boolean.class
                || clazz == Short.class || clazz == short.class
                || clazz == Byte.class || clazz == byte.class
                || clazz == Character.class || clazz == char.class
                || clazz == BigDecimal.class
                || clazz == Date.class
                || clazz == LocalDate.class
                || clazz == LocalDateTime.class
                || clazz == UUID.class
                || clazz.isEnum();
    }

    private String simpleValue(Class<?> clazz) {
        if (clazz == null) return "null";
        if (clazz == String.class || clazz == Character.class || clazz == char.class) return "\"string\"";
        if (clazz == Integer.class || clazz == int.class) return "0";
        if (clazz == Long.class || clazz == long.class) return "0";
        if (clazz == Double.class || clazz == double.class) return "0.0";
        if (clazz == Float.class || clazz == float.class) return "0.0";
        if (clazz == Boolean.class || clazz == boolean.class) return "true";
        if (clazz == Short.class || clazz == short.class) return "0";
        if (clazz == Byte.class || clazz == byte.class) return "0";
        if (clazz == BigDecimal.class) return "\"0.00\"";
        if (clazz == Date.class) return "\"2024-01-01T00:00:00Z\"";
        if (clazz == LocalDate.class) return "\"2024-01-01\"";
        if (clazz == LocalDateTime.class) return "\"2024-01-01T00:00:00\"";
        if (clazz == UUID.class) return "\"550e8400-e29b-41d4-a716-446655440000\"";
        if (clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            return "\"" + (constants != null && constants.length > 0
                    ? constants[0].toString() : "UNKNOWN") + "\"";
        }
        return "\"\"";
    }

    /**
     * Get the JSON field name, respecting @JsonProperty annotation.
     */
    private String getFieldName(Field field) {
        // Check for Jackson @JsonProperty via reflection
        try {
            Class<?> jsonPropertyClass = Class.forName("com.fasterxml.jackson.annotation.JsonProperty");
            for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
                if (jsonPropertyClass.isInstance(annotation)) {
                    java.lang.reflect.Method valueMethod = jsonPropertyClass.getMethod("value");
                    String value = (String) valueMethod.invoke(annotation);
                    if (value != null && !value.isEmpty()
                            && !"\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n".equals(value)) {
                        return value;
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return field.getName();
    }

    /**
     * Extract field descriptions from @ApiModelProperty annotations on the given class.
     *
     * @param clazz the model class to introspect
     * @return list of field descriptions (name, type, description, required, example)
     */
    public List<ParameterInfo> extractFieldDescriptions(Class<?> clazz) {
        if (clazz == null || isSimpleType(clazz)) return Collections.emptyList();
        List<ParameterInfo> fields = new ArrayList<>();
        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())
                    || field.isSynthetic()) {
                continue;
            }
            ParameterInfo info = new ParameterInfo();
            info.setName(getFieldName(field));
            info.setType(field.getType().getSimpleName());
            info.setIn("body");
            // Try @ApiModelProperty
            extractSwaggerFieldAnnotation(field, info);
            fields.add(info);
        }
        return fields;
    }

    private void extractSwaggerFieldAnnotation(Field field, ParameterInfo info) {
        // Try @MarkdownApiModelProperty (standalone, always available)
        try {
            com.github.tentac.annotation.MarkdownApiModelProperty mdProp =
                    field.getAnnotation(com.github.tentac.annotation.MarkdownApiModelProperty.class);
            if (mdProp != null) {
                if (!mdProp.value().isEmpty()) {
                    info.setDescription(mdProp.value());
                }
                info.setRequired(mdProp.required());
                if (!mdProp.example().isEmpty()) {
                    info.setExample(mdProp.example());
                }
                if (!mdProp.notes().isEmpty() && info.getDescription() == null) {
                    info.setDescription(mdProp.notes());
                }
                return;
            }
        } catch (Exception ignored) {
        }

        // Try @ApiModelProperty (Swagger2, optional)
        try {
            Class<?> apiModelPropClass = Class.forName("io.swagger.annotations.ApiModelProperty");
            for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
                if (apiModelPropClass.isInstance(annotation)) {
                    java.lang.reflect.Method valueMethod = apiModelPropClass.getMethod("value");
                    String value = (String) valueMethod.invoke(annotation);
                    if (value != null && !value.isEmpty()) {
                        info.setDescription(value);
                    }
                    try {
                        java.lang.reflect.Method requiredMethod = apiModelPropClass.getMethod("required");
                        Boolean required = (Boolean) requiredMethod.invoke(annotation);
                        if (required != null) info.setRequired(required);
                    } catch (NoSuchMethodException ignored2) {}
                    try {
                        java.lang.reflect.Method exampleMethod = apiModelPropClass.getMethod("example");
                        String example = (String) exampleMethod.invoke(annotation);
                        if (example != null && !example.isEmpty()) info.setExample(example);
                    } catch (NoSuchMethodException ignored2) {}
                    try {
                        java.lang.reflect.Method notesMethod = apiModelPropClass.getMethod("notes");
                        String notes = (String) notesMethod.invoke(annotation);
                        if (notes != null && !notes.isEmpty()) info.setDescription(notes);
                    } catch (NoSuchMethodException ignored2) {}
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Get the element type from a generic return type like List&lt;User&gt;.
     */
    public Class<?> getElementType(Type type) {
        if (type instanceof ParameterizedType paramType) {
            Type rawType = paramType.getRawType();
            if (rawType instanceof Class<?> rawClass && Collection.class.isAssignableFrom(rawClass)) {
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> elementClass) {
                    return elementClass;
                }
            }
        }
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        return null;
    }
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private String indent(int level) {
        return " ".repeat(level * INDENT);
    }
}
