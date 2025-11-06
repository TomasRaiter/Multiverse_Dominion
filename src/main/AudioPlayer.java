package main;

import javax.sound.sampled.*;
import java.net.URL;

public class AudioPlayer {

    private static javax.sound.sampled.AudioInputStream loadStream(String path) throws Exception {
        // 1) Classpath
        java.net.URL url = AudioPlayer.class.getResource("/resources/" + path);
        if (url != null) {
            return javax.sound.sampled.AudioSystem.getAudioInputStream(url);
        }
        // 2) bin/resources
        java.io.File fBin = new java.io.File("bin/resources/" + path);
        if (fBin.exists()) {
            return javax.sound.sampled.AudioSystem.getAudioInputStream(fBin);
        }
        // 3) src/resources
        java.io.File fSrc = new java.io.File("src/resources/" + path);
        if (fSrc.exists()) {
            return javax.sound.sampled.AudioSystem.getAudioInputStream(fSrc);
        }
        throw new java.io.FileNotFoundException("Audio no encontrado: " + path);
    }

    private static void openClipFromStream(javax.sound.sampled.Clip clip, javax.sound.sampled.AudioInputStream ais) throws Exception {
        javax.sound.sampled.AudioFormat format = ais.getFormat();
        // Evitar preasignación gigante: leer incrementalmente
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int read;
        while ((read = ais.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        byte[] audioBytes = baos.toByteArray();
        clip.open(format, audioBytes, 0, audioBytes.length);
    }

    public static synchronized javax.sound.sampled.Clip playOnce(String path) {
        try {
            javax.sound.sampled.AudioInputStream ais = loadStream(path);
            javax.sound.sampled.AudioFormat baseFormat = ais.getFormat();
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            try {
                // Si no es PCM, convertir a PCM firmado 16-bit
                javax.sound.sampled.AudioInputStream dais;
                boolean needsPcm16 = baseFormat.getEncoding() != javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
                        || baseFormat.getSampleSizeInBits() != 16;
                if (needsPcm16) {
                    javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                            javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                            baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                    dais = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, ais);
                } else {
                    dais = ais;
                }
                // Abrir leyendo todo el stream para evitar "Audio data < 0"
                openClipFromStream(clip, dais);
            } catch (Exception convEx) {
                System.err.println("[AudioPlayer] Error abriendo clip (once): " + convEx.getMessage() + " BaseFormat=" + baseFormat);
                throw convEx;
            }
            clip.start();
            return clip;
        } catch (Exception e) {
            System.err.println("Error reproduciendo audio (once): " + path);
            try { e.printStackTrace(); } catch (Exception ignored) {}
            return null;
        }
    }

    public static synchronized javax.sound.sampled.Clip playLoop(String path) {
        try {
            javax.sound.sampled.AudioInputStream ais = loadStream(path);
            javax.sound.sampled.AudioFormat baseFormat = ais.getFormat();
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            try {
                javax.sound.sampled.AudioInputStream dais;
                boolean needsPcm16 = baseFormat.getEncoding() != javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
                        || baseFormat.getSampleSizeInBits() != 16;
                if (needsPcm16) {
                    javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                            javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                            baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                    dais = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, ais);
                } else {
                    dais = ais;
                }
                openClipFromStream(clip, dais);
            } catch (Exception convEx) {
                System.err.println("[AudioPlayer] Error abriendo clip (loop): " + convEx.getMessage() + " BaseFormat=" + baseFormat);
                throw convEx;
            }
            clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
            return clip;
        } catch (Exception e) {
            System.err.println("Error reproduciendo audio (loop): " + path);
            try { e.printStackTrace(); } catch (Exception ignored) {}
            return null;
        }
    }

    // Versiones no bloqueantes: cargan y reproducen en un hilo aparte y notifican el Clip cuando está listo
    public static void playOnceAsync(String path, java.util.function.Consumer<javax.sound.sampled.Clip> onReady) {
        new Thread(() -> {
            javax.sound.sampled.Clip clip = playOnce(path);
            try {
                if (onReady != null) onReady.accept(clip);
            } catch (Exception ignored) {}
        }, "AudioPlayer-once-" + path).start();
    }

    public static void playLoopAsync(String path, java.util.function.Consumer<javax.sound.sampled.Clip> onReady) {
        new Thread(() -> {
            javax.sound.sampled.Clip clip = playLoop(path);
            try {
                if (onReady != null) onReady.accept(clip);
            } catch (Exception ignored) {}
        }, "AudioPlayer-loop-" + path).start();
    }

    public static void stop(Clip clip) {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {}
        }
    }
}