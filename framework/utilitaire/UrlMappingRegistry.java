package framework.utilitaire;

import framework.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Responsable de la gestion du registre des mappings URL -> Classe/Méthode
 * Principe de Responsabilité Unique (SRP)
 */
public class UrlMappingRegistry {
    
    private Map<String, MappingInfo> urlMappings;
    private boolean initialized;
    
    public UrlMappingRegistry() {
        this.urlMappings = new HashMap<>();
        this.initialized = false;
    }
    
    /**
     * Construit le registre des URLs à partir des classes scannées
     * @param classes Liste des classes avec @Controller
     */
    public void buildRegistry(List<Class<?>> classes) {
        if (initialized) {
            System.out.println("Registre déjà initialisé.");
            return;
        }
        
        urlMappings.clear();
        int urlCount = 0;
        
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String url = mapping.value();
                    urlMappings.put(url, new MappingInfo(clazz, method, url));
                    urlCount++;
                }
            }
        }
        
        initialized = true;
        System.out.println("Registre construit: " + urlCount + " URL(s) mappée(s).\n");
    }
    
    /**
     * Recherche un mapping par URL
     * @param url L'URL à rechercher
     * @return MappingInfo ou null si non trouvé
     */
    public MappingInfo findByUrl(String url) {
        return urlMappings.get(url);
    }
    
    /**
     * Vérifie si le registre est initialisé
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Retourne le nombre d'URLs enregistrées
     */
    public int size() {
        return urlMappings.size();
    }
}
