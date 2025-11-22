package framework.utilitaire;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MappingInfo {
    private Class<?> controllerClass;
    private Method method;
    private String url;
    private boolean found;
    private Map<String, String> pathVariables;
    
    public MappingInfo(Class<?> controllerClass, Method method, String url) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.url = url;
        this.found = true;
        this.pathVariables = new HashMap<>();
    }
    
    public MappingInfo() {
        this.found = false;
        this.pathVariables = new HashMap<>();
    }
    
    public Class<?> getControllerClass() {
        return controllerClass;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public String getUrl() {
        return url;
    }
    
    public boolean isFound() {
        return found;
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
