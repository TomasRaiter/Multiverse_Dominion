package main;

import java.awt.*;
import javax.swing.*;
import javax.sound.sampled.Clip;

public class Jugador {
    private int x, y;
    private int width, height;
    // Altura actual para estados (de pie vs agachado)
    private int alturaActual;
    private int velX, velY; // velocidad en X e Y
    private int fuerzaSalto = 20; // altura del salto
    private int gravedad = 2; // gravedad
    private boolean enSuelo = true;
    private boolean agachado = false;
    private int ajusteSuelo = 0;

    private int vida = 100;
    private int vidaMax = 100;
    private String nombre;
    private Color color;
    public boolean atacando = false;
    private Sprite spriteIdle, spriteAttack, spriteAttackAlt;
    // Sprites adicionales
    private Sprite spriteWalk, spriteCrouch, spriteJump, spriteHurt, spriteKO;
    private Sprite spriteActual;
    private int duracionAtaque = 19; // 25% más lenta
    private int cooldownAtaque = 0; // sin cooldown entre ataques
    private int contadorCooldown = 0;
    private int contadorAtaque = 0;
    // Aplicar daño una sola vez por ataque
    private boolean golpeAplicado = false;
    private int cooldownSalto = 60; // frames de cooldown entre saltos
    private int contadorCooldownSalto = 0;

    // Audio por personaje
    private String audioComienzo, audioRespiracion, audioDano, audioGameOver;
    private String audioSaltoAtaque; // compatibilidad
    private String audioSalto;
    private String audioAtaque;
    private Clip clipRespiracion;
    private Clip clipInicio;
    private Clip clipGameOver;
    private String personajeId;
    private boolean alternarAtaques = false;
    private boolean usarAttackAltNext = false;
    // Control especial de límites y escala visual
    private boolean ignorarLimitesHorizontales = false;
    private int alturaVisualOverride = -1; // si >0, usar en dibujar en lugar de 300
    private int drawYOffset = 0; // ajuste vertical del dibujo (px en base)
    
    // Control de animación de derrota
    private boolean enAnimacionDerrota = false;
    private int framesAnimacionDerrota = 0;
    private int duracionDerrotaFrames = 120; // configurable por personaje, mínimo 30
    
    // Control de animación de daño
    private int contadorHurtFrames = 0;
    private int duracionHurtFrames = 20;


    public Jugador(String nombre, int x, int y, Color color) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 100;
        this.alturaActual = this.height;
        this.color = color;

