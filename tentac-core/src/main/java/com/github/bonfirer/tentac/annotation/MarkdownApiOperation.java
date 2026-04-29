package com.github.bonfirer.tentac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for standalone mode (without Swagger2).
 * Describes a single API operation / endpoint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkdownApiOperation {

    /** Short summary */
    String value() default "";

    /** Detailed description / notes */
    String notes() default "";

    /** Tags for grouping */
    String[] tags() default {};

    /** HTTP method override (normally auto-detected) */
    String httpMethod() default "";
}
