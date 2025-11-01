package framework.utilitaire;

import java.lang.reflect.Method;

public class MappingInfo {
    private Class<?> controllerClass;
    private Method method;
    private String url;
    private boolean found;
    
    public MappingInfo(Class<?> controllerClass, Method method, String url) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.url = url;
        this.found = true;
    }
    
    public MappingInfo() {
        this.found = false;
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
}
