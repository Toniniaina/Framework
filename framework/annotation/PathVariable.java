package framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a URI template variable to a method parameter.
 * Example: @GetMapping("/produits/{id}") and method parameter @PathVariable("id") int id
 *////sprint 6 ter
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    String value();
}
