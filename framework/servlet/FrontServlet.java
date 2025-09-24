package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class FrontServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
       
        String urlPath = req.getRequestURI();
    
        System.out.println("Vous essayez d'acceder a : " + urlPath);
    
        resp.setContentType("text/html");
        resp.getWriter().write("<h1>Vous essayez d'acceder a : " + urlPath + "</h1>");
    }
}
