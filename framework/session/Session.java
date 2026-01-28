package framework.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    private final String id;
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    public Session(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void put(String key, Object value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }

    public void remove(String key) {
        data.remove(key);
    }

    public void invalidate() {
        data.clear();
    }
}
