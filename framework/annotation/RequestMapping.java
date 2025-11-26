package framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lightweight RequestMapping supporting a path and an optional HTTP method name.
 * If method() is empty, it means any HTTP method is accepted (wildcard).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();
    String method() default ""; // e.g. "GET", "POST"; empty = any
}
