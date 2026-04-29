package com.github.tentac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Standalone annotation for describing model fields (equivalent to Swagger2 @ApiModelProperty).
 * Used when Swagger2 is not on the classpath.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkdownApiModelProperty {

    /** Description of this field */
    String value() default "";

    /** Whether this field is required */
    boolean required() default false;

    /** Example value */
    String example() default "";

    /** Notes / additional details */
    String notes() default "";
}
