package main;

public class DebugPika {
    public static void main(String[] args) {
        String base = "Pokemon/Pikachu/images/";
        String[] paths = new String[]{
                base + "pikachu_idle.png",
                base + "pikachu_caminando.gif",
                base + "pikachu_idle.png", // crouch reuse
                base + "pikachu_atacando.png",
                base + "pikachu_daño.png",
                null, // ko not available
                base + "pikachu_idle.png" // jump reuse
        };
        String[] labels = new String[]{"idle","walk","crouch","attack","hurt","ko","jump"};
        for (int i = 0; i < paths.length; i++) {
            String p = paths[i];
            Sprite s = p != null ? new Sprite(p) : null;
            boolean loaded = s != null && (s.getIcon() != null || s.getImagen() != null);
            System.out.println("[Pikachu Debug] " + labels[i] + " path=" + p + " loaded=" + loaded);
        }
        // Audios
        String sounds = "Pokemon/Pikachu/sounds/";
        String[] audios = new String[]{
                sounds + "Pikachu_Comienzo.wav",
                sounds + "Pikachu_GameOver.wav",
                sounds + "Golpe.wav",
                sounds + "Daño.wav"
        };
        for (String a : audios) {
            java.net.URL url = DebugPika.class.getResource("/resources/" + a);
            boolean ok = false;
            if (url != null) ok = true; else {
                java.io.File fBin = new java.io.File("bin/resources/" + a);
                java.io.File fSrc = new java.io.File("src/resources/" + a);
                ok = fBin.exists() || fSrc.exists();
            }
            System.out.println("[Pikachu Debug] audio=" + a + " exists=" + ok);
        }
    }
}