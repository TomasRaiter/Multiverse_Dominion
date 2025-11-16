package main;

import java.awt.Image;
import javax.swing.ImageIcon;

// Clase para cargar y manejar sprites (imágenes y GIFs) del juego
public class Sprite {
    // Datos del sprite
    private Image imagen;                    // Imagen cargada
    private javax.swing.ImageIcon icon;     // Icono de Swing para animaciones
    private boolean esGif;                   // Si es un archivo GIF animado

    // Constructor: carga un sprite desde la ruta especificada
    public Sprite(String path) {
        // Validar ruta de entrada
        if (path == null || path.trim().isEmpty()) {
            System.err.println("[Sprite] Path is null or empty");
            this.icon = null;
            this.imagen = null;
            this.esGif = false;
            return;
        }

        // Intentar cargar el sprite
        try {
            // Intentar cargar desde classpath primero
            java.net.URL url = getClass().getResource("/resources/" + path);
            ImageIcon icon = null;
            if (url != null) {
                icon = new ImageIcon(url);
                System.out.println("[Sprite] Cargado por classpath: " + path);
            } else {
                // Fallback: cargar desde src/resources
                java.io.File fSrc = new java.io.File("src/resources/" + path);
                if (fSrc.exists()) {
                    icon = new ImageIcon(fSrc.toURI().toURL());
                    System.out.println("[Sprite] Cargado desde src/resources: " + path);
                } else {
                    System.err.println("[Sprite] Recurso no encontrado: " + path);
                }
            }
            // Asignar datos del sprite
            this.icon = icon;
            this.imagen = icon != null ? icon.getImage() : null;
            this.esGif = path.toLowerCase().endsWith(".gif");
        } catch (Exception ex) {
            // Manejar errores de carga
            System.err.println("[Sprite] Error cargando: " + path + " -> " + ex.getMessage());
            this.icon = null;
            this.imagen = null;
            this.esGif = false;
        }
    }

    // Obtiene la imagen del sprite
    public Image getImagen() {
        return imagen;
    }

    // Obtiene el icono de Swing (útil para GIFs animados)
    public javax.swing.ImageIcon getIcon() {
        return icon;
    }

    // Verifica si el sprite es un GIF animado
    public boolean isGif() {
        return esGif;
    }
}