        this.spriteIdle = null;
        this.spriteAttack = null;
        this.spriteActual = null;
    }

    // Configurar sprites y audios del personaje seleccionado
    public void setAssets(
            String personajeId,
            Sprite idle,
            Sprite walk,
            Sprite crouch,
            Sprite attack,
            Sprite hurt,
            Sprite ko,
            Sprite jump,
            String audioComienzo,
            String audioRespiracion,
            String audioDano,
            String audioGameOver,
            String audioSaltoAtaque,
            String audioSalto,
            String audioAtaque
    ) {
        this.personajeId = personajeId;
        if (idle != null) this.spriteIdle = idle;
        if (walk != null) this.spriteWalk = walk;
        if (crouch != null) this.spriteCrouch = crouch;
        if (attack != null) this.spriteAttack = attack;
        if (hurt != null) this.spriteHurt = hurt;
        if (ko != null) this.spriteKO = ko;
        if (jump != null) this.spriteJump = jump;
        this.spriteActual = this.spriteIdle;
        this.audioComienzo = audioComienzo;
        this.audioRespiracion = audioRespiracion;
        this.audioDano = audioDano;
        this.audioGameOver = audioGameOver;
        this.audioSaltoAtaque = audioSaltoAtaque;
        this.audioSalto = audioSalto;
        this.audioAtaque = audioAtaque;
        // Alternar ataques sólo para Ash
        this.alternarAtaques = "J1".equals(nombre) || "J2".equals(nombre); // default enable, refine in aplicarPersonaje
        this.usarAttackAltNext = false;
    }

    public void moverIzquierda() {
        // Bloquear movimiento si está agachado
        if (agachado) { velX = 0; return; }
        velX = -7;
        if (spriteWalk != null && !atacando && !enAnimacionDerrota) spriteActual = spriteWalk;
    }
    public void moverDerecha() {
        // Bloquear movimiento si está agachado
        if (agachado) { velX = 0; return; }
        velX = 7;
        if (spriteWalk != null && !atacando && !enAnimacionDerrota) spriteActual = spriteWalk;
    }
    public void detener() { velX = 0; if (!atacando && !enAnimacionDerrota) spriteActual = agachado && spriteCrouch != null ? spriteCrouch : spriteIdle; }

    public void setAgachado(boolean a) {
        this.agachado = a;
        if (agachado) {
            alturaActual = (int)(height * 0.6);
            // Detener movimiento al agacharse
            velX = 0;
            if (!atacando && spriteCrouch != null) spriteActual = spriteCrouch;
            // Ajustar tamaño visual al agacharse para Ash y Darth Vader
            if ("Ash".equals(getPersonajeId())) {
                setAlturaVisualOverride(240);
            } else if ("Darth_Vader".equals(getPersonajeId())) {
                setAlturaVisualOverride(330);
            }
        } else {
            alturaActual = height;
            if (!atacando && !enAnimacionDerrota) spriteActual = spriteIdle;
            clearAlturaVisualOverride();
        }
    }

    public void saltar() {
        // Bloquear salto si está agachado
        if (agachado) { return; }
        if (enSuelo && contadorCooldownSalto == 0) {
            velY = -fuerzaSalto;
            enSuelo = false;
            if (spriteJump != null) spriteActual = spriteJump;
            if (audioSalto != null) {
                AudioPlayer.playOnce(audioSalto);
            } else if (audioSaltoAtaque != null) {
                AudioPlayer.playOnce(audioSaltoAtaque);
            }
            contadorCooldownSalto = cooldownSalto;
        }
    }

    public void atacar() {
        // Bloquear ataque si está agachado
        if (agachado) { return; }
        if (contadorAtaque == 0 && contadorCooldown == 0) {
            atacando = true;
            contadorAtaque = duracionAtaque;
            golpeAplicado = false;
            // Seleccionar sprite de ataque
            if (alternarAtaques && spriteAttackAlt != null && "Ash".equals(getPersonajeId())) {
                spriteActual = usarAttackAltNext ? spriteAttackAlt : spriteAttack;
                usarAttackAltNext = !usarAttackAltNext;
            } else {
                spriteActual = spriteAttack;
            }
            if (audioAtaque != null) {
                AudioPlayer.playOnce(audioAtaque);
            } else if (audioSaltoAtaque != null) {
                AudioPlayer.playOnce(audioSaltoAtaque);
            }
        }
    }

    public void actualizar() {
        // Control de animación de derrota
        if (enAnimacionDerrota) {
            framesAnimacionDerrota++;
            if (framesAnimacionDerrota >= duracionDerrotaFrames) {
                enAnimacionDerrota = false;
                if (spriteIdle != null) spriteActual = spriteIdle;
            }
        }

        // Movimiento horizontal
        x += velX;
        // Límites horizontales (baseWidth = 800) usando ancho de caminar si no se ignoran
        if (!ignorarLimitesHorizontales) {
            int anchoVisual = obtenerAnchoVisualWalk();
            if (x < 0) x = 0;
            if (x > 800 - anchoVisual) x = 800 - anchoVisual;
        }

        // Movimiento vertical (salto y gravedad)
        y += velY;
        if (!enSuelo) velY += gravedad;

        // Limite superior de la pantalla usando alturaActual
        if (y - alturaActual < 0) {
            y = alturaActual;
            velY = 0;
        }

        // Limite del suelo (baseHeight = 600 -> suelo en 500)
        if (y >= 500) {
            y = 500;
            velY = 0;
            enSuelo = true;
            // Mantener spriteWalk mientras hay movimiento horizontal
            if (!agachado && !atacando && !enAnimacionDerrota) {
                if (velX != 0 && spriteWalk != null) {
                    spriteActual = spriteWalk;
                } else if (spriteIdle != null) {
                    spriteActual = spriteIdle;
                }
            }
        }

        // Contador de ataque
        if (contadorAtaque > 0) {
            contadorAtaque--;
            if (contadorAtaque == 0) {
                atacando = false;
                golpeAplicado = false;
                if (!enAnimacionDerrota) spriteActual = spriteIdle;
                contadorCooldown = cooldownAtaque;
            }
        }

        // Control de animación de daño breve
        if (contadorHurtFrames > 0) {
            contadorHurtFrames--;
            if (contadorHurtFrames == 0 && !atacando && spriteIdle != null && !enAnimacionDerrota) {
                spriteActual = spriteIdle;
            }
        }

        if (contadorCooldown > 0) contadorCooldown--; 
        if (contadorCooldownSalto > 0) contadorCooldownSalto--; 
    }

    public void recibirDano(int cantidad) {
        vida -= cantidad;
        if (vida < 0) vida = 0;
        if (spriteHurt != null) spriteActual = spriteHurt;
        contadorHurtFrames = duracionHurtFrames;
        if (audioDano != null) AudioPlayer.playOnce(audioDano);
        // Derrota (Game_Over) sólo se dispara al final del match desde Juego
    }

    // Normalización de daño: utilidades para Juego
    public boolean haGolpeado() { return golpeAplicado; }
    public void marcarGolpeado() { golpeAplicado = true; }
    
    public void congelar() {
        velX = 0;
        velY = 0;
        atacando = false;
        // Forzar a considerar al jugador en suelo para evitar gravedad
        enSuelo = true;
    }
    public void setVida(int v) { vida = Math.max(0, Math.min(vidaMax, v)); }
    public void setVidaMax(int max) { this.vidaMax = Math.max(1, max); this.vida = Math.min(this.vida, this.vidaMax); }
    public int getVidaMax() { return this.vidaMax; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getX() { return this.x; }
    public int getY() { return this.y; }

    private boolean haSonadoAudioComienzo = false;
    public void onCountdownStart() {
        // Forzar idle y resetear estados de acción
        if (spriteIdle != null && !enAnimacionDerrota) spriteActual = spriteIdle;
        atacando = false;
        agachado = false;
        
        // Reproducir audio de comienzo solo si no ha sonado
        if (audioComienzo != null && !haSonadoAudioComienzo) {
            System.out.println("[DEBUG] " + personajeId + " reproduciendo audio comienzo: " + audioComienzo);
            // Reproducir de forma no bloqueante para no congelar el EDT
            AudioPlayer.playOnceAsync(audioComienzo, c -> clipInicio = c);
            haSonadoAudioComienzo = true; // Marcar que ya sonó
        } else if (haSonadoAudioComienzo) {
            System.out.println("[DEBUG] " + personajeId + " ya reprodujo su audio de comienzo.");
        } else {
            System.out.println("[DEBUG] " + personajeId + " no tiene audio de comienzo.");
        }
    }
    public void resetParaNuevaRonda() {
        haSonadoAudioComienzo = false;
        enAnimacionDerrota = false;
        framesAnimacionDerrota = 0;
        stopAllAudio();
        // Forzar estado idle al inicio de la ronda
        if (spriteIdle != null) {
            spriteActual = spriteIdle;
        }
    }

    public void onFightStart() {
        // Iniciar respiración/loop si existe
        if (audioRespiracion != null) {
            // detener anterior si existiera
            AudioPlayer.stop(clipRespiracion);
            // Reproducir en loop de forma no bloqueante
            AudioPlayer.playLoopAsync(audioRespiracion, c -> clipRespiracion = c);
        }
        // Volver a idle si no hay acciones
        if (!agachado && !atacando && spriteIdle != null) spriteActual = spriteIdle;
    }

    public void onGameOver() {
        // Parar respiración y mostrar Game_Over + audio
        AudioPlayer.stop(clipRespiracion);
        if (spriteKO != null) {
            spriteActual = spriteKO;
        } else if (spriteHurt != null) {
            spriteActual = spriteHurt;
        } else if (spriteIdle != null) {
            spriteActual = spriteIdle;
        }
        if (audioGameOver != null) clipGameOver = AudioPlayer.playOnce(audioGameOver);
        
        // Iniciar animación de derrota controlada
        enAnimacionDerrota = true;
        framesAnimacionDerrota = 0;
        
        congelar();
    }

    public void forceIdle() {
        // Forzar al personaje a mostrar idle (para el ganador al final del match)
        if (spriteIdle != null) spriteActual = spriteIdle;
        congelar();
    }

    public long getGameOverAudioLengthMicros() {
        try { return clipGameOver != null ? clipGameOver.getMicrosecondLength() : 0L; } catch (Exception ex) { return 0L; }
    }

    public long getInicioAudioLengthMicros() {
        try { return clipInicio != null ? clipInicio.getMicrosecondLength() : 0L; } catch (Exception ex) { return 0L; }
    }

    public void stopAllAudio() {
        AudioPlayer.stop(clipRespiracion);
        clipRespiracion = null;
        AudioPlayer.stop(clipGameOver);
        clipGameOver = null;
        AudioPlayer.stop(clipInicio);
        clipInicio = null;
    }

    public void dibujar(Graphics g, java.awt.Component observer, float scaleX, float scaleY) {
        int drawX = (int) (x * scaleX);
        
        
        // POSICIÓN VERTICAL FIJA - TODOS LOS PERSONAJES EN EL MISMO NIVEL DEL SUELO
        int sueloY = (int) (500 * scaleY);
        int drawY = sueloY; // Base en el suelo para todos
        
        
        // ALTURA VISUAL CONSISTENTE - MISMA ESCALA PARA TODOS LOS ESTADOS
        int alturaBase = 300; // Altura base fija para normalización
        int alturaVisualNormalizada = (int) (alturaBase * scaleY);
        
        int ajusteSuelo = 0;

        Sprite s = spriteActual;
        javax.swing.ImageIcon icon = (s != null) ? s.getIcon() : null;
        Image img = (s != null) ? s.getImagen() : null;

        // Obtener dimensiones del sprite actual
        int iconW = 0, iconH = 0;
        if (icon != null) {
            iconW = icon.getIconWidth();
            iconH = icon.getIconHeight();
        }
        double escala = (double) alturaVisualNormalizada / iconH;
        
        if ((iconW <= 0 || iconH <= 0) && img != null) {
            int imgW = img.getWidth(observer);
            int imgH = img.getHeight(observer);
            iconW = (imgW > 0) ? imgW : iconW;
            iconH = (imgH > 0) ? imgH : iconH;
        }

        // Si no tenemos dimensiones válidas, usar fallback
        if (iconW <= 0 || iconH <= 0) {
            iconW = 100;
            iconH = alturaBase;
        }

        

        
        if ("Darth_Vader".equals(personajeId)) {
            // Personalización Darth Vader
            if (s == spriteIdle) escala *= 0.98;
            else if (s == spriteWalk) escala *= 1;
            ajusteSuelo = 5; // 5px más abajo del suelo pene
        } 
        else if ("Ash".equals(personajeId)) {
            // Personalización Ash
            if (s == spriteIdle) escala *= 0.90;
            else if (s == spriteWalk) escala *= 0.90;
            else if (s == spriteAttack) escala *= 0.95;
            ajusteSuelo = -10; // 10px más arriba del suelo
        }
        else if ("Iron_Man".equals(personajeId)) {
            // Personalización Iron Man
            if (s == spriteIdle) escala *= 1.05;
            else if (s == spriteWalk) escala *= 1.02;
            ajusteSuelo = -5; // 5px más arriba del suelo
        }
        else if ("Goku".equals(personajeId)) {
            // Personalización Goku
            if (s == spriteIdle) escala *= 0.95;
            else if (s == spriteWalk) escala *= 0.98;
            ajusteSuelo = 0; // En el suelo normal
        }
        else if ("Mr_Increible".equals(personajeId)) {
            // Personalización Mr Increíble
            if (s == spriteIdle) escala *= 1.1;
            else if (s == spriteWalk) escala *= 1.08;
            else if (s == spriteCrouch) escala *= 0.85;
            ajusteSuelo = 8; // 8px más abajo del suelo
        }
        else if ("Batman".equals(personajeId)) {
            // Personalización Batman
            if (s == spriteIdle) escala *= 1.0;
            else if (s == spriteWalk) escala *= 1.0;
            ajusteSuelo = -3; // 3px más arriba del suelo
        }

        int anchoNormalizado = (int) (iconW * escala);

        if (icon != null && iconW > 0 && iconH > 0) {
            // Pre-render del frame para limpiar fondos sólidos
            java.awt.image.BufferedImage frame = new java.awt.image.BufferedImage(
                    Math.max(1, iconW), Math.max(1, iconH), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D gt = frame.createGraphics();
            icon.paintIcon(observer, gt, 0, 0);
            gt.dispose();
            
            // Detectar y hacer transparente colores de fondo sólidos
            int bg = frame.getRGB(0, 0);
            int br = (bg >> 16) & 0xFF, bgc = (bg >> 8) & 0xFF, bb = bg & 0xFF;
            boolean esAzulIntenso = bb > 200 && br < 40 && bgc < 40;
            boolean esVerdeIntenso = bgc > 200 && br < 40 && bb < 40;
            boolean esCianIntenso = bb > 200 && bgc > 200 && br < 40;
            if (esAzulIntenso || esVerdeIntenso || esCianIntenso) {
                int tol = 35;
                for (int yy = 0; yy < frame.getHeight(); yy++) {
                    for (int xx = 0; xx < frame.getWidth(); xx++) {
                        int p = frame.getRGB(xx, yy);
                        int r = (p >> 16) & 0xFF, gch = (p >> 8) & 0xFF, b = p & 0xFF;
                        boolean match = false;
                        if (esAzulIntenso) match = Math.abs(r) < tol && Math.abs(gch) < tol && b > 200;
                        if (esVerdeIntenso) match = Math.abs(r) < tol && Math.abs(b) < tol && gch > 200;
                        if (esCianIntenso) match = Math.abs(r) < tol && gch > 200 && b > 200;
                        if (match) {
                            frame.setRGB(xx, yy, 0x00000000);
                        }
                    }
                }
            }

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            boolean flip = "J2".equals(nombre);
            if (flip) {
                g2.translate(drawX + anchoNormalizado, drawY - alturaVisualNormalizada);
                g2.scale(-1, 1);
            } else {
                g2.translate(drawX, drawY - alturaVisualNormalizada);
            }

            g2.scale(escala, escala);
            g2.drawImage(frame, 0, 0, null);
            g2.dispose();
        } else if (img != null) {
            // Fallback con Image directo
            int imgH = img.getHeight(observer);
            int imgW = img.getWidth(observer);
            if (imgH <= 0) imgH = alturaVisualNormalizada;
            
            double escalaImg = (double) alturaVisualNormalizada / imgH;
            int anchoNormalizadoImg = (int) ((imgW > 0 ? imgW : alturaVisualNormalizada) * escalaImg);

            if ("J2".equals(nombre)) {
                g.drawImage(img, drawX + anchoNormalizadoImg, drawY - alturaVisualNormalizada, 
                           -anchoNormalizadoImg, alturaVisualNormalizada, observer);
            } else {
                g.drawImage(img, drawX, drawY - alturaVisualNormalizada, 
                           anchoNormalizadoImg, alturaVisualNormalizada, observer);
            }
        } else {
            // Fallback final: rectángulo de color
            int drawW = (int) (width * scaleX);
            int drawH = (int) (alturaActual * scaleY);
            g.setColor(color);
            if ("J2".equals(nombre)) {
                g.fillRect(drawX - drawW, drawY - drawH, drawW, drawH);
            } else {
                g.fillRect(drawX, drawY - drawH, drawW, drawH);
            }
        }
    }

    public void dibujar(Graphics g, float scaleX, float scaleY) {
        dibujar(g, null, scaleX, scaleY);
    }

    public Rectangle getBounds() {
        int anchoWalk = obtenerAnchoVisualWalk();
        int alturaBase = 300; // Usar altura base consistente
        return new Rectangle(x, y - alturaBase, Math.max(10, anchoWalk), alturaBase);
    }

    public String getNombre() { return nombre; }
    public int getVida() { return vida; }

    public void setDerrotaFrames(int frames) {
        this.duracionDerrotaFrames = Math.max(30, frames);
    }

    public void setAttackAlt(Sprite attackAlt) {
        this.spriteAttackAlt = attackAlt;
    }

    public String getPersonajeId() { return this.personajeId; }

    public void dibujar(Graphics g, java.awt.Component observer) {
        dibujar(g, observer, 1.0f, 1.0f);
    }
    
    public void dibujar(Graphics g) {
        dibujar(g, null, 1.0f, 1.0f);
    }

    public void setBaseSize(int w, int h) {
        this.width = Math.max(10, w);
        this.height = Math.max(10, h);
        this.alturaActual = agachado ? (int)(this.height * 0.6) : this.height;
    }

    public void setDrawYOffset(int offset) { this.drawYOffset = offset; }
    public int getDrawYOffset() { return this.drawYOffset; }

    public void setIgnorarLimitesHorizontales(boolean ign) { this.ignorarLimitesHorizontales = ign; }
    public boolean isIgnorarLimitesHorizontales() { return this.ignorarLimitesHorizontales; }
    
    public void setAlturaVisualOverride(int altura) { this.alturaVisualOverride = Math.max(10, altura); }
    public void clearAlturaVisualOverride() { this.alturaVisualOverride = -1; }
    
    private int obtenerAnchoVisualWalk() {
        int alturaBase = 300; // Altura base consistente
        Sprite s = spriteWalk != null ? spriteWalk : spriteActual;
        javax.swing.ImageIcon icon = (s != null) ? s.getIcon() : null;
        java.awt.Image img = (s != null) ? s.getImagen() : null;
        int iconW = (icon != null) ? icon.getIconWidth() : 0;
        int iconH = (icon != null) ? icon.getIconHeight() : 0;
        if ((iconW <= 0 || iconH <= 0) && img != null) {
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
            iconW = (imgW > 0) ? imgW : iconW;
            iconH = (imgH > 0) ? imgH : iconH;
        }
        if (iconH > 0 && iconW > 0) {
            double escala = (double) alturaBase / iconH;
            return Math.max(10, (int) (iconW * escala));
        }
        return Math.max(10, width);
    }
}