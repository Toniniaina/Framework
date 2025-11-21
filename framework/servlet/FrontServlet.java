package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import framework.annotation.AnnotationReader;
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
                Object result = mapping.getMethod().invoke(instance);

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
}
