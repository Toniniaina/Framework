package framework.utilitaire;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class JsonSerializer {
    
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj.getClass().isArray()) {
            return toJsonArray(java.util.Arrays.asList((Object[])obj));
        }

        if (obj instanceof Collection) {
            return toJsonArray((Collection<?>) obj);
        }
        
        if (obj instanceof Map) {
            return toJsonMap((Map<?, ?>) obj);
        }
        
        return toJsonObject(obj);
    }
    
    private static String toJsonObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (!first) {
                    sb.append(",");
                }
                
                sb.append("\"").append(field.getName()).append("\":");
                sb.append(toJson(value));
                first = false;
            } catch (Exception e) {
                // Ignorer les champs inaccessibles
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private static String toJsonArray(Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : collection) {
            if (!first) {
                sb.append(",");
            }
            sb.append(toJson(item));
            first = false;
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    // Helper to handle arrays as lists for simplicity in recursion if needed, 
    // though the main entry point handles Object[] specifically.
    // Primitive arrays would need more specific handling or boxing.
    
    private static String toJsonMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
