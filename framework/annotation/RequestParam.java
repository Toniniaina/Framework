package framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a single request parameter to a controller method parameter.
 * Names are used exactly as provided (no renaming or transformation).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    /**
     * Exact name of the request parameter. If omitted, the method parameter name will be used
     * (requires compilation with -parameters to be reliable). For this project, prefer setting it explicitly.
     */
    String value() default "";

    /** Whether the parameter is required. */
    boolean required() default true;

    /** Default value when not present (only used if required=false). */
    String defaultValue() default "";
}
