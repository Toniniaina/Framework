package framework.utilitaire;

import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsable de la gestion du registre des mappings URL -> Classe/Méthode
 * Principe de Responsabilité Unique (SRP)
 */
public class UrlMappingRegistry {
    
    // exact matches: url -> (method -> MappingInfo) ; method '*' means any
    private Map<String, Map<String, MappingInfo>> urlMappings;
    private List<PatternEntry> patternMappings;   // template-based matches
    private boolean initialized;
    
    public UrlMappingRegistry() {
        this.urlMappings = new HashMap<>();
        this.patternMappings = new ArrayList<>();
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
        patternMappings.clear();
        int urlCount = 0;
        
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(RequestMapping.class)) {
                    String url = null;
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping mapping = method.getAnnotation(GetMapping.class);
                        url = mapping.value();
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        PostMapping mapping = method.getAnnotation(PostMapping.class);
                        url = mapping.value();
                    } else if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                        url = mapping.value();
                    }
                    // determine allowed method
                    String declaredMethod = "*";
                    if (method.isAnnotationPresent(GetMapping.class)) declaredMethod = "GET";
                    else if (method.isAnnotationPresent(PostMapping.class)) declaredMethod = "POST";
                    else if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping rm = method.getAnnotation(RequestMapping.class);
                        if (rm != null && rm.method() != null && !rm.method().trim().isEmpty()) declaredMethod = rm.method().trim().toUpperCase();
                        else declaredMethod = "*";
                    }

                    if (isTemplate(url)) {
                        // Pour les templates, chercher si une PatternEntry existe déjà
                        PatternEntry existing = findPatternEntry(url);
                        if (existing != null) {
                            // Ajouter la méthode HTTP à l'entrée existante
                            existing.allowedMethods.add(declaredMethod);
                            // Si c'est une méthode différente, mettre à jour les infos
                            if (!existing.methods.containsKey(declaredMethod)) {
                                existing.methods.put(declaredMethod, new MethodInfo(clazz, method));
                            }
                        } else {
                            PatternEntry pe = compileTemplate(url, clazz, method);
                            pe.allowedMethods.add(declaredMethod);
                            pe.methods.put(declaredMethod, new MethodInfo(clazz, method));
                            patternMappings.add(pe);
                        }
                    } else {
                        // CORRECTION: ne pas écraser, accumuler les méthodes HTTP différentes
                        Map<String, MappingInfo> methodMap = urlMappings.computeIfAbsent(url, k -> new HashMap<>());
                        
                        // Vérifier si cette combinaison URL+méthode existe déjà
                        if (methodMap.containsKey(declaredMethod)) {
                            System.out.println("ATTENTION: Mapping dupliqué ignoré: " + declaredMethod + " " + url + 
                                             " dans " + clazz.getSimpleName() + "." + method.getName());
                        } else {
                            methodMap.put(declaredMethod, new MappingInfo(clazz, method, url, declaredMethod));
                            urlCount++;
                            System.out.println("Enregistré: " + declaredMethod + " " + url + 
                                             " -> " + clazz.getSimpleName() + "." + method.getName());
                        }
                    }
                }
            }
        }
        
        initialized = true;
        System.out.println("\nRegistre construit: " + urlCount + " URL(s) mappée(s).\n");
    }
    
    /**
     * Recherche un mapping par URL
     * @param url L'URL à rechercher
     * @return MappingInfo ou null si non trouvé
     */
    public MappingInfo findByUrl(String url, String httpMethod) {
        String method = httpMethod == null ? "GET" : httpMethod.toUpperCase();

        // exact match
        Map<String, MappingInfo> methods = urlMappings.get(url);
        if (methods != null) {
            MappingInfo mi = methods.get(method);
            if (mi != null) return mi;
            mi = methods.get("*");
            if (mi != null) return mi;
            // method not allowed
            MappingInfo info = new MappingInfo();
            info.setMethodNotAllowed(methods.keySet());
            return info;
        }

        // pattern mappings
        java.util.Set<String> collectedAllowed = new java.util.HashSet<>();
        for (PatternEntry pe : patternMappings) {
            Matcher m = pe.pattern.matcher(url);
            if (m.matches()) {
                // if allowed for this method
                if (pe.allowedMethods.contains(method) || pe.allowedMethods.contains("*")) {
                    MethodInfo mInfo = pe.methods.get(method);
                    if (mInfo == null) mInfo = pe.methods.get("*");
                    if (mInfo != null) {
                        MappingInfo info = new MappingInfo(mInfo.controllerClass, mInfo.method, pe.template, method);
                        for (int i = 0; i < pe.variableNames.size(); i++) {
                            String value = m.group(i + 1);
                            info.setPathVariable(pe.variableNames.get(i), value);
                        }
                        return info;
                    }
                }
                collectedAllowed.addAll(pe.allowedMethods);
            }
        }

        if (!collectedAllowed.isEmpty()) {
            MappingInfo info = new MappingInfo();
            info.setMethodNotAllowed(collectedAllowed);
            return info;
        }

        return null;
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

    // --- Internal helpers for template handling ---
    private boolean isTemplate(String url) {
        return url != null && url.contains("{") && url.contains("}");
    }

    private PatternEntry findPatternEntry(String template) {
        for (PatternEntry pe : patternMappings) {
            if (pe.template.equals(template)) {
                return pe;
            }
        }
        return null;
    }

    private PatternEntry compileTemplate(String template, Class<?> controller, Method method) {
        List<String> varNames = new ArrayList<>();
        StringBuilder regex = new StringBuilder();
        regex.append('^');
        int i = 0;
        while (i < template.length()) {
            char c = template.charAt(i);
            if (c == '{') {
                int end = template.indexOf('}', i + 1);
                if (end < 0) throw new IllegalArgumentException("Invalid template: " + template);
                String var = template.substring(i + 1, end).trim();
                if (var.isEmpty()) throw new IllegalArgumentException("Empty path variable in template: " + template);
                varNames.add(var);
                regex.append("([^/]+)");
                i = end + 1;
            } else {
                // escape regex special chars
                if (".[]{}()+-^$|\\".indexOf(c) >= 0) {
                    regex.append('\\');
                }
                regex.append(c);
                i++;
            }
        }
        regex.append('$');
        Pattern pattern = Pattern.compile(regex.toString());
        return new PatternEntry(template, pattern, varNames);
    }

    private static class MethodInfo {
        final Class<?> controllerClass;
        final Method method;

        MethodInfo(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
        }
    }

    private static class PatternEntry {
        final String template;
        final Pattern pattern;
        final List<String> variableNames;
        final java.util.Set<String> allowedMethods = new java.util.HashSet<>();
        final Map<String, MethodInfo> methods = new HashMap<>();

        PatternEntry(String template, Pattern pattern, List<String> variableNames) {
            this.template = template;
            this.pattern = pattern;
            this.variableNames = variableNames;
        }
    }
}