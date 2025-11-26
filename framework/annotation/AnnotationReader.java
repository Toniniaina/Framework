package framework.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// Utilitaires déplacés
import framework.utilitaire.ConfigLoader;
import framework.utilitaire.ClassScanner;
import framework.utilitaire.UrlMappingRegistry;
import framework.utilitaire.MappingInfo;

/**
 * Service principal pour la gestion des annotations
 * Coordonne ConfigLoader, ClassScanner et UrlMappingRegistry
 * Principe de Responsabilité Unique et Inversion de Dépendance (SOLID)
 */
public class AnnotationReader {
    
    // Dépendances (Dependency Injection)
    private static final ConfigLoader configLoader = new ConfigLoader();
    private static final ClassScanner classScanner = new ClassScanner();
    private static final UrlMappingRegistry urlRegistry = new UrlMappingRegistry();
    
    
    
    
    /**
     * Filtre les classes qui ont des méthodes avec @GetMapping
     */
    private static List<Class<?>> findClassesWithMethodAnnotations(List<Class<?>> classes) {
        List<Class<?>> classesWithAnnotations = new ArrayList<>();
        
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(RequestMapping.class)) {
                    classesWithAnnotations.add(clazz);
                    break;
                }
            }
        }
        
        return classesWithAnnotations;
    }
    
    /**
     * Affiche toutes les classes avec annotations
     */
    public static void displayClassesWithAnnotations() {
        String basePackage = configLoader.getBasePackage();
        System.out.println("Scan du package de base: " + basePackage + "\n");
        
        List<Class<?>> classes = classScanner.scanPackage(basePackage);
        
        System.out.println("Classes avec @Controller découvertes: " + classes.size());
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
            
            // Lister les méthodes avec annotations de mapping
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    System.out.println("  └─ Méthode: " + method.getName() + " -> " + mapping.value() + " [GET]");
                }
                if (method.isAnnotationPresent(PostMapping.class)) {
                    PostMapping mapping = method.getAnnotation(PostMapping.class);
                    System.out.println("  └─ Méthode: " + method.getName() + " -> " + mapping.value() + " [POST]");
                }
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    String m = mapping.method() == null || mapping.method().isEmpty() ? "ANY" : mapping.method();
                    System.out.println("  └─ Méthode: " + method.getName() + " -> " + mapping.value() + " [" + m + "]");
                }
            }
        }
    }
    
    /**
     * Initialise le système en scannant toutes les URLs au démarrage
     * Coordonne les différents composants: ConfigLoader, ClassScanner, UrlMappingRegistry
     */
    public static void init() {
        if (urlRegistry.isInitialized()) {
            System.out.println("AnnotationReader déjà initialisé.");
            return;
        }
        
        System.out.println("Initialisation du système de mapping d'URLs...");
        
        // 1. Charger la configuration
        configLoader.loadConfiguration();
        String basePackage = configLoader.getBasePackage();
        
        // 2. Scanner les classes du package
        List<Class<?>> classes = classScanner.scanPackage(basePackage);
        System.out.println("Classes avec @Controller découvertes: " + classes.size());
        
        // 3. Construire le registre des URLs
        urlRegistry.buildRegistry(classes);
    }
    
    /**
     * Recherche une URL et retourne les informations de mapping
     * @param url L'URL à rechercher
     * @return MappingInfo ou un objet vide si non trouvé (404)
     */
    public static MappingInfo findMappingByUrl(String url) {
        return findMappingByUrl(url, "GET");
    }

    /**
     * Recherche une URL en tenant compte de la méthode HTTP
     * @param url L'URL à rechercher
     * @param httpMethod La méthode HTTP (GET, POST...)
     * @return MappingInfo ou un objet vide si non trouvé (404). Si la ressource existe mais la méthode n'est pas autorisée, renvoie MappingInfo avec methodNotAllowed=true
     */
    public static MappingInfo findMappingByUrl(String url, String httpMethod) {
        if (!urlRegistry.isInitialized()) {
            System.out.println("ATTENTION: AnnotationReader n'est pas initialisé. Appelez init() au démarrage.");
            init();
        }

        MappingInfo info = urlRegistry.findByUrl(url, httpMethod);
        return info != null ? info : new MappingInfo();
    }
    
    /**
     * Affiche les informations de mapping pour une URL donnée
     * @param url L'URL à rechercher
     */
    public static void displayMappingForUrl(String url) {
        MappingInfo info = findMappingByUrl(url);
        
        if (info.isFound()) {
            System.out.println("URL: " + url);
            System.out.println("Classe: " + info.getClassName());
            System.out.println("Méthode: " + info.getMethodName());
        } else {
            System.out.println("404 - URL non trouvée: " + url);
        }
    }
}
