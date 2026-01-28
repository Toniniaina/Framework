package framework.utilitaire;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MappingInfo {
    private Class<?> controllerClass;
    private Method method;
    private String url;
    private String httpMethod; // e.g. "GET", "POST" or "*" for any
    private boolean found;
    private Map<String, String> pathVariables;
    private boolean methodNotAllowed;
    private java.util.Set<String> allowedMethods;
    private framework.annotation.Auth authAnnotation;
    
    public MappingInfo(Class<?> controllerClass, Method method, String url) {
        this(controllerClass, method, url, "*");
    }

    public MappingInfo(Class<?> controllerClass, Method method, String url, String httpMethod) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.url = url;
        this.httpMethod = httpMethod == null ? "*" : httpMethod;
        this.found = true;
        this.pathVariables = new HashMap<>();
        this.methodNotAllowed = false;
        this.allowedMethods = null;
        
        // Résoudre l'annotation @Auth (méthode d'abord, puis classe)
        if (method != null && method.isAnnotationPresent(framework.annotation.Auth.class)) {
            this.authAnnotation = method.getAnnotation(framework.annotation.Auth.class);
        } else if (controllerClass != null && controllerClass.isAnnotationPresent(framework.annotation.Auth.class)) {
            this.authAnnotation = controllerClass.getAnnotation(framework.annotation.Auth.class);
        }
    }
    
    public MappingInfo() {
        this.found = false;
        this.pathVariables = new HashMap<>();
        this.httpMethod = null;
        this.methodNotAllowed = false;
        this.allowedMethods = null;
    }
    
    public Class<?> getControllerClass() {
        return controllerClass;
    }
    
    public Method getMethod() {
        return method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
    
    public String getUrl() {
        return url;
    }
    
    public boolean isFound() {
        return found;
    }

    public boolean isMethodNotAllowed() {
        return methodNotAllowed;
    }

    public java.util.Set<String> getAllowedMethods() {
        return allowedMethods == null ? java.util.Collections.emptySet() : java.util.Collections.unmodifiableSet(allowedMethods);
    }

    public void setMethodNotAllowed(java.util.Set<String> allowed) {
        this.found = false;
        this.methodNotAllowed = true;
        this.allowedMethods = allowed == null ? new java.util.HashSet<>() : new java.util.HashSet<>(allowed);
    }
    
    public framework.annotation.Auth getAuthAnnotation() {
        return authAnnotation;
    }

    public String getClassName() {
        return found ? controllerClass.getSimpleName() : null;
    }
    
    public String getMethodName() {
        return found ? method.getName() : null;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables == null ? Collections.emptyMap() : Collections.unmodifiableMap(pathVariables);
    }

    public void setPathVariable(String name, String value) {
        if (this.pathVariables == null) this.pathVariables = new HashMap<>();
        this.pathVariables.put(name, value);
    }

    public void setPathVariables(Map<String, String> vars) {
        if (this.pathVariables == null) this.pathVariables = new HashMap<>();
        this.pathVariables.clear();
        if (vars != null) this.pathVariables.putAll(vars);
    }
}
