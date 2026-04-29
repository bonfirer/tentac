package com.github.tentac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for standalone mode (without Swagger2).
 * Describes a parameter of an API operation.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkdownApiParam {

    /** Parameter name */
    String name() default "";

    /** Description */
    String value() default "";

    /** Whether this parameter is required */
    boolean required() default false;

    /** Default value */
    String defaultValue() default "";

    /** Example value */
    String example() default "";

    /** Parameter location: query, path, header, form, body */
    String in() default "query";
}
