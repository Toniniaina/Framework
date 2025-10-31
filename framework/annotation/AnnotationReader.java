package framework.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class AnnotationReader {
    
    private static String basePackage = null;
    
    /**
     * Charge le package de base depuis le fichier config.properties dans resources/
     */
    private static String loadBasePackage() {
        if (basePackage != null) {
            return basePackage;
        }
        
        Properties props = new Properties();
        InputStream input = null;
        
        try {
            // Essayer de charger depuis le classpath
            input = AnnotationReader.class.getClassLoader().getResourceAsStream("config.properties");
            
            if (input == null) {
                // Essayer de charger depuis le répertoire testFramework/resources
                input = new FileInputStream("testFramework/resources/config.properties");
            }
            
            if (input != null) {
                props.load(input);
                basePackage = props.getProperty("base.package");
                if (basePackage != null) {
                    basePackage = basePackage.trim();
                    System.out.println("Package de base chargé depuis config.properties: " + basePackage);
                }
            } else {
                System.out.println("ERREUR: Fichier config.properties introuvable!");
                basePackage = "com.testframework"; // Valeur par défaut
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du config.properties: " + e.getMessage());
            basePackage = "com.testframework"; // Valeur par défaut
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    // Ignorer
                }
            }
        }
        
        return basePackage;
    }
    
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
    
    /**
     * Découvre automatiquement toutes les classes dans le package de base et ses sous-packages
     * Filtre uniquement les classes avec l'annotation @Controller
     */
    public static Class<?>[] discoverClasses() {
        List<Class<?>> classes = new ArrayList<>();
        String packageName = loadBasePackage();
        
        try {
            String path = packageName.replace('.', '/');
            URL resource = ClassLoader.getSystemClassLoader().getResource(path);
            
            if (resource != null) {
                File directory = new File(resource.getFile());
                
                if (directory.exists()) {
                    // Scanner récursivement le package et ses sous-packages
                    scanDirectory(directory, packageName, classes);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la découverte des classes: " + e.getMessage());
        }
        
        return classes.toArray(new Class<?>[0]);
    }
    
    /**
     * Scanne récursivement un répertoire pour trouver toutes les classes avec @Controller
     */
    private static void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Scanner récursivement les sous-répertoires (sous-packages)
                String subPackage = packageName + "." + file.getName();
                scanDirectory(file, subPackage, classes);
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                // Charger la classe
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(packageName + "." + className);
                    // Filtrer uniquement les classes avec @Controller
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Impossible de charger la classe: " + packageName + "." + className);
                } catch (NoClassDefFoundError e) {
                    // Ignorer les erreurs de classes internes ou dépendances manquantes
                }
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
    
    /**
     * Scanne le package de base et affiche les classes avec annotations
     */
    public static void displayClassesWithAnnotations() {
        String packageName = loadBasePackage();
        System.out.println("Scan du package de base: " + packageName + "\n");
        
        Class<?>[] classes = discoverClasses();
        
        System.out.println("Classes avec @Controller découvertes: " + classes.length);
        for (Class<?> c : classes) {
            System.out.println("- " + c.getName());
        }
        System.out.println();
        
        List<Class<?>> annotatedClasses = findClassesWithMethodAnnotations(classes);
        
        System.out.println("Classes utilisant l'annotation @GetMapping au niveau méthode:");
        for (Class<?> clazz : annotatedClasses) {
            System.out.println("- " + clazz.getSimpleName());
            
            // Afficher aussi si la classe a l'annotation @Controller
            if (clazz.isAnnotationPresent(Controller.class)) {
                System.out.println("  └─ Annotée avec @Controller");
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
    
    // Méthode maintenue pour compatibilité
    public static void displayClassesWithAnnotations(Class<?>[] classes) {
        List<Class<?>> annotatedClasses = findClassesWithMethodAnnotations(classes);
        
        System.out.println("Classes utilisant l'annotation @GetMapping au niveau méthode:");
        for (Class<?> clazz : annotatedClasses) {
            System.out.println("- " + clazz.getSimpleName());
            
            // Afficher aussi si la classe a l'annotation @Controller
            if (clazz.isAnnotationPresent(Controller.class)) {
                System.out.println("  └─ Annotée avec @Controller");
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
