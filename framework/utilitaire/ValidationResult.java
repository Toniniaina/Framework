package framework.utilitaire;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidationResult {

    private final Map<String, String> fieldErrors = new LinkedHashMap<>();

    public void addError(String field, String message) {
        fieldErrors.put(field, message);
    }

    public boolean isValid() {
        return fieldErrors.isEmpty();
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
}
