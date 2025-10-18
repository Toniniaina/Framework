package framework.annotation;

import java.lang.reflect.Method;

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
}
