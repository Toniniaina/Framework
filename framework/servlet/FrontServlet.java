package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import framework.annotation.AnnotationReader;
import framework.annotation.RequestParam;
import framework.utilitaire.MappingInfo;
import framework.utilitaire.ConfigLoader;
import framework.utilitaire.MethodInvoker;
import framework.utilitaire.ModelAndView;

public class FrontServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String originalURI = (String) req.getAttribute("originalURI");
        String urlPath = originalURI != null ? originalURI : req.getRequestURI();
        String contextPath = req.getContextPath();
        String resourcePath = urlPath.startsWith(contextPath) ? urlPath.substring(contextPath.length()) : urlPath;

        // Initialiser l'annotation/mapping si nécessaire
        AnnotationReader.init();

        // Essayer de retrouver un mapping pour la ressource demandée
        MappingInfo mapping = AnnotationReader.findMappingByUrl(resourcePath);
        if (mapping.isFound()) {
            try {
                Class<?> controller = mapping.getControllerClass();
                Object instance = controller.getDeclaredConstructor().newInstance();
                // Resolve method parameters (GET only) using @RequestParam and simple injection
                java.lang.reflect.Method method = mapping.getMethod();
                java.lang.reflect.Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Class<?> type = parameters[i].getType();
                    // Injection of servlet objects
                    if (type == HttpServletRequest.class) { args[i] = req; continue; }
                    if (type == HttpServletResponse.class) { args[i] = resp; continue; }

                    RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                    if (rp != null) {
                        String paramName = rp.value();
                        // Strict rule: annotation value must be present and equal to the Java parameter name
                        if (paramName == null || paramName.isEmpty()) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.setContentType("text/plain; charset=UTF-8");
                            resp.getWriter().println("@RequestParam value must not be empty for parameter '" + parameters[i].getName() + "'");
                            return;
                        }
                        if (!parameters[i].getName().equals(paramName)) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.setContentType("text/plain; charset=UTF-8");
                            resp.getWriter().println("@RequestParam name mismatch: expected Java parameter name '" + parameters[i].getName() + "' to equal annotation value '" + paramName + "'");
                            return;
                        }
                        String raw = req.getParameter(paramName);
                        if (raw == null) {
                            if (rp.required()) {
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                resp.setContentType("text/plain; charset=UTF-8");
                                resp.getWriter().println("Missing required parameter: " + paramName);
                                return;
                            } else {
                                raw = rp.defaultValue();
                            }
                        }
                        args[i] = convertSimple(raw, type);
                        continue;
                    }

                    // If not annotated, do not bind implicitly (strict mode)

                    // Otherwise leave null (unsupported type without binder)
                    args[i] = null;
                }

                Object result = method.invoke(instance, args);

                // Si la méthode retourne un ModelAndView, forward vers la vue
                if (result instanceof ModelAndView) {
                    ConfigLoader cfg = new ConfigLoader();
                    String prefix = cfg.getViewPrefix();
                    String suffix = cfg.getViewSuffix();
                    ModelAndView mv = (ModelAndView) result;
                    String viewPath = prefix + mv.getViewName() + suffix;

                    // Attacher le modèle sur la requête
                    for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                    dispatcher.forward(req, resp);
                    return;
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<html><head><meta charset='UTF-8'><title>Résultat</title>"
                        + "<style>body{font-family:Arial, sans-serif;padding:24px} code{background:#f5f5f5;padding:2px 4px;border-radius:4px}</style>"
                        + "</head><body>");
                out.println("<h2>Mapping trouvé</h2>");
                out.println("<ul>");
                out.println("  <li>Classe: <code>" + controller.getSimpleName() + "</code></li>");
                out.println("  <li>Méthode: <code>" + mapping.getMethod().getName() + "</code></li>");
                out.println("</ul>");
                out.println("<h3>Résultat</h3>");
                out.println("<div>" + String.valueOf(result) + "</div>");
                out.println("</body></html>");
                return;
            } catch (Exception e) {
                // En cas d'erreur d'invocation, renvoyer 500
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("text/plain; charset=UTF-8");
                resp.getWriter().println("Erreur lors de l'invocation du contrôleur: " + e.getMessage());
                return;
            }
        }

        // Aucun mapping par annotations: essayer une convention simple
        // Convention: /{section}/{method} -> {basePackage}.{section}.AdminController#{method}()
        try {
            String path = resourcePath;
            if (path.startsWith("/")) path = path.substring(1);
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                String section = parts[0];
                String methodName = parts[1];

                ConfigLoader loader = new ConfigLoader();
                String basePackage = loader.getBasePackage();
                if (basePackage != null && !basePackage.isEmpty()) {
                    String controllerFqn = basePackage + "." + section + ".AdminController";
                    Class<?> controllerClazz = Class.forName(controllerFqn);
                    Object instance = controllerClazz.getDeclaredConstructor().newInstance();
                    Object result = MethodInvoker.execute(instance, methodName, new Class[] {}, new Object[] {});

                    // Gestion ModelAndView en conventionnel également
                    if (result instanceof ModelAndView) {
                        ConfigLoader cfg = new ConfigLoader();
                        String prefix = cfg.getViewPrefix();
                        String suffix = cfg.getViewSuffix();
                        ModelAndView mv = (ModelAndView) result;
                        String viewPath = prefix + mv.getViewName() + suffix;

                        for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }

                        RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                        dispatcher.forward(req, resp);
                        return;
                    }

                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("text/html; charset=UTF-8");
                    PrintWriter out = resp.getWriter();
                    out.println("<html><head><meta charset='UTF-8'><title>Résultat</title>"
                            + "<style>body{font-family:Arial, sans-serif;padding:24px} code{background:#f5f5f5;padding:2px 4px;border-radius:4px}</style>"
                            + "</head><body>");
                    out.println("<h2>Convention mapping</h2>");
                    out.println("<ul>");
                    out.println("  <li>Classe: <code>" + controllerClazz.getSimpleName() + "</code></li>");
                    out.println("  <li>Méthode: <code>" + methodName + "</code></li>");
                    out.println("</ul>");
                    out.println("<h3>Résultat</h3>");
                    out.println("<div>" + String.valueOf(result) + "</div>");
                    out.println("</body></html>");
                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            // Ignorer, on tombera en 404
        } catch (NoSuchMethodException e) {
            // Méthode non trouvée -> 404
        } catch (RuntimeException e) {
            // Erreur d'invocation (ex: méthode inexistante) -> considérer comme non trouvé et tomber en 404
        } catch (Throwable t) {
            t.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Erreur lors de la résolution conventionnelle: " + t.getMessage());
            return;
        }

        // Toujours rien: renvoyer un 404 propre
        System.out.println("FrontServlet: aucun mapping trouvé pour " + resourcePath);
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><head><meta charset='UTF-8'><title>404 - Non trouvé</title>"
                + "<style>body{font-family:Arial, sans-serif;padding:32px;color:#333} h1{color:#b00020}</style>"
                + "</head><body>");
        out.println("<h1>404 - Ressource non trouvée</h1>");
        out.println("<p>La ressource demandée n'a pas été trouvée.</p>");
        out.println("</body></html>");
    }

    private Object convertSimple(String raw, Class<?> type) {
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
