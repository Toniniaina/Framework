package framework.utilitaire;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Responsable du chargement de la configuration depuis config.properties
 * Principe de Responsabilité Unique (SRP)
 */
public class ConfigLoader {
    
    private String basePackage;
    private String viewPrefix;
    private String viewSuffix;
    
    /**
     * Charge le package de base depuis le fichier config.properties
     */
    public void loadConfiguration() {
        if (basePackage != null) {
            return;
        }
        
        Properties props = new Properties();
        InputStream input = null;
        
        try {
            // Essayer de charger depuis le classpath
            input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties");
            
            if (input == null) {
                // Essayer de charger depuis le répertoire testFramework/resources
                input = new FileInputStream("testFramework/resources/config.properties");
            }
            
            if (input != null) {
                props.load(input);
                basePackage = props.getProperty("base.package");
                if (basePackage != null) {
                    basePackage = basePackage.trim();
                    System.out.println("Package de base chargé depuis config.properties: " + basePackage);
                }
                viewPrefix = propOrDefault(props, "view.prefix", "/WEB-INF/views/");
                viewSuffix = propOrDefault(props, "view.suffix", ".jsp");
            } else {
                System.out.println("ERREUR: Fichier config.properties introuvable!");
                basePackage = "com.testframework"; // Valeur par défaut
                viewPrefix = "/WEB-INF/views/";
                viewSuffix = ".jsp";
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du config.properties: " + e.getMessage());
            basePackage = "com.testframework"; // Valeur par défaut
            viewPrefix = "/WEB-INF/views/";
            viewSuffix = ".jsp";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    // Ignorer
                }
            }
        }
    }
    
    public String getBasePackage() {
        if (basePackage == null) {
            loadConfiguration();
        }
        return basePackage;
    }

    public String getViewPrefix() {
        if (viewPrefix == null) {
            loadConfiguration();
        }
        return viewPrefix;
    }

    public String getViewSuffix() {
        if (viewSuffix == null) {
            loadConfiguration();
        }
        return viewSuffix;
    }

    private String propOrDefault(Properties p, String key, String defVal) {
        String v = p.getProperty(key);
        return v != null ? v.trim() : defVal;
    }
}
