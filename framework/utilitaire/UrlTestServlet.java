package framework.utilitaire;

import framework.annotation.AnnotationReader;
import framework.utilitaire.MappingInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
        // Afficher la page de test
        request.getRequestDispatcher("/test-url.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Récupérer l'URL à rechercher
        String url = request.getParameter("url");
        
        if (url == null || url.trim().isEmpty()) {
            request.setAttribute("result", "error");
            request.setAttribute("found", false);
            request.setAttribute("searchUrl", "");
            request.getRequestDispatcher("/test-url.jsp").forward(request, response);
            return;
        }
        
        // Rechercher le mapping
        MappingInfo mapping = AnnotationReader.findMappingByUrl(url);
        
        // Préparer les attributs pour le JSP
        request.setAttribute("searchUrl", url);
        request.setAttribute("found", mapping.isFound());
        
        if (mapping.isFound()) {
            request.setAttribute("result", "success");
            request.setAttribute("className", mapping.getClassName());
            request.setAttribute("methodName", mapping.getMethodName());
        } else {
            request.setAttribute("result", "error");
        }
        
        // Retourner à la page JSP avec les résultats
        request.getRequestDispatcher("/test-url.jsp").forward(request, response);
    }
}
