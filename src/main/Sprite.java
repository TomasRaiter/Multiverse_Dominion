package main;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Sprite {
    private Image imagen;
    private javax.swing.ImageIcon icon;
    private boolean esGif;

    public Sprite(String path) {
        try {
            java.net.URL url = getClass().getResource("/resources/" + path);
            ImageIcon icon = null;
            if (url != null) {
                icon = new ImageIcon(url);
                System.out.println("[Sprite] Cargado por classpath: " + path);
            } else {
                // Fallback 1: bin/resources cuando se ejecuta con compilados
                java.io.File fBin = new java.io.File("bin/resources/" + path);
                if (fBin.exists()) {
                    icon = new ImageIcon(fBin.toURI().toURL());
                    System.out.println("[Sprite] Cargado desde bin/resources: " + path);
                } else {
                    // Fallback 2: src/resources cuando se ejecuta desde IDE
                    java.io.File fSrc = new java.io.File("src/resources/" + path);
                    if (fSrc.exists()) {
                        icon = new ImageIcon(fSrc.toURI().toURL());
                        System.out.println("[Sprite] Cargado desde src/resources: " + path);
                    } else {
                        System.err.println("[Sprite] Recurso no encontrado: " + path);
                    }
                }
            }
            this.icon = icon;
            this.imagen = icon != null ? icon.getImage() : null;
            this.esGif = path != null && path.toLowerCase().endsWith(".gif");
        } catch (Exception ex) {
            System.err.println("[Sprite] Error cargando: " + path + " -> " + ex.getMessage());
            this.icon = null;
            this.imagen = null;
            this.esGif = false;
        }
    }

    public Image getImagen() {
        return imagen;
    }

    public javax.swing.ImageIcon getIcon() {
        return icon;
    }

    public boolean isGif() {
        return esGif;
    }
}
