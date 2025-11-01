package framework.utilitaire;

import framework.annotation.Controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsable de la découverte et du scan des classes dans un package
 * Principe de Responsabilité Unique (SRP)
 */
public class ClassScanner {
    
    /**
     * Découvre toutes les classes avec @Controller dans un package et ses sous-packages
     * @param packageName Le package de base à scanner
     * @return Liste des classes trouvées
     */
    public List<Class<?>> scanPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        
        try {
            String path = packageName.replace('.', '/');
            // Utiliser le Context ClassLoader (serveur d'app) pour voir WEB-INF/classes
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL resource = cl.getResource(path);
            
            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists() && directory.isDirectory()) {
                    scanDirectory(directory, packageName, classes);
                }
            } else {
                System.out.println("Aucune ressource trouvée pour le package: " + packageName + " (path=" + path + ")");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la découverte des classes: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * Scanne récursivement un répertoire pour trouver toutes les classes avec @Controller
     * @param directory Le répertoire à scanner
     * @param packageName Le nom du package correspondant
     * @param classes La liste pour stocker les classes trouvées
     */
    private void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Scanner récursivement les sous-répertoires (sous-packages)
                String subPackage = packageName + "." + file.getName();
                scanDirectory(file, subPackage, classes);
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                // Charger la classe
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(packageName + "." + className);
                    // Filtrer uniquement les classes avec @Controller
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Impossible de charger la classe: " + packageName + "." + className);
                } catch (NoClassDefFoundError e) {
                    // Ignorer les erreurs de classes internes ou dépendances manquantes
                }
            }
        }
    }
}
