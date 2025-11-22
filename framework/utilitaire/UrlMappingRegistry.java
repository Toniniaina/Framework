package framework.utilitaire;

import framework.annotation.GetMapping;

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
    
    private Map<String, MappingInfo> urlMappings; // exact matches
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
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String url = mapping.value();
                    if (isTemplate(url)) {
                        PatternEntry pe = compileTemplate(url, clazz, method);
                        patternMappings.add(pe);
                    } else {
                        urlMappings.put(url, new MappingInfo(clazz, method, url));
                        urlCount++;
                    }
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
        MappingInfo exact = urlMappings.get(url);
        if (exact != null) return exact;

        // Try pattern mappings
        for (PatternEntry pe : patternMappings) {
            Matcher m = pe.pattern.matcher(url);
            if (m.matches()) {
                MappingInfo info = new MappingInfo(pe.controllerClass, pe.method, pe.template);
                // extract variables by index order
                for (int i = 0; i < pe.variableNames.size(); i++) {
                    String value = m.group(i + 1);
                    info.setPathVariable(pe.variableNames.get(i), value);
                }
                return info;
            }
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
        return new PatternEntry(template, controller, method, pattern, varNames);
    }

    private static class PatternEntry {
        final String template;
        final Class<?> controllerClass;
        final Method method;
        final Pattern pattern;
        final List<String> variableNames;

        PatternEntry(String template, Class<?> controllerClass, Method method, Pattern pattern, List<String> variableNames) {
            this.template = template;
            this.controllerClass = controllerClass;
            this.method = method;
            this.pattern = pattern;
            this.variableNames = variableNames;
        }
    }
}
