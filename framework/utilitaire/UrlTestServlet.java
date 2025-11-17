package framework.utilitaire;

import framework.annotation.AnnotationReader;
import framework.utilitaire.MappingInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet pour tester la recherche d'URLs et leurs mappings
 */
@WebServlet("/testUrl")
public class UrlTestServlet extends HttpServlet {
    
    @Override
    public void init() throws ServletException {
        super.init();
        // Initialiser le système de mapping au démarrage du servlet
        System.out.println("=== Initialisation du servlet de test d'URL ===");
        AnnotationReader.init();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Permettre le test direct via l'URL: /testUrl?url=/chemin/a/tester
        String url = request.getParameter("url");

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><meta charset='UTF-8'><title>Test d'URL</title>"
                + "<style>body{font-family:Arial, sans-serif; padding:16px} .ok{color:#0a7b1a} .ko{color:#b00020} code{background:#f5f5f5;padding:2px 4px;border-radius:4px}</style>"
                + "</head><body>");

        out.println("<h2>Test d'URL (sans page JSP)</h2>");

        if (url == null || url.trim().isEmpty()) {
            out.println("<p>Indiquez un paramètre <code>url</code> dans la barre d'adresse pour tester un mapping.</p>");
            out.println("<p>Exemple: <code>" + request.getContextPath() + "/testUrl?url=/hello</code></p>");
            out.println("</body></html>");
            return;
        }

        MappingInfo mapping = AnnotationReader.findMappingByUrl(url);

        out.println("<p>Recherche du mapping pour: <code>" + url + "</code></p>");
        if (mapping.isFound()) {
            out.println("<p class='ok'><strong>Trouvé</strong></p>");
            out.println("<ul>");
            out.println("  <li>Classe: <code>" + mapping.getClassName() + "</code></li>");
            out.println("  <li>Méthode: <code>" + mapping.getMethodName() + "</code></li>");
            out.println("</ul>");
        } else {
            out.println("<p class='ko'><strong>Aucun mapping trouvé</strong></p>");
        }

        out.println("</body></html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Support POST en délégant au GET (même logique de réponse directe)
        doGet(request, response);
    }
}
