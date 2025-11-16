package main;

import java.awt.*;
import javax.sound.sampled.Clip;

// clase que representa un personaje jugable con fisica, animaciones y audio
public class Jugador {
    // propiedades fisicas y de posicion
    private int x, y, width, height, alturaActual;  // posicion y dimensiones
    private int velX, velY;                         // velocidades en x e y
    private int fuerzaSalto = 20;                   // fuerza del salto
    private int gravedad = 2;                       // gravedad aplicada
    private boolean enSuelo = true;                 // si esta tocando el suelo
    private boolean agachado = false;               // si esta agachado

    // propiedades del personaje
    private int vida = 100;                         // vida actual
    private int vidaMax = 100;                      // vida maxima
    private String nombre;                          // nombre del personaje
    private Color color;                            // color de fallback
    public boolean atacando = false;                // si esta atacando

    // sprites para diferentes estados
    private Sprite spriteIdle, spriteAttack, spriteAttackAlt;  // idle y ataques
    private Sprite spriteWalk, spriteCrouch, spriteJump;       // movimiento
    private Sprite spriteHurt, spriteKO;                       // dano y derrota
    private Sprite spriteActual;                               // sprite actual mostrado

    // sistema de combate y cooldowns
    private int duracionAtaque = 19;                // frames que dura un ataque
    private int cooldownAtaque = 0;                 // cooldown entre ataques
    private int contadorCooldown = 0;               // contador de cooldown actual
    private int contadorAtaque = 0;                 // contador de frames de ataque
    private boolean golpeAplicado = false;          // si ya se aplico dano en este ataque
    private int cooldownSalto = 60;                 // cooldown entre saltos
    private int contadorCooldownSalto = 0;          // contador de cooldown de salto

    // sistema de audio
    private String audioComienzo, audioRespiracion, audioDano, audioGameOver;  // rutas de audio
    private String audioSaltoAtaque, audioSalto, audioAtaque;
    private Clip clipRespiracion, clipInicio, clipGameOver;                    // clips de audio activos
    
    // configuracion del personaje
    private String personajeId;                     // id unico del personaje
    private boolean alternarAtaques = false;        // si alterna entre ataques
    private boolean usarAttackAltNext = false;      // usar ataque alternativo siguiente
    private boolean flipHorizontal = false;         // si voltear horizontalmente

    // configuracion visual y de renderizado
    private boolean ignorarLimitesHorizontales = false;  // ignorar limites de pantalla
    private int alturaVisualOverride = -1;               // override de altura visual
    private int drawYOffset = 0;                         // offset de dibujo en y

    // sistema de animaciones especiales
    private boolean enAnimacionDerrota = false;          // si esta en animacion de derrota
    private int framesAnimacionDerrota = 0;              // frames de animacion de derrota
    private int duracionDerrotaFrames = 120;             // duracion total de derrota

    // sistema de dano visual
    private int contadorHurtFrames = 0;                  // frames de animacion de dano
    private int duracionHurtFrames = 20;                 // duracion de animacion de dano


    // constructor: inicializa un jugador con posicion y color
    public Jugador(String nombre, int x, int y, Color color) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 100;
        this.alturaActual = this.height;
        this.color = color;

