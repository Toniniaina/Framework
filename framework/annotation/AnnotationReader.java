package framework.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationReader {
    
    public static void readGetMappingAnnotations(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping annotation = method.getAnnotation(GetMapping.class);
                String url = annotation.value();
                System.out.println("URL trouvée: " + url);
            }
        }
    }
    
    public static List<Class<?>> findClassesWithMethodAnnotations(Class<?>[] classes) {
        List<Class<?>> classesWithAnnotations = new ArrayList<>();
        
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            boolean hasMethodAnnotation = false;
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    hasMethodAnnotation = true;
                    break;
                }
            }
            
            if (hasMethodAnnotation) {
                classesWithAnnotations.add(clazz);
            }
        }
        
        return classesWithAnnotations;
    }
    
    public static void displayClassesWithAnnotations(Class<?>[] classes) {
        List<Class<?>> annotatedClasses = findClassesWithMethodAnnotations(classes);
        
        System.out.println("Classes utilisant l'annotation @GetMapping au niveau méthode:");
        for (Class<?> clazz : annotatedClasses) {
            System.out.println("- " + clazz.getSimpleName());
            
            // Afficher aussi si la classe a l'annotation @Controller
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controller = clazz.getAnnotation(Controller.class);
                String value = controller.value().isEmpty() ? "" : " (value: " + controller.value() + ")";
                System.out.println("  └─ Annotée avec @Controller" + value);
            }
            
            // Lister les méthodes avec @GetMapping
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    System.out.println("  └─ Méthode: " + method.getName() + " -> " + mapping.value());
                }
            }
        }
    }
}
