package main;

import javax.sound.sampled.*;

// clase para manejar la reproduccion de audio en el juego
public class AudioPlayer {

    // carga un archivo de audio desde el classpath o el sistema de archivos
    private static javax.sound.sampled.AudioInputStream loadStream(String path) throws Exception {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("la ruta del audio no puede estar vacia");
        }

        // intenta cargar desde el classpath primero
        java.net.URL url = AudioPlayer.class.getResource("/resources/" + path);
        if (url != null) {
            return javax.sound.sampled.AudioSystem.getAudioInputStream(url);
        }

        // si no se encuentra, busca en src/resources
        java.io.File fSrc = new java.io.File("src/resources/" + path);
        if (fSrc.exists()) {
            return javax.sound.sampled.AudioSystem.getAudioInputStream(fSrc);
        }
        throw new java.io.FileNotFoundException("audio no encontrado: " + path);
    }

    // abre un clip de audio cargando todo en memoria
    private static void openClipFromStream(javax.sound.sampled.Clip clip, 
            javax.sound.sampled.AudioInputStream ais) throws Exception {
        javax.sound.sampled.AudioFormat format = ais.getFormat();

        // lee todo el audio en memoria para evitar problemas
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int read;
        while ((read = ais.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        byte[] audioBytes = baos.toByteArray();
        clip.open(format, audioBytes, 0, audioBytes.length);
    }

    // reproduce un sonido una sola vez
    public static synchronized javax.sound.sampled.Clip playOnce(String path) {
        try {
            javax.sound.sampled.AudioInputStream ais = loadStream(path);
            javax.sound.sampled.AudioFormat baseFormat = ais.getFormat();
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            try {
                // convierte a pcm 16-bit si es necesario
                javax.sound.sampled.AudioInputStream dais;
                boolean needsPcm16 = baseFormat.getEncoding() != 
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
                    || baseFormat.getSampleSizeInBits() != 16;
                if (needsPcm16) {
                    javax.sound.sampled.AudioFormat decodedFormat = 
                        new javax.sound.sampled.AudioFormat(
                            javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                            baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                    dais = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, ais);
                } else {
                    dais = ais;
                }

                openClipFromStream(clip, dais);
            } catch (Exception convEx) {
                System.err.println("[audioplayer] error al abrir clip: " + 
                    convEx.getMessage() + " formato=" + baseFormat);
                throw convEx;
            }
            clip.start();
            return clip;
        } catch (Exception e) {
            System.err.println("error al reproducir audio: " + path);
            try { e.printStackTrace(); } catch (Exception ignored) {}
            return null;
        }
    }

    // reproduce un sonido en bucle continuo
    public static synchronized javax.sound.sampled.Clip playLoop(String path) {
        try {
            javax.sound.sampled.AudioInputStream ais = loadStream(path);
            javax.sound.sampled.AudioFormat baseFormat = ais.getFormat();
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            try {
                // convierte a pcm 16-bit si es necesario
                javax.sound.sampled.AudioInputStream dais;
                boolean needsPcm16 = baseFormat.getEncoding() != 
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
                    || baseFormat.getSampleSizeInBits() != 16;
                if (needsPcm16) {
                    javax.sound.sampled.AudioFormat decodedFormat = 
                        new javax.sound.sampled.AudioFormat(
                            javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                            baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                    dais = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, ais);
                } else {
                    dais = ais;
                }
                openClipFromStream(clip, dais);
            } catch (Exception convEx) {
                System.err.println("[audioplayer] error al abrir clip (bucle): " + 
                    convEx.getMessage() + " formato=" + baseFormat);
                throw convEx;
            }
            clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
            return clip;
        } catch (Exception e) {
            System.err.println("error al reproducir audio en bucle: " + path);
            try { e.printStackTrace(); } catch (Exception ignored) {}
            return null;
        }
    }

    // version asincrona de playOnce
    public static void playOnceAsync(String path, 
            java.util.function.Consumer<javax.sound.sampled.Clip> onReady) {
        new Thread(() -> {
            javax.sound.sampled.Clip clip = playOnce(path);
            try {
                if (onReady != null) onReady.accept(clip);
            } catch (Exception ignored) {}
        }, "audioplayer-once-" + path).start();
    }

    // version asincrona de playLoop
    public static void playLoopAsync(String path, 
            java.util.function.Consumer<javax.sound.sampled.Clip> onReady) {
        new Thread(() -> {
            javax.sound.sampled.Clip clip = playLoop(path);
            try {
                if (onReady != null) onReady.accept(clip);
            } catch (Exception ignored) {}
        }, "audioplayer-loop-" + path).start();
    }

    // detiene la reproduccion de un clip
    public static void stop(Clip clip) {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {}
        }
    }
}