        // inicializar sprites como null (se asignan despues)
        this.spriteIdle = null;
        this.spriteAttack = null;
        this.spriteActual = null;
    }

    // asigna todos los assets (sprites y audio) del personaje
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
        this.alternarAtaques = "J1".equals(nombre) || "J2".equals(nombre);
        this.usarAttackAltNext = false;
    }

    // Mueve el personaje hacia la izquierda
    public void moverIzquierda() {
        if (agachado) { velX = 0; return; }
        velX = -7;
        if (spriteWalk != null && !atacando && !enAnimacionDerrota) spriteActual = spriteWalk;
    }
    
    // Mueve el personaje hacia la derecha
    public void moverDerecha() {
        if (agachado) { velX = 0; return; }
        velX = 7;
        if (spriteWalk != null && !atacando && !enAnimacionDerrota) spriteActual = spriteWalk;
    }
    
    // Detiene el movimiento horizontal del personaje
    public void detener() { 
        velX = 0; 
        if (!atacando && !enAnimacionDerrota) 
            spriteActual = agachado && spriteCrouch != null ? spriteCrouch : spriteIdle; 
    }

    // Establece el estado de agachado del personaje
    public void setAgachado(boolean a) {
        this.agachado = a;
        if (agachado) {
            // Reducir altura de colisión y detener movimiento
            alturaActual = (int)(height * 0.6);
            velX = 0;
            if (spriteCrouch != null && !atacando && !enAnimacionDerrota) spriteActual = spriteCrouch;
            // Ajustes visuales por personaje
            if ("Ash".equals(getPersonajeId())) {
                setAlturaVisualOverride(240);
            } else if ("Darth_Vader".equals(getPersonajeId())) {
                setAlturaVisualOverride(330);
            }
        } else {
            // restaurar altura normal
            alturaActual = height;
            if (!atacando && !enAnimacionDerrota) spriteActual = spriteIdle;
            clearAlturaVisualOverride();
        }
    }

    public boolean isAgachado() {
        return agachado;
    }

    // hace saltar al personaje si esta en el suelo
    public void saltar() {
        if (agachado) return;
        if (enSuelo && contadorCooldownSalto == 0) {
            // aplicar velocidad vertical negativa (hacia arriba)
            velY = -fuerzaSalto;
            enSuelo = false;
            if (spriteJump != null) spriteActual = spriteJump;
            // reproducir audio de salto
            if (audioSalto != null) {
                AudioPlayer.playOnce(audioSalto);
            } else if (audioSaltoAtaque != null) {
                AudioPlayer.playOnce(audioSaltoAtaque);
            }
            contadorCooldownSalto = cooldownSalto;
        }
    }

    // ejecuta un ataque si no esta en cooldown
    public void atacar() {
        if (agachado) { return; }
        if (contadorAtaque == 0 && contadorCooldown == 0) {
            // iniciar ataque
            atacando = true;
            contadorAtaque = duracionAtaque;
            golpeAplicado = false;
            // alternar entre ataques para ash
            if (alternarAtaques && spriteAttackAlt != null && "Ash".equals(getPersonajeId())) {
                spriteActual = usarAttackAltNext ? spriteAttackAlt : spriteAttack;
                usarAttackAltNext = !usarAttackAltNext;
            } else {
                spriteActual = spriteAttack;
            }
            // reproducir audio de ataque
            if (audioAtaque != null) {
                AudioPlayer.playOnce(audioAtaque);
            } else if (audioSaltoAtaque != null) {
                AudioPlayer.playOnce(audioSaltoAtaque);
            }
        }
    }

    // actualiza la fisica y estado del personaje cada frame
    public void actualizar() {
        // manejar animacion de derrota
        if (enAnimacionDerrota) {
            framesAnimacionDerrota++;
            if (framesAnimacionDerrota >= duracionDerrotaFrames) {
                enAnimacionDerrota = false;
                if (spriteIdle != null) spriteActual = spriteIdle;
            }
        }

        // actualizar posicion horizontal
        x += velX;
        if (!ignorarLimitesHorizontales) {
            int anchoVisual = obtenerAnchoVisualWalk();
            if (x < 0) x = 0;
            if (x > 800 - anchoVisual) x = 800 - anchoVisual;
        }

        // actualizar posicion vertical y aplicar gravedad
        y += velY;
        if (!enSuelo) velY += gravedad;

        // colision con techo
        if (y - alturaActual < 0) {
            y = alturaActual;
            velY = 0;
        }

        // colision con suelo
        if (y >= 500) {
            y = 500;
            velY = 0;
            enSuelo = true;
            if (!agachado && !atacando && !enAnimacionDerrota) {
                if (velX != 0 && spriteWalk != null) {
                    spriteActual = spriteWalk;
                } else if (spriteIdle != null) {
                    spriteActual = spriteIdle;
                }
            }
        }

        if (contadorAtaque > 0) {
            contadorAtaque--;
            if (contadorAtaque == 0) {
                atacando = false;
                golpeAplicado = false;
                if (!enAnimacionDerrota) spriteActual = spriteIdle;
                contadorCooldown = cooldownAtaque;
            }
        }

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
    }

    // Métodos para controlar si ya se aplicó daño en el ataque actual
    public boolean haGolpeado() { return golpeAplicado; }
    public void marcarGolpeado() { golpeAplicado = true; }

    // Congela al personaje (detiene movimiento y acciones)
    public void congelar() {
        velX = 0;
        velY = 0;
        atacando = false;
        enSuelo = true;
    }
    // Getters y setters básicos del personaje
    public void setVida(int v) { vida = Math.max(0, Math.min(vidaMax, v)); }
    public void setVidaMax(int max) { this.vidaMax = Math.max(1, max); this.vida = Math.min(this.vida, this.vidaMax); }
    public int getVidaMax() { return this.vidaMax; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getX() { return this.x; }
    public void setFlipHorizontal(boolean flip) { this.flipHorizontal = flip; }
    public boolean isFlipHorizontal() { return this.flipHorizontal; }
    public int getY() { return this.y; }

    // Sistema de audio de introducción del personaje
    private boolean haSonadoAudioComienzo = false;
    public void onCountdownStart() {
        if (spriteIdle != null && !enAnimacionDerrota) spriteActual = spriteIdle;
        atacando = false;
        agachado = false;

        if (audioComienzo != null && !haSonadoAudioComienzo) {
            System.out.println("[DEBUG] " + personajeId + " reproduciendo audio comienzo: " + audioComienzo);
            AudioPlayer.playOnceAsync(audioComienzo, c -> {
                clipInicio = c;
                if (c != null) {
                    haSonadoAudioComienzo = true;
                } else {
                    System.err.println("[DEBUG] " + personajeId + " fallo al cargar audio de comienzo: " + audioComienzo);
                }
            });
        } else if (haSonadoAudioComienzo) {
            System.out.println("[DEBUG] " + personajeId + " ya reprodujo su audio de comienzo.");
        } else {
            System.out.println("[DEBUG] " + personajeId + " no tiene audio de comienzo.");
        }
    }
    // Resetea el estado del personaje para una nueva ronda
    public void resetParaNuevaRonda() {
        haSonadoAudioComienzo = false;
        enAnimacionDerrota = false;
        framesAnimacionDerrota = 0;
        stopAllAudio();
        if (spriteIdle != null) {
            spriteActual = spriteIdle;
        }
    }

    // Se ejecuta cuando inicia el combate (después de la cuenta regresiva)
    public void onFightStart() {
        if (audioRespiracion != null) {
            AudioPlayer.stop(clipRespiracion);
            AudioPlayer.playLoopAsync(audioRespiracion, c -> clipRespiracion = c);
        }
        if (!agachado && !atacando && spriteIdle != null) spriteActual = spriteIdle;
    }

    // Maneja el evento de derrota del personaje
    public void onGameOver() { onGameOver(true); }

    public void onGameOver(boolean reproducirAudio) {
        AudioPlayer.stop(clipRespiracion);
        if (spriteKO != null) {
            spriteActual = spriteKO;
        } else if (spriteHurt != null) {
            spriteActual = spriteHurt;
        } else if (spriteIdle != null) {
            spriteActual = spriteIdle;
        }
        if (reproducirAudio && audioGameOver != null) {
            AudioPlayer.playOnceAsync(audioGameOver, c -> clipGameOver = c);
        }
        enAnimacionDerrota = true;
        framesAnimacionDerrota = 0;
        congelar();
    }

    // Fuerza al personaje al estado idle
    public void forceIdle() {
        if (spriteIdle != null) spriteActual = spriteIdle;
        congelar();
    }

    // Métodos para obtener duración de audios (para sincronización)
    public long getGameOverAudioLengthMicros() {
        try { return clipGameOver != null ? clipGameOver.getMicrosecondLength() : 0L; } catch (Exception ex) { return 0L; }
    }

    public long getInicioAudioLengthMicros() {
        try { return clipInicio != null ? clipInicio.getMicrosecondLength() : 0L; } catch (Exception ex) { return 0L; }
    }

    // Detiene todos los audios del personaje
    public void stopAllAudio() {
        AudioPlayer.stop(clipRespiracion);
        clipRespiracion = null;
        AudioPlayer.stop(clipGameOver);
        clipGameOver = null;
        AudioPlayer.stop(clipInicio);
        clipInicio = null;
    }

    // Método principal de renderizado del personaje
    public void dibujar(Graphics g, java.awt.Component observer, float scaleX, float scaleY) {
        int drawX = (int) (x * scaleX);

        int drawY = (int) ((y) * scaleY);

        int alturaBase = 240;
        int alturaVisualNormalizada = (int) (alturaBase * scaleY);
        if (alturaVisualOverride > 0) {
            alturaVisualNormalizada = (int) (alturaVisualOverride * scaleY);
        }

        int ajusteSueloLocal = 0;

        Sprite s = spriteActual;
        javax.swing.ImageIcon icon = (s != null) ? s.getIcon() : null;
        Image img = (s != null) ? s.getImagen() : null;

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

        if (iconW <= 0 || iconH <= 0) {
            iconW = 100;
            iconH = alturaBase;
        }

        if ("Darth_Vader".equals(personajeId)) {
            escala *= 0.85;

            if (s == spriteAttack) {
                escala *= 1.5;
            } else if (s == spriteWalk) {
                escala *= 1.45;
            } else if (s == spriteCrouch) {
                escala *= 1.25;
            } else if (s == spriteKO) {
                escala *= 1.3;
            }
            ajusteSueloLocal = 0;
        }
        else if ("Ash".equals(personajeId)) {
            escala *= 1.0;
            ajusteSueloLocal = 0;
        }
        else if ("Iron_Man".equals(personajeId)) {
            escala *= 0.95;
            ajusteSueloLocal = 0;
        }
        else if ("Goku".equals(personajeId)) {
            escala *= 0.9;
            ajusteSueloLocal = 0;
        }
        else if ("Mr_Increible".equals(personajeId)) {
            escala *= 0.88;

            if (s == spriteCrouch) {
                escala *= 0.75;
            }
            ajusteSueloLocal = 0;
        }
        else if ("Batman".equals(personajeId)) {
            escala *= 0.92;
            ajusteSueloLocal = 0;
        }
        else if ("Luke_Skywalker".equals(personajeId)) {
            escala *= 0.9;
            ajusteSueloLocal = 0;
        }
        else if ("Naruto".equals(personajeId)) {
            escala *= 0.88;
            ajusteSueloLocal = 0;
        }
        else if ("Pyke".equals(personajeId)) {
            escala *= 0.9;
            ajusteSueloLocal = 0;
        }

        else if ("Charizard".equals(personajeId)) {
            escala *= 0.95;
            ajusteSueloLocal = 0;
        }
        else if ("Greninja".equals(personajeId)) {
            escala *= 0.9;
            ajusteSueloLocal = 0;
        }
        else if ("Pikachu".equals(personajeId)) {
            escala *= 0.5;
            ajusteSueloLocal = 0;
        }

        int anchoNormalizado = (int) (iconW * escala);

        if (icon != null && iconW > 0 && iconH > 0) {

            java.awt.image.BufferedImage frame = new java.awt.image.BufferedImage(
                    Math.max(1, iconW), Math.max(1, iconH), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D gt = frame.createGraphics();
            icon.paintIcon(observer, gt, 0, 0);
            gt.dispose();

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
            boolean flip = this.flipHorizontal;

            int drawYConAjuste = drawY + (int) (ajusteSueloLocal * scaleY);

            if (flip) {
                g2.translate(drawX + anchoNormalizado, drawYConAjuste);
                g2.scale(-1, 1);
            } else {
                g2.translate(drawX, drawYConAjuste);
            }

            g2.scale(escala, escala);
            g2.drawImage(frame, 0, -iconH, null);
            g2.dispose();
        } else if (img != null) {

            int imgH = img.getHeight(observer);
            int imgW = img.getWidth(observer);
            if (imgH <= 0) imgH = alturaVisualNormalizada;

            double escalaImg = (double) alturaVisualNormalizada / imgH;
            int anchoNormalizadoImg = (int) ((imgW > 0 ? imgW : alturaVisualNormalizada) * escalaImg);

            if (this.flipHorizontal) {
                g.drawImage(img, drawX + anchoNormalizadoImg, drawY - alturaVisualNormalizada,
                           -anchoNormalizadoImg, alturaVisualNormalizada, observer);
            } else {
                g.drawImage(img, drawX, drawY - alturaVisualNormalizada,
                           anchoNormalizadoImg, alturaVisualNormalizada, observer);
            }
        } else {

            int drawW = (int) (width * scaleX);
            int drawH = (int) (alturaActual * scaleY);
            g.setColor(color);
            if (this.flipHorizontal) {
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
        int alturaBase = 240;

        double escalaHitbox = 1.0;
        if ("Darth_Vader".equals(personajeId)) {
            escalaHitbox = 0.85;
        } else if ("Iron_Man".equals(personajeId)) {
            escalaHitbox = 0.95;
        } else if ("Goku".equals(personajeId)) {
            escalaHitbox = 0.9;
        } else if ("Mr_Increible".equals(personajeId)) {
            escalaHitbox = 0.88;
        } else if ("Batman".equals(personajeId)) {
            escalaHitbox = 0.92;
        } else if ("Luke_Skywalker".equals(personajeId)) {
            escalaHitbox = 0.9;
        } else if ("Naruto".equals(personajeId)) {
            escalaHitbox = 0.88;
        } else if ("Pyke".equals(personajeId)) {
            escalaHitbox = 0.9;
        } else if ("Charizard".equals(personajeId)) {
            escalaHitbox = 0.95;
        } else if ("Greninja".equals(personajeId)) {
            escalaHitbox = 0.9;
        } else if ("Pikachu".equals(personajeId)) {
            escalaHitbox = 0.4;
        }

        int alturaHitbox = (int) (alturaBase * escalaHitbox);
        int anchoHitbox = (int) (anchoWalk * escalaHitbox);

        return new Rectangle(x, y - alturaHitbox, Math.max(10, anchoHitbox), alturaHitbox);
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
        int alturaBase = 240;
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

    // Método de debug para verificar recursos de Pikachu (movido desde DebugPika.java)
    public static void debugPikachuResources() {
        String base = "Pokemon/Pikachu/images/";
        String[] paths = new String[]{
                base + "pikachu_idle.png",
                base + "pikachu_caminando.gif",
                base + "pikachu_idle.png",
                base + "pikachu_atacando.png",
                base + "pikachu_daño.png",
                null,
                base + "pikachu_idle.png"
        };
        String[] labels = new String[]{"idle","walk","crouch","attack","hurt","ko","jump"};

        // Verificar sprites de Pikachu
        for (int i = 0; i < paths.length; i++) {
            String p = paths[i];
            Sprite s = p != null ? new Sprite(p) : null;
            boolean loaded = s != null && (s.getIcon() != null || s.getImagen() != null);
            System.out.println("[Pikachu Debug] " + labels[i] + " path=" + p + " loaded=" + loaded);
        }

        // Verificar audios de Pikachu
        String sounds = "Pokemon/Pikachu/sounds/";
        String[] audios = new String[]{
                sounds + "Pikachu_Comienzo.wav",
                sounds + "Pikachu_GameOver.wav",
                sounds + "Golpe.wav",
                sounds + "Daño.wav"
        };
        for (String a : audios) {
            java.net.URL url = Jugador.class.getResource("/resources/" + a);
            boolean ok = false;
            if (url != null) {
                ok = true;
            } else {
                java.io.File fSrc = new java.io.File("src/resources/" + a);
                ok = fSrc.exists();
            }
            System.out.println("[Pikachu Debug] audio=" + a + " exists=" + ok);
        }
    }
}
