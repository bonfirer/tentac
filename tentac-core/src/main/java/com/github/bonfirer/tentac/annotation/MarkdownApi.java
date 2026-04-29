package com.github.bonfirer.tentac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for standalone mode (without Swagger2).
 * Marks a controller class for API documentation generation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkdownApi {

    /** Tags for grouping APIs */
    String[] tags() default {};

    /** Description of this API group */
    String description() default "";

    /** Whether this controller should be included */
    boolean hidden() default false;
}
