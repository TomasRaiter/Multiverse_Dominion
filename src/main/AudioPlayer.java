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

    public static synchronized javax.sound.sampled.Clip playOnce(String path) {
        try {
            javax.sound.sampled.AudioInputStream ais = loadStream(path);
            javax.sound.sampled.AudioFormat baseFormat = ais.getFormat();
            javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            javax.sound.sampled.AudioInputStream dais = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, ais);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(dais);
            clip.start();
            return clip;
        } catch (Exception e) {
            System.err.println("Error reproduciendo audio (once): " + path);
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized javax.sound.sampled.Clip playLoop(String path) {
        try {
            javax.sound.sampled.AudioInputStream ais = loadStream(path);
            javax.sound.sampled.AudioFormat baseFormat = ais.getFormat();
            javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            javax.sound.sampled.AudioInputStream dais = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, ais);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(dais);
            clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
            return clip;
        } catch (Exception e) {
            System.err.println("Error reproduciendo audio (loop): " + path);
            e.printStackTrace();
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