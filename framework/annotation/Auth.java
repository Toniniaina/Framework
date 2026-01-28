package framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour sécuriser un contrôleur ou une méthode.
 * Si présente, l'utilisateur doit être authentifié.
 * Si une valeur est spécifiée, l'utilisateur doit avoir ce rôle.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
    String value() default "";
}
