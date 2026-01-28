package framework.session;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, Session> store = new ConcurrentHashMap<>();
    private static final String COOKIE_NAME = "FSID";
    private static final SecureRandom rnd = new SecureRandom();

    public static Session getOrCreate(HttpServletRequest req, HttpServletResponse resp) {
        String sid = readCookie(req);
        if (sid == null || sid.isEmpty()) {
            sid = newId();
            writeCookie(resp, sid, req.getContextPath());
        }
        return store.computeIfAbsent(sid, Session::new);
    }

    public static Session getById(String id) {
        if (id == null || id.isEmpty()) return null;
        return store.get(id);
    }

    public static void invalidate(String id) {
        if (id == null || id.isEmpty()) return;
        Session s = store.remove(id);
        if (s != null) s.invalidate();
    }

    private static String readCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (COOKIE_NAME.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private static void writeCookie(HttpServletResponse resp, String sid, String contextPath) {
        Cookie c = new Cookie(COOKIE_NAME, sid);
        c.setPath(contextPath == null || contextPath.isEmpty() ? "/" : contextPath);
        c.setHttpOnly(true);
        resp.addCookie(c);
    }

    private static String newId() {
        byte[] buf = new byte[18];
        rnd.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
