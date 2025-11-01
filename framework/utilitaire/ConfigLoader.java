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
            } else {
                System.out.println("ERREUR: Fichier config.properties introuvable!");
                basePackage = "com.testframework"; // Valeur par défaut
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du config.properties: " + e.getMessage());
            basePackage = "com.testframework"; // Valeur par défaut
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
}
