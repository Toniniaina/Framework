package framework.utilitaire;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FormMapper {

    public static class FormMappingException extends RuntimeException {
        private final Map<String, String> fieldErrors = new HashMap<>();

        public FormMappingException(String message) {
            super(message);
        }

        public void addFieldError(String field, String message) {
            fieldErrors.put(field, message);
        }

        public boolean hasErrors() {
            return !fieldErrors.isEmpty();
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }
    }

    public static <T> T map(Map<String, Object> source, Class<T> targetClass) {
        if (source == null) {
            source = new HashMap<>();
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            FormMappingException mappingException = new FormMappingException("Form mapping errors");

            for (Field field : targetClass.getDeclaredFields()) {
                String fieldName = field.getName();
                if (!source.containsKey(fieldName)) {
                    continue;
                }
                Object rawValue = source.get(fieldName);
                Class<?> fieldType = field.getType();
                Object converted;
                try {
                    converted = convertValue(rawValue, fieldType);
                } catch (RuntimeException e) {
                    mappingException.addFieldError(fieldName, "Invalid value '" + rawValue + "' for type " + fieldType.getSimpleName());
                    continue;
                }

                boolean set = false;
                String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                try {
                    Method setter = targetClass.getMethod(setterName, fieldType);
                    setter.invoke(target, converted);
                    set = true;
                } catch (NoSuchMethodException ignored) {
                }

                if (!set) {
                    boolean accessible = field.canAccess(target);
                    if (!accessible) {
                        field.setAccessible(true);
                    }
                    field.set(target, converted);
                    if (!accessible) {
                        field.setAccessible(false);
                    }
                }
            }

            if (mappingException.hasErrors()) {
                throw mappingException;
            }
            return target;
        } catch (FormMappingException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to map form data to " + targetClass.getName() + ": " + t.getMessage(), t);
        }
    }

    private static Object convertValue(Object raw, Class<?> type) {
        if (raw == null) {
            return null;
        }

        // Si la valeur est déjà du bon type, la retourner directement
        if (type.isInstance(raw)) {
            return raw;
        }

        // Si la valeur est une chaîne, utiliser la conversion simple
        if (raw instanceof String) {
            return convertSimple((String) raw, type);
        }

        // Dernier recours : utiliser toString() puis conversion simple
        return convertSimple(raw.toString(), type);
    }

    private static Object convertSimple(String raw, Class<?> type) {
        if (type == String.class) return raw;
        if (type == int.class) return raw == null || raw.isEmpty() ? 0 : Integer.parseInt(raw);
        if (type == Integer.class) return raw == null || raw.isEmpty() ? null : Integer.valueOf(raw);
        if (type == long.class) return raw == null || raw.isEmpty() ? 0L : Long.parseLong(raw);
        if (type == Long.class) return raw == null || raw.isEmpty() ? null : Long.valueOf(raw);
        if (type == double.class) return raw == null || raw.isEmpty() ? 0d : Double.parseDouble(raw);
        if (type == Double.class) return raw == null || raw.isEmpty() ? null : Double.valueOf(raw);
        if (type == boolean.class) return raw != null && ("true".equalsIgnoreCase(raw) || "1".equals(raw));
        if (type == Boolean.class) return raw == null ? null : ("true".equalsIgnoreCase(raw) || "1".equals(raw));
        return null;
    }
}
