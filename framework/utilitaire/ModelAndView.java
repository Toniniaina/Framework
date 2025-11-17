package framework.utilitaire;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal ModelAndView-like class to carry a view name and a model map.
 */
public class ModelAndView {
    private String viewName;
    private final Map<String, Object> model = new HashMap<>();

    public ModelAndView() { }

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public ModelAndView setViewName(String viewName) {
        this.viewName = viewName;
        return this;
    }

    public ModelAndView addObject(String attributeName, Object attributeValue) {
        model.put(attributeName, attributeValue);
        return this;
    }

    public Map<String, Object> getModel() {
        return Collections.unmodifiableMap(model);
    }
}
