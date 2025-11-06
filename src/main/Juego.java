package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Juego extends JPanel implements ActionListener {

    private JFrame ventana;
    private Timer timer;
    private Jugador jugador1, jugador2;
    private Input input;
    private boolean juegoTerminado = false;
    private String mensajeVictoria = "";
    private boolean enCuentaRegresiva = true;
    private int contadorRegresivo = 3; // 3, 2, 1, FIGHT
    private int contadorFrames = 0;     // para controlar el tiempo
    private JButton botonReiniciar;
    private JButton botonCambiar;
    private boolean juegoIniciado = false;
    private int contadorInicio = 3;
    // Base de diseño para escalado responsive
    private final int baseWidth = 800;
    private final int baseHeight = 600;
    // Pausa
    private boolean enPausa = false;
    private boolean escPrev = false;
    // Cronómetro de partida
    private int segundosRestantes = 90;
    private int framesTimer = 0; // deprecado
    private long cronometroAcumuladoNanos = 0L;
    private boolean tiempoAgotado = false;
    private int drainRatePorFrame = 1;
    // Marcador mejor de 3 victorias
    private int victoriasJ1 = 0;
    private int victoriasJ2 = 0;
    // Selección de personajes
    private String personajeSelJ1 = null;
    private String personajeSelJ2 = null;
    private boolean inicioAudioLanzado = false;
    private boolean inicioAudioJ2Pendiente = false;
    private int framesDelayAudioJ2 = 0;
    private static final int FRAMES_ESPERA_AUDIO_J2 = 60; // ~1.0s a ~60fps
    private int delayAudioJ2Frames = 20;
    // Espera al final del match para terminar audios/animaciones
    private boolean enEsperaPostMatch = false;
    private int framesEsperaPostMatch = 0;
    // Espera al final de la ronda (no match) para reproducir sólo derrotado
    private boolean enEsperaPostRonda = false;
    private int framesEsperaPostRonda = 0;
    // Retirada del ganador antes de animación KO
    private boolean enRetiradaTrasKO = false;
    private Jugador ganadorKO = null;
    private Jugador derrotadoKO = null;
    private int framesRetiradaKO = 0;
    private int velRetiradaKO = 5;
    private int dirRetiradaKO = 1;
    private boolean esMatchKO = false;
    // Fondo del escenario
    private Image fondoImg = null;
    private String fondoSel = "deathStar.png";

    // Overlay y estado de selección de Pokémon (Ash)
    private Sprite selAshBase = null;
    private Sprite selAshC = null;
    private Sprite selAshG = null;
    private Sprite selAshP = null;
    private boolean seleccionActivaJ1 = false;
    private int seleccionIndexJ1 = 0; // 0:C, 1:G, 2:P
    private String seleccionPokemonJ1 = null;
    // Estado de selección para Jugador 2 (Ash)
    private boolean seleccionActivaJ2 = false;
    private int seleccionIndexJ2 = 0; // 0:C, 1:G, 2:P
    private String seleccionPokemonJ2 = null;
    // Estados de Pokémons muertos (no re-seleccionables)
    private boolean muertoC = false; // Charizard
    private boolean muertoG = false; // Greninja
    private boolean muertoP = false; // Pikachu
    // Flujo de salida de Ash y estado del pokémon de J1
    private boolean ashSaliendoJ1 = false;
    private int dirSalidaAshJ1 = -1; // J1 sale a la izquierda
    private boolean pokemonActivoJ1 = false;
    private int pokemonVidaJ1 = 200;
    private boolean pokemonVolviendoJ1 = false;
    private int shrinkAlturaJ1 = 300; // altura visual para efecto de regreso a pokebola
    private int esperaAudioPokemonKOJ1 = 0; // frames de espera para terminar audio de GameOver del Pokémon
    // Flujo de Jugador 2
    private boolean ashSaliendoJ2 = false;
    private int dirSalidaAshJ2 = 1; // J2 sale a la derecha
    private boolean pokemonActivoJ2 = false;
    private int pokemonVidaJ2 = 200;
    private boolean pokemonVolviendoJ2 = false;
    private int shrinkAlturaJ2 = 300;
    private int esperaAudioPokemonKOJ2 = 0; // frames de espera para terminar audio de GameOver del Pokémon

    // --- Modo Historia ---
    private boolean storyMode = false;
    private Level[] nivelesHistoria = null;
    private int indiceNivel = 0;
    private BotIA botIA = null;
    private boolean bossFinalActivo = false;
    private int bossCycleFrames = 0;    // cuenta hacia 20s (~1250 frames)
    private int bossInvulFrames = 0;    // invulnerable por ~5s (~312 frames)
    // Debug: contador de ticks para verificar actividad del timer
    private int debugTickCount = 0;



    public Juego() {
    ventana = new JFrame("Multiverse Dominion");
    ventana.setSize(baseWidth, baseHeight);
    ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ventana.setResizable(true);
    ventana.add(this);
    // Iniciar en pantalla completa (maximizado)
    ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
    // Menú de opciones ahora se muestra al presionar ESC (pausa)
    ventana.setVisible(true);

    // Input
        input = new Input();
        // Escuchar teclado tanto en la ventana como en el panel para mayor fiabilidad
        ventana.addKeyListener(input);
        try {
            this.addKeyListener(input);
            this.setFocusable(true);
            this.setFocusTraversalKeysEnabled(false);
            // Click en el panel recupera el foco del teclado
            this.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mousePressed(java.awt.event.MouseEvent e) {
                    try {
                        requestFocusInWindow();
                        if (ventana != null) ventana.requestFocus();
                    } catch (Exception ignored) {}
                }
            });
            // Solicitar foco tras construir la UI en el EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    this.requestFocusInWindow();
                    if (ventana != null) ventana.requestFocus();
                } catch (Exception ignored) {}
            });
        } catch (Exception ignore) {}

    // Verificación de Timer tras mostrar la UI (asegurar arranque en EDT)
    SwingUtilities.invokeLater(() -> {
        try {
            if (timer != null && !timer.isRunning()) {
                timer.start();
            }
            System.out.println("[DEBUG] Post-UI: timer running=" + (timer != null && timer.isRunning()));
        } catch (Exception ignored) {}
    });

    // Jugadores
    jugador1 = new Jugador("J1", 100, 500, Color.BLUE);
    jugador2 = new Jugador("J2", 500, 500, Color.RED);

    aplicarPersonaje(jugador1, "Darth_Vader");
    aplicarPersonaje(jugador2, "Darth_Vader");

    // Cargar fondo inicial
    cargarFondo(fondoSel);
    // Timer para el loop del juego (60 FPS)
    timer = new Timer(16, this);
    timer.start();
    System.out.println("[DEBUG] Timer creado y arrancado");

    // Cargar imágenes de overlay de selección de Ash
    try {
        selAshBase = new Sprite("Pokemon/Ash/images/ash_seleccion.png");
        selAshC = new Sprite("Pokemon/Ash/images/ash_seleccion_c.png");
        selAshG = new Sprite("Pokemon/Ash/images/ash_seleccion_g.png");
        selAshP = new Sprite("Pokemon/Ash/images/ash_seleccion_p.png");
    } catch (Exception ex) {
        System.out.println("[WARN] Imágenes de selección Ash no cargadas: " + ex.getMessage());
    }
}

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        float scaleX = getWidth() / (float) baseWidth;
        float scaleY = getHeight() / (float) baseHeight;

        // Paleta pixel rojo/negro
        Color PIXEL_BLACK = new Color(10,10,10);
        Color PIXEL_RED = new Color(170,0,0);
        Color PIXEL_RED_DARK = new Color(120,0,0);
        Font PIXEL_FONT = new Font("Courier New", Font.BOLD, Math.max(10, (int)(14 * scaleY)));

        // Fondo
        if (fondoImg != null) {
            g.drawImage(fondoImg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(PIXEL_BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Suelo pixelado (gris)
        int sueloY = (int) (500 * scaleY);
        int sueloH = (int) (100 * scaleY);
        Color GRAY_MID = new Color(90,90,90);
        g.setColor(GRAY_MID);
        g.fillRect(0, sueloY, getWidth(), sueloH);
        g.setColor(Color.BLACK);
        g.fillRect(0, sueloY - 2, getWidth(), 2);
        g.fillRect(0, sueloY - 5, getWidth(), 2);

        // Si aún no están inicializados los jugadores, dibujar mensaje y salir
        if (jugador1 == null || jugador2 == null) {
            g.setColor(PIXEL_RED);
            int fontInit = Math.max(12, (int) (20 * scaleY));
            g.setFont(new Font("Monospaced", Font.PLAIN, fontInit));
            g.drawString("Inicializando...", 20, 40);
            return;
        }

        // Barras de vida estilo pixel
        int margin = (int) (20 * scaleX);
        int barW = (int) (300 * scaleX);
        int barH = (int) (20 * scaleY);
        int barY = (int) (15 * scaleY);
        int j1BarX = margin;
        int j2BarX = getWidth() - margin - barW;
        g.setFont(PIXEL_FONT);
        // Bordes negros gruesos
        g.setColor(Color.BLACK);
        g.fillRect(j1BarX - 6, barY - 6, barW + 12, barH + 12);
        g.fillRect(j2BarX - 6, barY - 6, barW + 12, barH + 12);
        // Relleno rojo según vida (normalizar a vidaMax de cada jugador)
        double vidaMaxJ1 = Math.max(1, jugador1.getVidaMax());
        double vidaMaxJ2 = Math.max(1, jugador2.getVidaMax());
        int j1Len = Math.max(0, Math.min(barW, (int)(barW * (jugador1.getVida() / vidaMaxJ1))));
        int j2Len = Math.max(0, Math.min(barW, (int)(barW * (jugador2.getVida() / vidaMaxJ2))));
        g.setColor(PIXEL_RED);
        g.fillRect(j1BarX, barY, j1Len, barH);
        g.fillRect(j2BarX, barY, j2Len, barH);
        // Barra secundaria de vida del Pokémon de J1, más fina y separada
        if (pokemonActivoJ1) {
            int sep = Math.max(4, (int)(6 * scaleY));
            int barH2 = Math.max(6, (int)(10 * scaleY));
            int barY2 = barY + barH + sep;
            // Marco negro
            g.setColor(Color.BLACK);
            g.fillRect(j1BarX - 4, barY2 - 4, barW + 8, barH2 + 8);
            // Relleno rojo según vida del Pokémon (base 200)
            int pLen = Math.max(0, (int)(barW * (pokemonVidaJ1 / 200.0)));
            g.setColor(PIXEL_RED);
            g.fillRect(j1BarX, barY2, pLen, barH2);
        }
        // Barra secundaria de vida del Pokémon de J2, simétrica a J1
        if (pokemonActivoJ2) {
            int sep = Math.max(4, (int)(6 * scaleY));
            int barH2 = Math.max(6, (int)(10 * scaleY));
            int barY2 = barY + barH + sep;
            // Marco negro
            g.setColor(Color.BLACK);
            g.fillRect(j2BarX - 4, barY2 - 4, barW + 8, barH2 + 8);
            // Relleno rojo según vida del Pokémon de J2 (base 200)
            int pLen2 = Math.max(0, (int)(barW * (pokemonVidaJ2 / 200.0)));
            g.setColor(PIXEL_RED);
            g.fillRect(j2BarX, barY2, pLen2, barH2);
        }
        
        // Cronómetro superior: usa cuenta regresiva o segundos
        int boxWTop = Math.max(70, (int)(90 * scaleX));
        int boxHTop = Math.max(30, (int)(40 * scaleY));
        int cxTop = (getWidth() - boxWTop) / 2;
        int cyTop = barY - (int)(5 * scaleY);
        g.setColor(PIXEL_RED);
        g.fillRect(cxTop, cyTop, boxWTop, boxHTop);
        // Borde más grueso
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new java.awt.BasicStroke(4));
        g2.drawRect(cxTop, cyTop, boxWTop, boxHTop);
        g2.setColor(PIXEL_RED_DARK);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(cxTop+2, cyTop+2, boxWTop-4, boxHTop-4);
        String timerText = enCuentaRegresiva ? (contadorRegresivo > 0 ? String.valueOf(contadorRegresivo) : "FIGHT!") : String.valueOf(Math.max(0, segundosRestantes));
        g.setFont(new Font("Monospaced", Font.BOLD, Math.max(16, (int)(24 * scaleY))));
        FontMetrics fmtTop = g.getFontMetrics();
        int txTop = cxTop + (boxWTop - fmtTop.stringWidth(timerText)) / 2;
        int tyTop = cyTop + (boxHTop + fmtTop.getAscent()) / 2 - 6;
        g.setColor(Color.BLACK);
        g.drawString(timerText, txTop-1, tyTop);
        g.drawString(timerText, txTop+1, tyTop);
        g.drawString(timerText, txTop, tyTop-1);
        g.drawString(timerText, txTop, tyTop+1);
        g.drawString(timerText, txTop, tyTop);
        
        // Marcador J1/J2 debajo del cronómetro (con caja estilo timer)
        String etiquetaJ2 = storyMode ? "Oponente" : "J2";
        String marcador = "J1 " + victoriasJ1 + " - " + etiquetaJ2 + " " + victoriasJ2;
        g.setFont(new Font("Monospaced", Font.BOLD, Math.max(12, (int)(18 * scaleY))));
        FontMetrics fms = g.getFontMetrics();
        int boxWScore = fms.stringWidth(marcador) + Math.max(40, (int)(24 * scaleX));
        int boxHScore = Math.max(28, (int)(34 * scaleY));
        int cxScore = (getWidth() - boxWScore) / 2;
        int cyScore = cyTop + boxHTop + (int)(10 * scaleY);
        g.setColor(new Color(170,0,0));
        g.fillRect(cxScore, cyScore, boxWScore, boxHScore);
        // Borde más grueso para marcador
        g2.setColor(Color.BLACK);
        g2.setStroke(new java.awt.BasicStroke(4));
        g2.drawRect(cxScore, cyScore, boxWScore, boxHScore);
        g2.setColor(new Color(120,0,0));
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(cxScore+2, cyScore+2, boxWScore-4, boxHScore-4);
        int txScore = cxScore + (boxWScore - fms.stringWidth(marcador)) / 2;
        int tyScore = cyScore + (boxHScore + fms.getAscent()) / 2 - 6;
        g.setColor(Color.BLACK);
        g.drawString(marcador, txScore-1, tyScore);
        g.drawString(marcador, txScore+1, tyScore);
        g.drawString(marcador, txScore, tyScore-1);
        g.drawString(marcador, txScore, tyScore+1);
        g.drawString(marcador, txScore, tyScore);
        // Marcador ya dibujado dentro de la caja estilo timer

        // Orientación dinámica: que se miren siempre
        // Si J1 está a la derecha de J2, J1 mira a la izquierda (flip) y J2 a la derecha (no flip).
        // Si J1 vuelve a estar a la izquierda, invertimos de nuevo.
        try {
            int centroJ1 = jugador1.getX() + Math.max(10, jugador1.getBounds().width) / 2;
            int centroJ2 = jugador2.getX() + Math.max(10, jugador2.getBounds().width) / 2;
            boolean j1MasDerecha = centroJ1 > centroJ2;
            jugador1.setFlipHorizontal(j1MasDerecha);
            jugador2.setFlipHorizontal(!j1MasDerecha);
        } catch (Exception ignored) {}

        // Dibujar jugadores (pasar el panel como observer para animar GIFs)
        jugador1.dibujar(g, this, scaleX, scaleY);
        jugador2.dibujar(g, this, scaleX, scaleY);

        // Overlay de selección Pokémon (Ash) por encima de todo
        if (seleccionActivaJ1) {
            // fondo semitransparente
            g.setColor(new Color(0,0,0,150));
            g.fillRect(0, 0, getWidth(), getHeight());
            // dibujar base y opción resaltada al centro
            int overlayW = (int)(Math.min(getWidth(), getHeight()) * 0.7);
            int overlayH = (int)(overlayW * 0.6);
            int ox = (getWidth() - overlayW) / 2;
            int oy = (getHeight() - overlayH) / 2;
            boolean dibujadoConImagen = false;
            if (selAshBase != null && selAshBase.getImagen() != null) {
                g.drawImage(selAshBase.getImagen(), ox, oy, overlayW, overlayH, this);
                dibujadoConImagen = true;
            }
            Sprite highlight = seleccionIndexJ1 == 0 ? selAshC : (seleccionIndexJ1 == 1 ? selAshG : selAshP);
            if (highlight != null && highlight.getImagen() != null) {
                g.drawImage(highlight.getImagen(), ox, oy, overlayW, overlayH, this);
                dibujadoConImagen = true;
            }
            // Fallback gráfico si no hay imágenes: dibujar panel con letras C/G/P y resaltar selección
            if (!dibujadoConImagen) {
                // Panel
                g.setColor(new Color(20,20,20));
                g.fillRoundRect(ox, oy, overlayW, overlayH, 20, 20);
                g2.setColor(new Color(170,0,0));
                g2.setStroke(new java.awt.BasicStroke(4));
                g2.drawRoundRect(ox, oy, overlayW, overlayH, 20, 20);

                // Título
                String titulo = "Selecciona tu Pokémon";
                g.setColor(Color.WHITE);
                g.setFont(new Font("Courier New", Font.BOLD, Math.max(18, (int)(26 * scaleY))));
                FontMetrics fmtTit = g.getFontMetrics();
                int tx = ox + (overlayW - fmtTit.stringWidth(titulo)) / 2;
                int ty = oy + Math.max(30, (int)(40 * scaleY));
                g.drawString(titulo, tx, ty);

                // Opciones C/G/P (marcar con X si están muertos)
                int opcW = overlayW / 3;
                int baseY = oy + overlayH / 2;
                for (int i = 0; i < 3; i++) {
                    int bx = ox + i * opcW + 10;
                    int bw = opcW - 20;
                    int bh = Math.max(60, (int)(80 * scaleY));
                    boolean sel = (i == seleccionIndexJ1);
                    boolean muerto = (i==0?muertoC:(i==1?muertoG:muertoP));
                    g.setColor(sel ? new Color(255,255,255,40) : new Color(255,255,255,20));
                    g.fillRoundRect(bx, baseY - bh/2, bw, bh, 12, 12);
                    g2.setColor(sel ? new Color(170,0,0) : new Color(120,120,120));
                    g2.setStroke(new java.awt.BasicStroke(sel ? 3 : 2));
                    g2.drawRoundRect(bx, baseY - bh/2, bw, bh, 12, 12);
                    // Letra
                    char letra = (i==0?'C':(i==1?'G':'P'));
                    g.setColor(sel ? Color.WHITE : new Color(200,200,200));
                    g.setFont(new Font("Courier New", Font.BOLD, Math.max(30, (int)(44 * scaleY))));
                    FontMetrics fmo = g.getFontMetrics();
                    int lx = bx + (bw - fmo.stringWidth(String.valueOf(letra))) / 2;
                    int ly = baseY + fmo.getAscent()/2 - 6;
                    g.drawString(String.valueOf(letra), lx, ly);
                    if (muerto) {
                        // Dibujar X roja encima para indicar no seleccionable
                        g2.setColor(new Color(170,0,0));
                        g2.setStroke(new java.awt.BasicStroke(4));
                        int pad = Math.max(10, (int)(12 * scaleY));
                        g2.drawLine(bx + pad, baseY - bh/2 + pad, bx + bw - pad, baseY + bh/2 - pad);
                        g2.drawLine(bx + pad, baseY + bh/2 - pad, bx + bw - pad, baseY - bh/2 + pad);
                    }
                }
            }
            // Ayuda de texto
            g.setColor(new Color(170,0,0));
            g.setFont(new Font("Courier New", Font.BOLD, Math.max(12, (int)(18 * scaleY))));
            String help = "F: abrir | A/D: navegar | E: cancelar | R: confirmar";
            FontMetrics fmh = g.getFontMetrics();
            int hx = (getWidth() - fmh.stringWidth(help)) / 2;
            int hy = oy + overlayH + Math.max(20, (int)(24 * scaleY));
            g.drawString(help, hx, Math.min(hy, getHeight() - 10));
        }

        // Overlay de selección Pokémon (Ash) para J2
        if (seleccionActivaJ2) {
            g.setColor(new Color(0,0,0,150));
            g.fillRect(0, 0, getWidth(), getHeight());
            int overlayW2 = (int)(Math.min(getWidth(), getHeight()) * 0.7);
            int overlayH2 = (int)(overlayW2 * 0.6);
            int ox2 = (getWidth() - overlayW2) / 2;
            int oy2 = (getHeight() - overlayH2) / 2;
            boolean dibujadoConImagen2 = false;
            if (selAshBase != null && selAshBase.getImagen() != null) {
                g.drawImage(selAshBase.getImagen(), ox2, oy2, overlayW2, overlayH2, this);
                dibujadoConImagen2 = true;
            }
            Sprite highlight2 = seleccionIndexJ2 == 0 ? selAshC : (seleccionIndexJ2 == 1 ? selAshG : selAshP);
            if (highlight2 != null && highlight2.getImagen() != null) {
                g.drawImage(highlight2.getImagen(), ox2, oy2, overlayW2, overlayH2, this);
                dibujadoConImagen2 = true;
            }
            if (!dibujadoConImagen2) {
                java.awt.Graphics2D g2b = (java.awt.Graphics2D) g;
                g.setColor(new Color(20,20,20));
                g.fillRoundRect(ox2, oy2, overlayW2, overlayH2, 20, 20);
                g2b.setColor(new Color(170,0,0));
                g2b.setStroke(new java.awt.BasicStroke(4));
                g2b.drawRoundRect(ox2, oy2, overlayW2, overlayH2, 20, 20);

                String titulo2 = "Selecciona tu Pokémon (J2)";
                g.setColor(Color.WHITE);
                g.setFont(new Font("Courier New", Font.BOLD, Math.max(18, (int)(26 * scaleY))));
                FontMetrics fmtTit2 = g.getFontMetrics();
                int tx2 = ox2 + (overlayW2 - fmtTit2.stringWidth(titulo2)) / 2;
                int ty2 = oy2 + Math.max(30, (int)(40 * scaleY));
                g.drawString(titulo2, tx2, ty2);

                int opcW2 = overlayW2 / 3;
                int baseY2 = oy2 + overlayH2 / 2;
                for (int i = 0; i < 3; i++) {
                    int bx2 = ox2 + i * opcW2 + 10;
                    int bw2 = opcW2 - 20;
                    int bh2 = Math.max(60, (int)(80 * scaleY));
                    boolean sel2 = (i == seleccionIndexJ2);
                    boolean muerto2 = (i==0?muertoC:(i==1?muertoG:muertoP));
                    g.setColor(sel2 ? new Color(255,255,255,40) : new Color(255,255,255,20));
                    g.fillRoundRect(bx2, baseY2 - bh2/2, bw2, bh2, 12, 12);
                    g2b.setColor(sel2 ? new Color(170,0,0) : new Color(120,120,120));
                    g2b.setStroke(new java.awt.BasicStroke(sel2 ? 3 : 2));
                    g2b.drawRoundRect(bx2, baseY2 - bh2/2, bw2, bh2, 12, 12);
                    char letra2 = (i==0?'C':(i==1?'G':'P'));
                    g.setColor(sel2 ? Color.WHITE : new Color(200,200,200));
                    g.setFont(new Font("Courier New", Font.BOLD, Math.max(30, (int)(44 * scaleY))));
                    FontMetrics fmo2 = g.getFontMetrics();
                    int lx2 = bx2 + (bw2 - fmo2.stringWidth(String.valueOf(letra2))) / 2;
                    int ly2 = baseY2 + fmo2.getAscent()/2 - 6;
                    g.drawString(String.valueOf(letra2), lx2, ly2);
                    if (muerto2) {
                        g2b.setColor(new Color(170,0,0));
                        g2b.setStroke(new java.awt.BasicStroke(4));
                        int pad2 = Math.max(10, (int)(12 * scaleY));
                        g2b.drawLine(bx2 + pad2, baseY2 - bh2/2 + pad2, bx2 + bw2 - pad2, baseY2 + bh2/2 - pad2);
                        g2b.drawLine(bx2 + pad2, baseY2 + bh2/2 - pad2, bx2 + bw2 - pad2, baseY2 - bh2/2 + pad2);
                    }
                }
            }
            g.setColor(new Color(170,0,0));
            g.setFont(new Font("Courier New", Font.BOLD, Math.max(12, (int)(18 * scaleY))));
            String help2 = "L: abrir | Izq/Dcha: navegar | Enter: confirmar";
            FontMetrics fmh2 = g.getFontMetrics();
            int hx2 = (getWidth() - fmh2.stringWidth(help2)) / 2;
            int hy2 = oy2 + overlayH2 + Math.max(20, (int)(24 * scaleY));
            g.drawString(help2, hx2, Math.min(hy2, getHeight() - 10));
        }

        // Removido el letrero central de FIGHT para evitar invasivo
        // (ya se muestra dentro del cronómetro superior)
        
        if (juegoTerminado) {
            int fontSize2 = Math.max(18, (int) (50 * scaleY));
            g.setFont(new Font("Monospaced", Font.BOLD, fontSize2));
            g.setColor(PIXEL_RED);
            FontMetrics fm = g.getFontMetrics();
            int textoX = (getWidth() - fm.stringWidth(mensajeVictoria)) / 2;
            int textoY = getHeight() / 2;
            g.drawString(mensajeVictoria, textoX, textoY);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
        // Debug limitado: imprimir cada ~1s para confirmar actividad del timer
        try {
            debugTickCount++;
            if (debugTickCount % 60 == 0) {
                System.out.println("[DEBUG] Tick:" + debugTickCount +
                    " enCuentaRegresiva=" + enCuentaRegresiva +
                    " contador=" + contadorRegresivo +
                    " pausa=" + enPausa +
                    " tiempoAgotado=" + tiempoAgotado);
            }
        } catch (Exception ignored) {}
        // Toggle de pausa con ESC (detección de flanco) - permitir incluso durante esperas
        if (input.esc && !escPrev) {
            enPausa = !enPausa;
            if (enPausa) {
                mostrarMenuPausa();
            }
        }
        escPrev = input.esc;

        // Asegurar IA activa en Modo Historia si quedó nula por errores previos
        if (storyMode && botIA == null) {
            try {
                botIA = new BotIA();
                if (nivelesHistoria != null && indiceNivel >= 0 && indiceNivel < nivelesHistoria.length) {
                    Level lvl = nivelesHistoria[indiceNivel];
                    botIA.setNivel(lvl.dificultadIA);
                    botIA.setOverrides(lvl.overrideRangoAtaque, lvl.overrideVelAcercamiento, lvl.overrideCooldownAtaque,
                            lvl.overrideProbSalto, lvl.overrideProbAgachar, lvl.overrideDistEvadir, lvl.overrideAgresividad);
                }
                System.out.println("[Historia][IA] BotIA re-inicializada correctamente tras ser nula.");
            } catch (Throwable t) {
                System.err.println("[Historia][IA] No se pudo re-inicializar BotIA: " + t.getMessage());
            }
        }

        // Ciclo de invulnerabilidad del jefe final (5s cada ~20s)
        if (storyMode && bossFinalActivo && !enCuentaRegresiva && !tiempoAgotado && !juegoTerminado) {
            if (bossInvulFrames > 0) {
                bossInvulFrames--;
            } else {
                bossCycleFrames++;
                // ~20s a ~60fps ≈ 1250 frames; invul 5s ≈ 312 frames
                if (bossCycleFrames >= 1250) {
                    bossInvulFrames = 312;
                    bossCycleFrames = 0;
                }
            }
        }
        // Espera post-ronda para terminar audio/animación del derrotado y luego continuar
        if (enEsperaPostRonda) {
            // Congelar movimiento pero seguir actualizando para que la animación de derrota avance y vuelva a idle
            jugador1.congelar();
            jugador2.congelar();
            if (framesEsperaPostRonda > 0) {
                framesEsperaPostRonda--;
                jugador1.actualizar();
                jugador2.actualizar();
                repaint();
                return;
            } else {
                enEsperaPostRonda = false;
                System.out.println("[Ronda] Fin de espera post‑ronda. Preparando nueva ronda...");
                prepararNuevaRonda();
                return;
            }
        }
        // Espera post-match para terminar audios/animaciones antes de mostrar opciones
        if (enEsperaPostMatch) {
            // Congelar movimiento pero seguir actualizando para que la animación de derrota avance y vuelva a idle
            jugador1.congelar();
            jugador2.congelar();
            if (framesEsperaPostMatch > 0) {
                framesEsperaPostMatch--; 
                jugador1.actualizar();
                jugador2.actualizar();
                repaint();
                return;
            } else {
                enEsperaPostMatch = false;
                // En Modo Historia: ofrecer opciones de progreso o reintento
                if (storyMode) {
                    boolean ganoJ1 = victoriasJ1 > victoriasJ2;
                    if (ganoJ1) {
                        if (nivelesHistoria != null && indiceNivel + 1 < nivelesHistoria.length) {
                            // Aviso de desbloqueo de personaje al completar el nivel
                            try {
                                String unlocked = nombreDisplayDesdeId(nivelesHistoria[indiceNivel].oponenteId);
                                mostrarAlertaHistoriaPixel(
                                        "¡Nuevo personaje desbloqueado: " + unlocked + "!",
                                        "Historia",
                                        new String[]{"OK"},
                                        "OK",
                                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                                );
                            } catch (Exception ignored) {}
                            // Ganó y hay siguiente nivel: preguntar
                            int opt = mostrarAlertaHistoriaPixel(
                                    "¡Nivel superado! ¿Qué deseas hacer?",
                                    "Historia",
                                    new String[]{"Siguiente nivel", "Menú inicial"},
                                    "Siguiente nivel",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE
                            );
                            jugador1.stopAllAudio();
                            jugador2.stopAllAudio();
                            if (timer != null) {
                                try { timer.stop(); } catch (Exception ignore) {}
                            }
                            ventana.dispose();
                            if (opt == 0) {
                                // Ir a selección del siguiente nivel
                                Main.setHistoriaNivelActual(indiceNivel + 1);
                                Main.guardarProgresoHistoria();
                                Main.mostrarMenuYArrancar(true);
                            } else {
                                // Volver al menú inicial PERO conservar el progreso del nivel superado
                                Main.setHistoriaNivelActual(indiceNivel + 1);
                                Main.guardarProgresoHistoria();
                                Main.mostrarPreMenu();
                            }
                            return;
                        } else {
                            // Fin de la historia: cinemática final y pantalla negra con opciones
                            try {
                                String[] textosFin = new String[]{
                                        "HAS LIBERADO EL MULTIVERSO.",
                                        "GRACIAS POR JUGAR."
                                };
                                CinematicManager.mostrarCinematicasBlocking(textosFin);
                                // Registrar tiempo de campaña y mostrar pantalla negra con opciones
                                Main.finalizarCampaniaYRegistrarTiempo();
                                int opt = mostrarPantallaFinalNegraHistoria();
                                jugador1.stopAllAudio();
                                jugador2.stopAllAudio();
                                if (timer != null) {
                                    try { timer.stop(); } catch (Exception ignore) {}
                                }
                                ventana.dispose();
                                if (opt == 0) {
                                    // Reiniciar campaña: vaciar nombre, resetear progreso y solicitar nombre antes de la intro
                                    Main.setHistoriaNombreCampana("");
                                    Main.setHistoriaNivelActual(0);
                                    Main.guardarProgresoHistoria();
                                    Main.reiniciarTemporizadorHistoria();
                                    Main.mostrarIntroHistoria();
                                } else {
                                    // Menú principal tras finalizar: vaciar nombre y resetear progreso para que al entrar se solicite y arranque desde el inicio
                                    Main.setHistoriaNombreCampana("");
                                    Main.setHistoriaNivelActual(0);
                                    Main.guardarProgresoHistoria();
                                    Main.mostrarPreMenu();
                                }
                                return;
                            } catch (Exception ex) {
                                System.err.println("[Historia] Error mostrando cinemática final: " + ex.getMessage());
                            }
                            javax.swing.JOptionPane.showMessageDialog(ventana, "Historia completada", "Historia", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            jugador1.stopAllAudio();
                            jugador2.stopAllAudio();
                            if (timer != null) {
                                try { timer.stop(); } catch (Exception ignore) {}
                            }
                            ventana.dispose();
                            Main.mostrarPreMenu();
                            return;
                        }
                    } else {
                        // Derrota en historia: permitir reintento o volver al inicio
                        int opt = mostrarAlertaHistoriaPixel(
                                "Has sido derrotado. ¿Reintentar este nivel?",
                                "Historia",
                                new String[]{"Reintentar nivel", "Menú inicial"},
                                "Reintentar nivel",
                                javax.swing.JOptionPane.WARNING_MESSAGE
                        );
                        jugador1.stopAllAudio();
                        jugador2.stopAllAudio();
                        if (timer != null) {
                            try { timer.stop(); } catch (Exception ignore) {}
                        }
                        ventana.dispose();
                        if (opt == 0) {
                            Main.setHistoriaNivelActual(indiceNivel);
                            Main.mostrarMenuYArrancar(true);
                        } else {
                            Main.mostrarPreMenu();
                        }
                        return;
                    }
                }
                // PvP clásico: detener loop y mostrar botones de post-partida
        timer.stop();
                botonReiniciar = new JButton("REINICIAR");
                botonReiniciar.setFocusable(false);
                botonReiniciar.setBounds(getWidth()/2 - 75, getHeight()/2 + 50, 150, 50);
                // estilo pixel
                botonReiniciar.setBackground(new Color(10,10,10));
                botonReiniciar.setForeground(new Color(170,0,0));
                botonReiniciar.setFont(new Font("Courier New", Font.BOLD, 18));
                botonReiniciar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(170,0,0), 4),
                    javax.swing.BorderFactory.createLineBorder(new Color(10,10,10), 3)
                ));
                botonReiniciar.addActionListener(ae -> reiniciarJuego());
                this.add(botonReiniciar);
                botonReiniciar.setVisible(true);
                
                // Crear botón para cambiar personajes
                botonCambiar = new JButton("CAMBIAR PERSONAJES");
                botonCambiar.setFocusable(false);
                botonCambiar.setBounds(getWidth()/2 - 125, getHeight()/2 + 110, 250, 45);
                // estilo pixel
                botonCambiar.setBackground(new Color(10,10,10));
                botonCambiar.setForeground(new Color(170,0,0));
                botonCambiar.setFont(new Font("Courier New", Font.BOLD, 16));
                botonCambiar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(170,0,0), 4),
                    javax.swing.BorderFactory.createLineBorder(new Color(10,10,10), 3)
                ));
                botonCambiar.addActionListener(ae -> { 
                    // Detener audios loop (p.ej. respiración) antes de salir
                    jugador1.stopAllAudio();
                    jugador2.stopAllAudio();
                    ventana.dispose();
                    // Redirigir al menú de selección Versus directamente
                    Main.mostrarMenuYArrancar(false);
                });
                this.add(botonCambiar);
                botonCambiar.setVisible(true);
                repaint();
                return;
            }
        }
        if (enPausa) {
            repaint();
            return; // no actualizar lÃ³gica del juego mientras estÃ¡ en pausa
        }
		
            if (enCuentaRegresiva) {
                contadorFrames++;
                if (contadorFrames >= 30) { // medio segundo entre 3,2,1,FIGHT
                    contadorFrames = 0;
                    contadorRegresivo--;
                }
                // Secuenciar audio de comienzo de J2 tras delay dinámico
                if (inicioAudioJ2Pendiente) {
                    framesDelayAudioJ2++;
                    if (framesDelayAudioJ2 >= delayAudioJ2Frames) {
                        jugador2.onCountdownStart();
                        inicioAudioJ2Pendiente = false;
                    }
                }
                if (contadorRegresivo < 0) {
                    enCuentaRegresiva = false; // iniciar el combate
                    jugador1.onFightStart();
                    jugador2.onFightStart();
                    // recuperar foco para asegurar entrada de teclado
                    SwingUtilities.invokeLater(() -> {
                        try {
                            this.setFocusable(true);
                            this.requestFocusInWindow();
                            if (ventana != null) ventana.requestFocus();
                        } catch (Exception ignored) {}
                    });
                    System.out.println("[FIGHT] Cuenta regresiva terminada. Combate iniciado.");
                }
                repaint();
                return; // no actualizar jugadores hasta que termine la cuenta regresiva
            }
        // Cronómetro de partida usando tiempo real (aprox.)
        if (!juegoTerminado) {
            // Acumular 16ms por tick del Timer (ajustable si cambias delay)
            cronometroAcumuladoNanos += 16_000_000L;
            while (cronometroAcumuladoNanos >= 1_000_000_000L) {
                cronometroAcumuladoNanos -= 1_000_000_000L;
                if (segundosRestantes > 0) {
                    segundosRestantes--;
                    if (segundosRestantes <= 0) {
                        tiempoAgotado = true;
                    }
                }
            }
        }

        // Entrada: congelar en desempate, sino procesar normalmente
        if (tiempoAgotado || enCuentaRegresiva) {
            jugador1.congelar();
            jugador2.congelar();
            if (tiempoAgotado) {
                System.out.println("[INPUT] Congelado por tiempo agotado.");
            } else if (enCuentaRegresiva) {
                System.out.println("[INPUT] Congelado por cuenta regresiva en curso. contador=" + contadorRegresivo);
            }
        } else {
            // Activación de selección J1 (F) si el personaje es Ash
            if (input.fPulse && "Ash".equals(jugador1.getPersonajeId()) && !seleccionActivaJ1 && !ashSaliendoJ1 && !pokemonActivoJ1) {
                seleccionActivaJ1 = true;
                seleccionIndexJ1 = 0; // Charizard por defecto
                seleccionPokemonJ1 = "Charizard";
                input.fPulse = false;
            }
            // Activación de selección J2 (L) si el personaje es Ash
            if (input.lPulse && "Ash".equals(jugador2.getPersonajeId()) && !seleccionActivaJ2 && !ashSaliendoJ2 && !pokemonActivoJ2) {
                seleccionActivaJ2 = true;
                seleccionIndexJ2 = 0; // Charizard por defecto
                seleccionPokemonJ2 = "Charizard";
                input.lPulse = false;
            }
            // Procesar selección activa: congelar ambos y navegar/confirmar
        if (seleccionActivaJ1) {
            jugador1.congelar();
            jugador2.congelar();
            // Invertir navegación: A desplaza a la izquierda visualmente, D a la derecha
            if (input.navIzqPulse) {
                int orig = seleccionIndexJ1;
                do { seleccionIndexJ1 = (seleccionIndexJ1 + 1) % 3; }
                while (((seleccionIndexJ1==0 && muertoC) || (seleccionIndexJ1==1 && muertoG) || (seleccionIndexJ1==2 && muertoP)) && seleccionIndexJ1 != orig);
                input.navIzqPulse = false;
            }
            if (input.navDerPulse) {
                int orig = seleccionIndexJ1;
                do { seleccionIndexJ1 = (seleccionIndexJ1 + 2) % 3; }
                while (((seleccionIndexJ1==0 && muertoC) || (seleccionIndexJ1==1 && muertoG) || (seleccionIndexJ1==2 && muertoP)) && seleccionIndexJ1 != orig);
                input.navDerPulse = false;
            }
            // Cancelar selección y volver al estado inicial
            if (input.ePulse) {
                seleccionActivaJ1 = false;
                seleccionPokemonJ1 = null;
                input.ePulse = false;
            }
            seleccionPokemonJ1 = seleccionIndexJ1 == 0 ? "Charizard" : (seleccionIndexJ1 == 1 ? "Greninja" : "Pikachu");
            if (input.ataque1Pulse) {
                input.ataque1Pulse = false;
                // Confirmar sólo si el Pokémon seleccionado no está muerto
                boolean selMuerto = (seleccionIndexJ1==0 && muertoC) || (seleccionIndexJ1==1 && muertoG) || (seleccionIndexJ1==2 && muertoP);
                if (!selMuerto) {
                    seleccionActivaJ1 = false;
                    // Mostrar derrota de Ash sin audio para animar su salida
                    jugador1.onGameOver(false);
                    // permitir salida fuera de límites
                    jugador1.setIgnorarLimitesHorizontales(true);
                    ashSaliendoJ1 = true; dirSalidaAshJ1 = -1;
                }
            }
        }
        if (seleccionActivaJ2) {
            jugador1.congelar();
            jugador2.congelar();
            // Navegación con flechas para J2
            if (input.navIzq2Pulse) {
                int orig2 = seleccionIndexJ2;
                do { seleccionIndexJ2 = (seleccionIndexJ2 + 1) % 3; }
                while (((seleccionIndexJ2==0 && muertoC) || (seleccionIndexJ2==1 && muertoG) || (seleccionIndexJ2==2 && muertoP)) && seleccionIndexJ2 != orig2);
                input.navIzq2Pulse = false;
            }
            if (input.navDer2Pulse) {
                int orig2 = seleccionIndexJ2;
                do { seleccionIndexJ2 = (seleccionIndexJ2 + 2) % 3; }
                while (((seleccionIndexJ2==0 && muertoC) || (seleccionIndexJ2==1 && muertoG) || (seleccionIndexJ2==2 && muertoP)) && seleccionIndexJ2 != orig2);
                input.navDer2Pulse = false;
            }
            seleccionPokemonJ2 = seleccionIndexJ2 == 0 ? "Charizard" : (seleccionIndexJ2 == 1 ? "Greninja" : "Pikachu");
            if (input.ataque2Pulse) {
                input.ataque2Pulse = false;
                boolean selMuerto2 = (seleccionIndexJ2==0 && muertoC) || (seleccionIndexJ2==1 && muertoG) || (seleccionIndexJ2==2 && muertoP);
                if (!selMuerto2) {
                    seleccionActivaJ2 = false;
                    // Mostrar derrota de Ash sin audio para animar su salida
                    jugador2.onGameOver(false);
                    jugador2.setIgnorarLimitesHorizontales(true);
                    ashSaliendoJ2 = true; dirSalidaAshJ2 = 1;
                }
            }
        }
        // Movimiento de salida de Ash (KO animado fuera de pantalla)
        if (ashSaliendoJ1) {
            jugador1.setX(jugador1.getX() + dirSalidaAshJ1 * 8);
            if (jugador1.getX() < -200) {
                ashSaliendoJ1 = false;
                jugador1.setIgnorarLimitesHorizontales(false);
                aplicarPersonaje(jugador1, seleccionPokemonJ1);
                jugador1.setX(100);
                pokemonActivoJ1 = true;
                pokemonVidaJ1 = 200;
                jugador1.resetParaNuevaRonda();
                // Reproducir el audio de comienzo del Pokémon invocado
                jugador1.onCountdownStart();
            }
        }
        if (ashSaliendoJ2) {
            jugador2.setX(jugador2.getX() + dirSalidaAshJ2 * 8);
            if (jugador2.getX() > baseWidth + 200 - jugador2.getBounds().width) {
                ashSaliendoJ2 = false;
                jugador2.setIgnorarLimitesHorizontales(false);
                aplicarPersonaje(jugador2, seleccionPokemonJ2);
                jugador2.setX(500);
                pokemonActivoJ2 = true;
                pokemonVidaJ2 = 200;
                jugador2.resetParaNuevaRonda();
                // Reproducir el audio de comienzo del Pokémon invocado
                jugador2.onCountdownStart();
            }
        }

            // Jugador 1
            if (!seleccionActivaJ1 && !ashSaliendoJ1 && !pokemonVolviendoJ1) {
                if (input.izquierda1) jugador1.moverIzquierda();
                else if (input.derecha1) jugador1.moverDerecha();
                else jugador1.detener();
                jugador1.setAgachado(input.agachar1);
                if (input.salto1Pulse) { jugador1.saltar(); input.salto1Pulse = false; }
                if (input.ataque1Pulse) { jugador1.atacar(); input.ataque1Pulse = false; }
            } else {
                jugador1.detener();
                jugador1.setAgachado(false);
            }

            // Jugador 2: manual en PvP, controlado por IA en Historia
            if (!seleccionActivaJ2 && !ashSaliendoJ2 && !pokemonVolviendoJ2) {
                if (storyMode) {
                    if (botIA != null) botIA.actualizar(jugador2, jugador1);
                    else { jugador2.detener(); jugador2.setAgachado(false); }
                } else {
                    if (input.izquierda2) jugador2.moverIzquierda();
                    else if (input.derecha2) jugador2.moverDerecha();
                    else jugador2.detener();
                    jugador2.setAgachado(input.agachar2);
                    if (input.salto2Pulse) { jugador2.saltar(); input.salto2Pulse = false; }
                    if (input.ataque2Pulse) { jugador2.atacar(); input.ataque2Pulse = false; }
                }
            } else {
                jugador2.detener();
                jugador2.setAgachado(false);
            }
        }
        // Actualización de estado física/animación (una vez por frame)
        jugador1.actualizar();
        jugador2.actualizar();

        if (!juegoTerminado) {
            
            // Agotamiento del tiempo: drenar vida de ambos hasta que alguno llegue a 0
            if (tiempoAgotado) {
                jugador1.recibirDano(drainRatePorFrame);
                jugador2.recibirDano(drainRatePorFrame);
            }
            
            // Detectar colisiones y ataques (solo si no hay desempate)
        if (!tiempoAgotado && !enCuentaRegresiva) {
            if (jugador1.atacando && !jugador1.haGolpeado() && jugador1.getBounds().intersects(jugador2.getBounds())) {
                boolean invulBoss = storyMode && bossFinalActivo && bossInvulFrames > 0;
                if (!invulBoss) {
                    if (pokemonActivoJ2) {
                        pokemonVidaJ2 = Math.max(0, pokemonVidaJ2 - 9);
                    } else {
                        jugador2.recibirDano(9);
                    }
                }
                jugador1.marcarGolpeado();
            }
            if (jugador2.atacando && !jugador2.haGolpeado() && jugador2.getBounds().intersects(jugador1.getBounds())) {
                int dano = 9;
                if (storyMode && bossFinalActivo && jugador2.getVida() <= (jugador2.getVidaMax() * 0.10)) {
                    // One-shot cuando el jefe <=10% de su vida
                    dano = jugador1.getVida();
                }
                if (pokemonActivoJ1) {
                    pokemonVidaJ1 = Math.max(0, pokemonVidaJ1 - dano);
                } else {
                    jugador1.recibirDano(dano);
                }
                jugador2.marcarGolpeado();
            }
        }
        }
        // KO del pokémon J1 y regreso a Ash sin terminar ronda
        if (pokemonActivoJ1 && pokemonVidaJ1 <= 0 && !pokemonVolviendoJ1) {
            jugador1.onGameOver(true);
            // Marcar al seleccionado como muerto para el menú
            if ("Charizard".equals(seleccionPokemonJ1)) muertoC = true;
            else if ("Greninja".equals(seleccionPokemonJ1)) muertoG = true;
            else if ("Pikachu".equals(seleccionPokemonJ1)) muertoP = true;
            pokemonVolviendoJ1 = true;
            dirSalidaAshJ1 = -1;
            // iniciar efecto de shrink y permitir salida
            jugador1.setIgnorarLimitesHorizontales(true);
            shrinkAlturaJ1 = 300;
            jugador1.setAlturaVisualOverride(shrinkAlturaJ1);
            // Calcular espera según duración del audio de GameOver del Pokémon
            try {
                int audioFramesKO = (int) Math.ceil(jugador1.getGameOverAudioLengthMicros() / 16000.0);
                esperaAudioPokemonKOJ1 = Math.min(Math.max(audioFramesKO, 90), 240);
            } catch (Exception ignored) { esperaAudioPokemonKOJ1 = 120; }
        }
        if (pokemonVolviendoJ1) {
            jugador1.setX(jugador1.getX() + dirSalidaAshJ1 * 8);
            // reducir altura visual progresivamente simulando regreso a pokebola
            shrinkAlturaJ1 = Math.max(0, shrinkAlturaJ1 - 6);
            jugador1.setAlturaVisualOverride(shrinkAlturaJ1);
            if (esperaAudioPokemonKOJ1 > 0) esperaAudioPokemonKOJ1--;
            if (jugador1.getX() < -200 && esperaAudioPokemonKOJ1 <= 0) {
                pokemonVolviendoJ1 = false;
                pokemonActivoJ1 = false;
                aplicarPersonaje(jugador1, "Ash");
                jugador1.setX(100);
                // Al volver Ash, no reproducir Game Over. Dejarlo en idle y retomar loop.
                jugador1.forceIdle();
                jugador1.onFightStart();
                jugador1.clearAlturaVisualOverride();
                jugador1.setIgnorarLimitesHorizontales(false);
                shrinkAlturaJ1 = 300;
            }
        }

        // KO del pokémon J2 y regreso a Ash sin terminar ronda
        if (pokemonActivoJ2 && pokemonVidaJ2 <= 0 && !pokemonVolviendoJ2) {
            jugador2.onGameOver(true);
            if ("Charizard".equals(seleccionPokemonJ2)) muertoC = true;
            else if ("Greninja".equals(seleccionPokemonJ2)) muertoG = true;
            else if ("Pikachu".equals(seleccionPokemonJ2)) muertoP = true;
            pokemonVolviendoJ2 = true;
            dirSalidaAshJ2 = 1;
            jugador2.setIgnorarLimitesHorizontales(true);
            shrinkAlturaJ2 = 300;
            jugador2.setAlturaVisualOverride(shrinkAlturaJ2);
            try {
                int audioFramesKO2 = (int) Math.ceil(jugador2.getGameOverAudioLengthMicros() / 16000.0);
                esperaAudioPokemonKOJ2 = Math.min(Math.max(audioFramesKO2, 90), 240);
            } catch (Exception ignored) { esperaAudioPokemonKOJ2 = 120; }
        }
        if (pokemonVolviendoJ2) {
            jugador2.setX(jugador2.getX() + dirSalidaAshJ2 * 8);
            shrinkAlturaJ2 = Math.max(0, shrinkAlturaJ2 - 6);
            jugador2.setAlturaVisualOverride(shrinkAlturaJ2);
            if (esperaAudioPokemonKOJ2 > 0) esperaAudioPokemonKOJ2--;
            if (jugador2.getX() > baseWidth + 200 - jugador2.getBounds().width && esperaAudioPokemonKOJ2 <= 0) {
                pokemonVolviendoJ2 = false;
                pokemonActivoJ2 = false;
                aplicarPersonaje(jugador2, "Ash");
                jugador2.setX(500);
                jugador2.forceIdle();
                jugador2.onFightStart();
                jugador2.clearAlturaVisualOverride();
                jugador2.setIgnorarLimitesHorizontales(false);
                shrinkAlturaJ2 = 300;
            }
        }

     // detección de vida (fin de ronda y mejor de 3)
        if (jugador1.getVida() <= 0 || jugador2.getVida() <= 0) {
            boolean empate = jugador1.getVida() <= 0 && jugador2.getVida() <= 0;
            if (empate) {
                mensajeVictoria = "EMPATE!";
            } else if (jugador1.getVida() <= 0) {
                victoriasJ2++;
                mensajeVictoria = jugador2.getNombre() + " GANA RONDA!";
            } else {
                victoriasJ1++;
                mensajeVictoria = jugador1.getNombre() + " GANA RONDA!";
            }
            boolean finMatch = (victoriasJ1 >= 2 || victoriasJ2 >= 2);
            boolean j1Derrotado = jugador1.getVida() <= 0;
            boolean j2Derrotado = jugador2.getVida() <= 0;

            if (finMatch) {
                juegoTerminado = true;
                // Mensaje final de campeón
                if (victoriasJ1 > victoriasJ2) {
                    mensajeVictoria = jugador1.getNombre() + " CAMPEON!";
                } else if (victoriasJ2 > victoriasJ1) {
                    mensajeVictoria = jugador2.getNombre() + " CAMPEON!";
                } else {
                    mensajeVictoria = "EMPATE FINAL!";
                }
                // Reproducir Game_Over del derrotado y dejar ganador en idle
                if (empate) {
                    jugador1.onGameOver();
                    jugador2.onGameOver();
                } else if (j1Derrotado) {
                    jugador1.onGameOver();
                    jugador2.stopAllAudio();
                    jugador2.forceIdle();
                } else {
                    jugador2.onGameOver();
                    jugador1.stopAllAudio();
                    jugador1.forceIdle();
                }
                long derrotadoMicros = Math.max(
                    j1Derrotado ? jugador1.getGameOverAudioLengthMicros() : 0L,
                    j2Derrotado ? jugador2.getGameOverAudioLengthMicros() : 0L
                );
                int audioFrames = (int) Math.ceil(derrotadoMicros / 16000.0);
                // Limitar espera post-match para evitar bloqueos prolongados
                framesEsperaPostMatch = Math.min(Math.max(audioFrames, 180), 300);
                enEsperaPostMatch = true;
            } else {
                // Fin de ronda sin terminar match
                if (empate) {
                    jugador1.onGameOver();
                    jugador2.onGameOver();
                } else if (j1Derrotado) {
                    jugador1.onGameOver();
                    jugador2.stopAllAudio();
                    jugador2.forceIdle();
                } else {
                    jugador2.onGameOver();
                    jugador1.stopAllAudio();
                    jugador1.forceIdle();
                }
                long derrotadoMicros = Math.max(
                    j1Derrotado ? jugador1.getGameOverAudioLengthMicros() : 0L,
                    j2Derrotado ? jugador2.getGameOverAudioLengthMicros() : 0L
                );
                int audioFrames = (int) Math.ceil(derrotadoMicros / 16000.0);
                // Limitar espera post-ronda para una transición más ágil
                framesEsperaPostRonda = Math.min(Math.max(audioFrames, 120), 240);
                enEsperaPostRonda = true;
                System.out.println("[Ronda] Derrota= " + (j1Derrotado ? "J1" : j2Derrotado ? "J2" : "Empate") +
                        ", framesEsperaPostRonda=" + framesEsperaPostRonda +
                        ", marcador J1=" + victoriasJ1 + " J2=" + victoriasJ2);
            }
        }

        
        repaint();
        } catch (Throwable t) {
            System.err.println("[Timer] Excepción no capturada en actionPerformed: " + t.getMessage());
        }
    }

    // Diálogo con estilo pixel para opciones de historia (bloqueante)
    private int mostrarAlertaHistoriaPixel(String mensaje, String titulo, String[] opciones, String defaultOption, int messageType) {
        JDialog dialog = new JDialog(ventana, titulo, true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(600, 260);
        dialog.setLocationRelativeTo(ventana);

        Color PIXEL_BLACK = new Color(10, 10, 10);
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(new Color(20, 20, 20, 230));
        container.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
        ));

        JLabel lbl = new JLabel(mensaje, SwingConstants.CENTER);
        lbl.setForeground(PIXEL_RED);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 20));
        container.add(lbl, BorderLayout.CENTER);

        JPanel botones = new JPanel();
        botones.setOpaque(false);
        int[] result = new int[]{-1};
        for (int i = 0; i < opciones.length; i++) {
            String texto = opciones[i];
            JButton b = new JButton(texto);
            b.setFont(new Font("Monospaced", Font.BOLD, 18));
            b.setBackground(PIXEL_RED_DARK);
            b.setForeground(Color.BLACK);
            b.setFocusPainted(false);
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                    javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
            ));
            final int idx = i;
            b.addActionListener(e -> { result[0] = idx; dialog.dispose(); });
            botones.add(b);
            if (defaultOption != null && defaultOption.equals(texto)) {
                dialog.getRootPane().setDefaultButton(b);
            }
        }
        container.add(botones, BorderLayout.SOUTH);
        dialog.setContentPane(container);
        dialog.setVisible(true);
        return result[0] < 0 ? 0 : result[0];
    }
    
    // Pantalla final negra con mensaje y opciones (bloqueante)
    private int mostrarPantallaFinalNegraHistoria() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(ventana, true);
        dialog.setUndecorated(true);
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screen.width, screen.height);
        dialog.setLocationRelativeTo(null);

        java.awt.Color PIXEL_RED = new java.awt.Color(170, 0, 0);
        java.awt.Color PIXEL_RED_DARK = new java.awt.Color(120, 0, 0);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(java.awt.Color.BLACK);

        javax.swing.JLabel titulo = new javax.swing.JLabel("has liberado el multiverso", javax.swing.SwingConstants.CENTER);
        titulo.setForeground(java.awt.Color.WHITE);
        titulo.setFont(new java.awt.Font("Courier New", java.awt.Font.BOLD, 42));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(120, 20, 40, 20));
        root.add(titulo, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel botones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 24, 24));
        botones.setBackground(java.awt.Color.BLACK);

        javax.swing.JButton bReiniciar = new javax.swing.JButton("Reiniciar campaña");
        javax.swing.JButton bMenu = new javax.swing.JButton("Menú principal");
        bReiniciar.setFont(new java.awt.Font("Courier New", java.awt.Font.BOLD, 20));
        bMenu.setFont(new java.awt.Font("Courier New", java.awt.Font.BOLD, 20));
        bReiniciar.setBackground(PIXEL_RED_DARK);
        bMenu.setBackground(PIXEL_RED_DARK);
        bReiniciar.setForeground(java.awt.Color.BLACK);
        bMenu.setForeground(java.awt.Color.BLACK);
        bReiniciar.setFocusPainted(false);
        bMenu.setFocusPainted(false);
        bReiniciar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
        ));
        bMenu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
        ));
        botones.add(bReiniciar);
        botones.add(bMenu);
        root.add(botones, java.awt.BorderLayout.SOUTH);

        final int[] result = new int[]{1};
        bReiniciar.addActionListener(e -> { result[0] = 0; dialog.dispose(); });
        bMenu.addActionListener(e -> { result[0] = 1; dialog.dispose(); });
        dialog.getRootPane().setDefaultButton(bReiniciar);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return result[0];
    }
    

    private void reiniciarJuego() {
        // Reset jugadores
        jugador1.setVida(jugador1.getVidaMax());
        jugador2.setVida(jugador2.getVidaMax());
        jugador1.setX(100);
        jugador2.setX(500);

        jugador1.stopAllAudio();
        jugador2.stopAllAudio();

        juegoTerminado = false;
        juegoIniciado = false;
        contadorInicio = 3;
        enCuentaRegresiva = true;
        contadorRegresivo = 3;
        contadorFrames = 0;
        segundosRestantes = obtenerSegundosIniciales();
        framesTimer = 0;
        cronometroAcumuladoNanos = 0L;
        tiempoAgotado = false;
        mensajeVictoria = "";
        enPausa = false; // aseguramos reanudar el juego
        victoriasJ1 = 0;
        victoriasJ2 = 0;
        // audio/animación de inicio
        inicioAudioLanzado = false;
        jugador1.onCountdownStart();
        inicioAudioJ2Pendiente = true; framesDelayAudioJ2 = 0;
        int framesIntroJ1 = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
        // Asegurar que el audio de inicio de J2 ocurra dentro de la cuenta regresiva
        delayAudioJ2Frames = Math.min(Math.max(framesIntroJ1 + 10, FRAMES_ESPERA_AUDIO_J2), 100);
        System.out.println("[DEBUG] delayAudioJ2Frames reinicio=" + delayAudioJ2Frames);

        // Quitar botones
        if (botonReiniciar != null) {
            botonReiniciar.setVisible(false);
            this.remove(botonReiniciar);
        }
        if (botonCambiar != null) {
            botonCambiar.setVisible(false);
            this.remove(botonCambiar);
        }

        // Reiniciar timer principal
        timer.start();

        // Recuperar foco para que el input funcione
        this.setFocusable(true);
        this.requestFocusInWindow();
        ventana.requestFocus();

        repaint();
    }

    private void prepararNuevaRonda() {
        jugador1.setVida(jugador1.getVidaMax());
        jugador2.setVida(jugador2.getVidaMax());
        jugador1.setX(100);
        jugador2.setX(500);
        // Reset por ronda para evitar audio doble y asegurar animación idle
        jugador1.resetParaNuevaRonda();
        jugador2.resetParaNuevaRonda();
        // reset estados de ronda
        tiempoAgotado = false;
        segundosRestantes = obtenerSegundosIniciales();
        framesTimer = 0;
        cronometroAcumuladoNanos = 0L;
        enCuentaRegresiva = true;
        contadorRegresivo = 3;
        contadorFrames = 0;
        mensajeVictoria = "";
        // audio/animación de inicio de ronda: primero J1, luego J2 con delay
        inicioAudioJ2Pendiente = true;
        framesDelayAudioJ2 = 0;
        jugador1.onCountdownStart();
        int framesIntroJ1b = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
        delayAudioJ2Frames = Math.min(Math.max(framesIntroJ1b + 10, FRAMES_ESPERA_AUDIO_J2), 100);
        System.out.println("[DEBUG] delayAudioJ2Frames ronda=" + delayAudioJ2Frames);
        // continuar loop
        if (!timer.isRunning()) timer.start();
        repaint();
    }

    // Conversión de id interno a nombre mostrable
    private String nombreDisplayDesdeId(String id) {
        if (id == null) return "-";
        return switch (id) {
            case "Mr_Increible" -> "Mr. Increíble";
            case "Iron_Man" -> "Iron Man";
            case "Luke_Skywalker" -> "Luke Skywalker";
            case "Darth_Vader" -> "Darth Vader";
            default -> id.replace('_', ' ');
        };
    }

    // --- API de Modo Historia ---
    public void activarStoryMode(Level[] niveles) {
        this.storyMode = true;
        this.nivelesHistoria = niveles;
        // Usar el progreso actual guardado desde Main, si existe
        try {
            this.indiceNivel = Math.max(0, Math.min(niveles != null ? niveles.length - 1 : 0, Main.getHistoriaNivelActual()));
        } catch (Exception ex) {
            this.indiceNivel = 0;
        }
        if (this.botIA == null) {
            try {
                this.botIA = new BotIA();
            } catch (Throwable t) {
                System.err.println("[Historia] Error creando BotIA: " + t.getMessage());
                this.botIA = null; // Continuar sin IA para no bloquear el combate
            }
        }
        System.out.println("[Historia] Activando Story Mode. indiceNivel=" + this.indiceNivel);
        try {
            if (this.indiceNivel == 0) {
                Main.iniciarTemporizadorHistoria();
            }
        } catch (Exception ignored) {}
        try {
            aplicarNivelActual();
        } catch (Throwable t) {
            System.err.println("[Historia] Falló aplicar nivel actual: " + t.getMessage());
            // Desactivar IA para evitar romper el bucle del juego
            this.botIA = null;
            // Aún así inicializar HUD y cuenta regresiva segura
            try {
                enCuentaRegresiva = true;
                contadorRegresivo = 3;
                contadorFrames = 0;
                segundosRestantes = obtenerSegundosIniciales();
                cronometroAcumuladoNanos = 0L;
                tiempoAgotado = false;
                mensajeVictoria = "";
                if (!timer.isRunning()) timer.start();
                repaint();
            } catch (Exception ignored) {}
        }
    }

    private void aplicarNivelActual() {
        if (nivelesHistoria == null || indiceNivel < 0 || indiceNivel >= nivelesHistoria.length) return;
        Level lvl = nivelesHistoria[indiceNivel];
        // Fondo definido por el nivel
        if (lvl.fondoArchivo != null && !lvl.fondoArchivo.isEmpty()) {
            try {
                this.fondoSel = lvl.fondoArchivo;
                cargarFondo(lvl.fondoArchivo);
            } catch (Exception ex) {
                System.err.println("[Historia] No se pudo cargar fondo: " + ex.getMessage());
            }
        }
        // Oponente
        try {
            aplicarPersonaje(jugador2, lvl.oponenteId);
            System.out.println("[Historia] Nivel aplicado: fondo=" + lvl.fondoArchivo + ", J2=" + jugador2.getNombre());
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo aplicar oponente: " + ex.getMessage());
        }
        // Vida máxima y reglas de jefe final
        bossFinalActivo = lvl.esFinalBoss && !Main.MODO_TEST_BOSS_SUAVE;
        try { jugador1.setVidaMax(100); } catch (Exception ignored) {}
        try { jugador2.setVidaMax(bossFinalActivo ? 500 : 100); } catch (Exception ignored) {}
        jugador1.setVida(jugador1.getVidaMax());
        jugador2.setVida(jugador2.getVidaMax());
        bossCycleFrames = 0;
        bossInvulFrames = 0;
        // Configuración de IA
        if (botIA != null) {
            try {
                botIA.setNivel(lvl.dificultadIA);
                botIA.setOverrides(lvl.overrideRangoAtaque, lvl.overrideVelAcercamiento, lvl.overrideCooldownAtaque,
                        lvl.overrideProbSalto, lvl.overrideProbAgachar, lvl.overrideDistEvadir, lvl.overrideAgresividad);
            } catch (Throwable t) {
                System.err.println("[Historia][IA] Falló configuración de IA: " + t.getMessage());
                // Desactivar IA para no romper el combate; J2 quedará manual/inactivo según modo
                botIA = null;
            }
        }
        // HUD y reloj
        victoriasJ1 = 0;
        victoriasJ2 = 0;
        enCuentaRegresiva = true;
        contadorRegresivo = 3;
        contadorFrames = 0;
        segundosRestantes = obtenerSegundosIniciales();
        framesTimer = 0;
        cronometroAcumuladoNanos = 0L;
        tiempoAgotado = false;
        mensajeVictoria = "";
        // audio de inicio
        inicioAudioJ2Pendiente = true;
        framesDelayAudioJ2 = 0;
        try {
            jugador1.onCountdownStart();
            int framesIntroJ1 = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
            delayAudioJ2Frames = Math.min(Math.max(framesIntroJ1 + 10, FRAMES_ESPERA_AUDIO_J2), 100);
        } catch (Exception ignored) {}
        if (!timer.isRunning()) timer.start();
        repaint();
    }

    /**
     * Determina los segundos iniciales por ronda en función del oponente.
     * Para combates contra Darth Vader se usan 150s, caso contrario 90s.
     */
    private int obtenerSegundosIniciales() {
        try {
            String oppId = (jugador2 != null) ? jugador2.getPersonajeId() : null;
            if ("Darth_Vader".equals(oppId)) return 150;
        } catch (Exception ignored) {}
        return 90;
    }

    private void mostrarMenuPausa() {
        // Aplicar paleta al JOptionPane (temporal)
        Color PIXEL_BLACK = new Color(10,10,10);
        Color PIXEL_RED = new Color(170,0,0);
        Color PIXEL_RED_DARK = new Color(120,0,0);
        Font PIXEL_FONT = new Font("Courier New", Font.BOLD, 14);
        Object oldBg = javax.swing.UIManager.getDefaults().get("OptionPane.background");
        Object oldPanelBg = javax.swing.UIManager.getDefaults().get("Panel.background");
        Object oldMsgFg = javax.swing.UIManager.getDefaults().get("OptionPane.messageForeground");
        Object oldBtnBg = javax.swing.UIManager.getDefaults().get("Button.background");
        Object oldBtnFg = javax.swing.UIManager.getDefaults().get("Button.foreground");
        Object oldBtnFont = javax.swing.UIManager.getDefaults().get("Button.font");
        Object oldMsgFont = javax.swing.UIManager.getDefaults().get("OptionPane.messageFont");
        try {
            javax.swing.UIManager.put("OptionPane.background", PIXEL_BLACK);
            javax.swing.UIManager.put("Panel.background", PIXEL_BLACK);
            javax.swing.UIManager.put("OptionPane.messageForeground", Color.BLACK);
            javax.swing.UIManager.put("Button.background", PIXEL_RED);
            javax.swing.UIManager.put("Button.foreground", Color.BLACK);
            javax.swing.UIManager.put("Button.font", PIXEL_FONT);
            javax.swing.UIManager.put("OptionPane.messageFont", PIXEL_FONT);
            String[] opciones = {"Controles", "Pantalla", "Despausar", "Menu principal", "Salir"};
            int seleccion = JOptionPane.showOptionDialog(
                    ventana,
                    "PAUSA",
                    "Pausa",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );
            if (seleccion == 0) {
                mostrarControles();
                enPausa = false;
                SwingUtilities.invokeLater(() -> {
                    try { this.requestFocusInWindow(); if (ventana != null) ventana.requestFocus(); } catch (Exception ignored) {}
                });
            } else if (seleccion == 1) {
                mostrarMenuResolucion();
                enPausa = false;
                SwingUtilities.invokeLater(() -> {
                    try { this.requestFocusInWindow(); if (ventana != null) ventana.requestFocus(); } catch (Exception ignored) {}
                });
            } else if (seleccion == 2) {
                enPausa = false;
                SwingUtilities.invokeLater(() -> {
                    try { this.requestFocusInWindow(); if (ventana != null) ventana.requestFocus(); } catch (Exception ignored) {}
                });
            } else if (seleccion == 3) {
                // Volver al menú principal sin cerrar la aplicación
                // Detener timer y audios, y volver al menú
                if (timer != null && timer.isRunning()) timer.stop();
                jugador1.stopAllAudio();
                jugador2.stopAllAudio();
                ventana.dispose();
                Main.mostrarMenuYArrancar();
            } else if (seleccion == 4) {
                timer.stop();
                ventana.dispose();
                System.exit(0);
            } else {
                enPausa = false;
                SwingUtilities.invokeLater(() -> {
                    try { this.requestFocusInWindow(); if (ventana != null) ventana.requestFocus(); } catch (Exception ignored) {}
                });
            }
        } finally {
            javax.swing.UIManager.put("OptionPane.background", oldBg);
            javax.swing.UIManager.put("Panel.background", oldPanelBg);
            javax.swing.UIManager.put("OptionPane.messageForeground", oldMsgFg);
            javax.swing.UIManager.put("Button.background", oldBtnBg);
            javax.swing.UIManager.put("Button.foreground", oldBtnFg);
            javax.swing.UIManager.put("Button.font", oldBtnFont);
            javax.swing.UIManager.put("OptionPane.messageFont", oldMsgFont);
        }
    }

    private void mostrarMenuResolucion() {
        String[] resols = {"640 x 480", "960 x 540", "1280 x 720", "1920 x 1080"};
        int sel = JOptionPane.showOptionDialog(
                ventana,
                "Selecciona resolucion",
                "Resolucion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                resols,
                resols[2]
        );
        switch (sel) {
            case 0 -> cambiarResolucion(640, 480);
            case 1 -> cambiarResolucion(960, 540);
            case 2 -> cambiarResolucion(1280, 720);
            case 3 -> cambiarResolucion(1920, 1080);
            default -> {}
        }
        // Recuperar foco tras cerrar el diÃ¡logo
        this.requestFocusInWindow();
        ventana.requestFocus();
    }

    private void mostrarControles() {
        String controles = "Jugador 1:\n" +
                "- Izquierda: A\n" +
                "- Derecha: D\n" +
                "- Saltar: W\n" +
                "- Agacharse: S\n" +
                "- Atacar: R\n\n" +
                "Jugador 2:\n" +
                "- Izquierda: Flecha Izquierda\n" +
                "- Derecha: Flecha Derecha\n" +
                "- Saltar: Flecha Arriba\n" +
                "- Agacharse: Flecha Abajo\n" +
                "- Atacar: Enter";

        // Controles especiales de Ash (selección de Pokémon) si está en combate
        boolean ashEnCombate =
                (jugador1 != null && "Ash".equals(jugador1.getPersonajeId())) ||
                (jugador2 != null && "Ash".equals(jugador2.getPersonajeId()));
        if (ashEnCombate) {
            controles += "\n\nAsh (Selección de Pokémon):\n" +
                    "- J1: Abrir selección: F\n" +
                    "- J1: Navegar selección: A/D\n" +
                    "- J1: Confirmar selección: R\n" +
                    "- J1: Cancelar selección: E\n" +
                    "- J2: Abrir selección: L\n" +
                    "- J2: Navegar selección: Flecha Izquierda/Derecha\n" +
                    "- J2: Confirmar selección: Enter";
        }
        JOptionPane.showMessageDialog(ventana, controles, "Controles", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cambiarResolucion(int w, int h) {
        // Salir de maximizado para aplicar tamaÃ±o custom
        ventana.setExtendedState(JFrame.NORMAL);
        ventana.setSize(w, h);
        this.setPreferredSize(new Dimension(w, h));
        ventana.validate();
        revalidate();
        repaint();
        // Recuperar foco para que el input funcione
        this.requestFocusInWindow();
        ventana.requestFocus();
    }

    private void cargarFondo(String archivo) {
        try {
            // 1) Intentar por classpath
            java.net.URL url = getClass().getResource("/resources/BackGround/" + archivo);
            if (url != null) {
                fondoImg = new ImageIcon(url).getImage();
                return;
            }
            // 2) Fallback: bin/resources
            java.io.File fBin = new java.io.File("bin/resources/BackGround/" + archivo);
            if (fBin.exists()) {
                fondoImg = new ImageIcon(fBin.toURI().toURL()).getImage();
                return;
            }
            // 3) Fallback: src/resources
            java.io.File fSrc = new java.io.File("src/resources/BackGround/" + archivo);
            if (fSrc.exists()) {
                fondoImg = new ImageIcon(fSrc.toURI().toURL()).getImage();
                return;
            }
            fondoImg = null;
            System.err.println("Fondo no encontrado: " + archivo);
        } catch (Exception ex) {
            fondoImg = null;
            ex.printStackTrace();
        }
    }

    public void aplicarSeleccionInicial(String pj1, String pj2, String fondo) {
        if (pj1 != null) aplicarPersonaje(jugador1, pj1);
        if (pj2 != null) aplicarPersonaje(jugador2, pj2);
        if (fondo != null) { fondoSel = fondo; cargarFondo(fondoSel); }
        // Resetear estados de audio/animación al seleccionar
        jugador1.resetParaNuevaRonda();
        jugador2.resetParaNuevaRonda();
        // Iniciar audio secuencial: primero J1, luego J2 tras delay dinámico
        inicioAudioJ2Pendiente = true;
        framesDelayAudioJ2 = 0;
        if (jugador1 != null) {
            jugador1.onCountdownStart();
            int framesIntroJ1 = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
            delayAudioJ2Frames = Math.min(Math.max(framesIntroJ1 + 10, FRAMES_ESPERA_AUDIO_J2), 100);
            System.out.println("[DEBUG] delayAudioJ2Frames seleccionInicial=" + delayAudioJ2Frames);
        }
        // J2 se dispara tras delayAudioJ2Frames dentro del loop
    }

    private void mostrarSeleccionPersonajes() {
        String[] personajes = {"Darth_Vader","Iron_Man","Mr_Increible","Pyke","Goku","Batman","Luke_Skywalker","Naruto"};
        personajeSelJ1 = (String) JOptionPane.showInputDialog(ventana, "Jugador 1: elige personaje", "Selección J1", JOptionPane.PLAIN_MESSAGE, null, personajes, personajes[0]);
        personajeSelJ2 = (String) JOptionPane.showInputDialog(ventana, "Jugador 2: elige personaje", "Selección J2", JOptionPane.PLAIN_MESSAGE, null, personajes, personajes[1]);
    }

    private void aplicarPersonaje(Jugador jugador, String personajeId) {
        jugador.stopAllAudio();
        String images = "";
        String sounds = "";
        String idlePath = null, walkPath = null, crouchPath = null, attackPath = null, hurtPath = null, koPath = null, jumpPath = null;
        String inicio = null, resp = null, dano = null, gameover = null, saltoAtaque = null;
        String audioSalto = null, audioAtaque = null;
        int derrotaFrames = 90;
        switch (personajeId) {
            case "Ash" -> {
                images = "Pokemon/Ash/images/";
                sounds = "Pokemon/Ash/sounds/";
                idlePath = images + "ash_idle.png";
                walkPath = images + "ash_caminar.gif";
                crouchPath = images + "ash_agachar_saltar.png";
                attackPath = images + "ash_atacar1.gif"; // attack 1 por defecto
                hurtPath = images + "ash_daño.png";
                koPath = images + "ash_gameOver.gif";
                jumpPath = images + "ash_agachar_saltar.png";
                // Audios estandarizados
                derrotaFrames = 90;
                // Comienzo y GameOver dedicados
                inicio = sounds + "Ash_Comienzo.wav";
                gameover = sounds + "Ash_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Charizard" -> {
                images = "Pokemon/Charizard/";
                sounds = "Pokemon/Charizard/sounds/";
                idlePath = images + "charizard_idle_caminar.gif";
                walkPath = images + "charizard_idle_caminar.gif";
                crouchPath = images + "charizard.png"; // reutilizamos
                attackPath = images + "charizard_atacar.gif";
                hurtPath = images + "charizard_daño.png";
                koPath = null; // no disponible
                jumpPath = images + "charizard.png";
                derrotaFrames = 90;
                // Comienzo y GameOver dedicados
                inicio = sounds + "Charizard_Comienzo.wav";
                gameover = sounds + "Charizard_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Greninja" -> {
                images = "Pokemon/Greninja/";
                sounds = "Pokemon/Greninja/sounds/";
                idlePath = images + "greninja_idle.png";
                walkPath = images + "greninja_caminar.gif";
                crouchPath = images + "greninja_idle.png";
                attackPath = images + "greninja_atacar.gif";
                hurtPath = images + "greninja_daño.png";
                koPath = null;
                jumpPath = images + "greninja_idle.png";
                derrotaFrames = 90;
                // Comienzo y GameOver dedicados
                inicio = sounds + "Greninja_Comienzo.wav";
                gameover = sounds + "Greninja_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Pikachu" -> {
                images = "Pokemon/Pikachu/images/";
                sounds = "Pokemon/Pikachu/sounds/";
                idlePath = images + "pikachu_idle.png";
                walkPath = images + "pikachu_caminando.gif";
                crouchPath = images + "pikachu_idle.png";
                attackPath = images + "pikachu_atacando.png";
                hurtPath = images + "pikachu_daño.png";
                koPath = null;
                jumpPath = images + "pikachu_idle.png";
                derrotaFrames = 90;
                // Audios de comienzo y gameover dedicados
                inicio = sounds + "Pikachu_Comienzo.wav";
                gameover = sounds + "Pikachu_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Darth_Vader" -> {
                images = "Darth_Vader/images/";
                sounds = "Darth_Vader/sounds/";
                // Idle dedicado disponible
                idlePath = images + "dartVader_idle.png";
                walkPath = images + "dartVader_caminar.gif";
                crouchPath = images + "dartVader_agachar.png";
                attackPath = images + "dartVader_atacar.gif";
                hurtPath = images + "dartVader_daño.png";
                koPath = images + "dartVader_gameOver.gif";
                jumpPath = images + "dartVader_agachar.png";
                inicio = sounds + "Darth_Vader_Comienzo.wav";
                resp = sounds + "Darth_Vader_Respiracion.wav";
                dano = sounds + "Darth_Vader_Daño.wav";
                gameover = sounds + "Darth_Vader_GameOver.wav";
                derrotaFrames = 140;
                audioAtaque = null; audioSalto = null;
            }
            case "Iron_Man" -> {
                images = "Iron_Man/images/";
                sounds = "Iron_Man/sounds/";
                idlePath = images + "iron_Man_idle.png";
                walkPath = images + "ironMan_caminar.gif";
                crouchPath = images + "ironMan_agachar.png";
                attackPath = images + "ironMan_atacar.gif";
                hurtPath = images + "ironMan_daño.png";
                koPath = images + "ironMan_gameOver.gif";
                jumpPath = images + "ironMan_salto.png";
                inicio = sounds + "ironman_comienzo.wav";
                resp = null;
                dano = sounds + "iroman_daño.wav";
                gameover = sounds + "ironman_gameover.wav";
                saltoAtaque = sounds + "ironman_ataque_saltar.wav";
                derrotaFrames = 90;
                audioAtaque = saltoAtaque; audioSalto = saltoAtaque;
            }
            case "Mr_Increible" -> {
                images = "Mr_Increible/images/";
                sounds = "Mr_Increible/sounds/";
                idlePath = images + "mrIncreible_idle.png";
                walkPath = images + "mrIncreible_caminar.gif";
                crouchPath = images + "mrIncreible_agachar.png";
                attackPath = images + "mrIncreible_atacar.gif";
                hurtPath = images + "mrIncreible_daño.png";
                koPath = images + "mrIncreible_gameOver.gif";
                jumpPath = images + "mrIncreible_saltar.png";
                inicio = sounds + "MrIncreible_comienzo.wav";
                resp = null;
                dano = sounds + "MrIncreible_Damage.wav";
                gameover = sounds + "MrIncreible_GameOver.wav";
                saltoAtaque = sounds + "MrIncreible_Punch.wav";
                derrotaFrames = 120;
                audioAtaque = saltoAtaque; audioSalto = null;
            }
            case "Pyke" -> {
                images = "Pyke/images/";
                sounds = "Pyke/sounds/";
                idlePath = images + "pyke_idle.png";
                walkPath = images + "pyke_caminar.gif";
                crouchPath = images + "pyke_agachar.png";
                attackPath = images + "pyke_atacar.gif";
                hurtPath = images + "pyke_daño.png";
                koPath = images + "pyke_gameOver.gif";
                jumpPath = images + "pyke_saltar.png";
                inicio = sounds + "pyke_comienzo.wav";
                resp = null;
                dano = sounds + "pyke_daño.wav";
                gameover = sounds + "pyke_gameover.wav";
                saltoAtaque = sounds + "pyke_salto.wav";
                derrotaFrames = 90;
                audioAtaque = null; audioSalto = saltoAtaque;
            }
            case "Goku" -> {
                images = "Goku/images/";
                sounds = "Goku/sounds/";
                idlePath = images + "goku_idle.png";
                walkPath = images + "goku_caminar.gif";
                crouchPath = images + "goku_agachar.png";
                // Ajuste: el archivo real es goku_atacar.gif
                attackPath = images + "goku_atacar.gif";
                hurtPath = images + "goku_daño.png";
                koPath = images + "goku_gameOver.gif";
                jumpPath = images + "goku_saltar.png";
                inicio = sounds + "goku_comienzo.wav";
                resp = null;
                dano = sounds + "goku_daño_salto.wav"; // daños
                gameover = sounds + "goku_gameover.wav";
                saltoAtaque = null; // reemplazado por separados
                audioAtaque = sounds + "goku_ataque.wav";
                audioSalto = sounds + "goku_daño_salto.wav"; // usar como salto
                derrotaFrames = 90;
            }
            case "Batman" -> {
                images = "Batman/";
                sounds = "Batman/sounds/"; // carpeta de sonidos estandarizada
                // El archivo real es "batma.idle.png" en recursos
                idlePath = images + "batma.idle.png";
                walkPath = images + "batman_caminar.gif";
                crouchPath = images + "batman_agachar.png";
                attackPath = images + "batman_atacar.gif";
                hurtPath = images + "batman_daño.png";
                koPath = images + "batman_gameOver.gif";
                jumpPath = images + "batman_saltar.png";
                // Audios de comienzo y gameover dedicados
                inicio = sounds + "Batman_Comienzo.wav"; 
                resp = null; 
                gameover = sounds + "Batman_GameOver.wav"; 
                saltoAtaque = null;
                audioAtaque = null; audioSalto = null;
                derrotaFrames = 90;
            }
            case "Luke_Skywalker" -> {
                images = "Luke Skywalker/";
                sounds = "Luke Skywalker/sounds/"; // carpeta de sonidos estandarizada
                idlePath = images + "luke_idle.png";
                walkPath = images + "luke_caminar.gif";
                crouchPath = images + "luke_agachar_saltar.png";
                attackPath = images + "luke_atacar.gif";
                hurtPath = images + "luke_daño.png";
                koPath = images + "luke_gameOver.gif";
                jumpPath = images + "luke_agachar_saltar.png";
                // Audios de comienzo y gameover dedicados
                inicio = sounds + "Luke_Comienzo.wav"; 
                resp = null; 
                gameover = sounds + "Luke_GameOver.wav"; 
                saltoAtaque = null;
                audioAtaque = null; audioSalto = null;
                derrotaFrames = 90;
            }
            case "Naruto" -> {
                images = "Naruto/";
                sounds = "Naruto/sounds/"; // carpeta de sonidos disponible
                idlePath = images + "naruto_idle.png";
                walkPath = images + "naruto_caminar.gif";
                crouchPath = images + "naruto_agachar.png";
                attackPath = images + "naruto_atacar.png";
                hurtPath = images + "naruto_daño.png";
                koPath = images + "naruto_gameOver.gif";
                jumpPath = images + "naruto_saltar.png";
                // Audios de comienzo y gameover dedicados
                inicio = sounds + "Naruto_Comienzo.wav";
                resp = null;
                dano = null; 
                gameover = sounds + "Naruto_GameOver.wav"; 
                saltoAtaque = null;
                audioAtaque = null; audioSalto = null;
                derrotaFrames = 90;
            }
        }

        // Asignación estandarizada de audios de ataque y daño
        // Todas las carpetas de sonidos usan los mismos nombres: "Golpe.wav" y "Daño.wav"
        if (sounds != null) {
            try {
                dano = sounds + "Daño.wav";
                audioAtaque = sounds + "Golpe.wav";
                // Desactivar antiguos alias de salto/ataque si existían
                saltoAtaque = null;
                // Si no tenemos audio de salto estándar, mantener null
                audioSalto = null;
            } catch (Exception ignored) {}
        }
        // Debug: log de rutas y carga
        System.out.println("[DEBUG] Cargando personaje: " + personajeId);
        System.out.println("[DEBUG] idle: " + idlePath);
        System.out.println("[DEBUG] walk: " + walkPath);
        System.out.println("[DEBUG] crouch: " + crouchPath);
        System.out.println("[DEBUG] attack: " + attackPath);
        System.out.println("[DEBUG] hurt: " + hurtPath);
        System.out.println("[DEBUG] ko: " + koPath);
        System.out.println("[DEBUG] jump: " + jumpPath);

        Sprite idle = idlePath != null ? new Sprite(idlePath) : null;
        Sprite walk = walkPath != null ? new Sprite(walkPath) : null;
        Sprite crouch = crouchPath != null ? new Sprite(crouchPath) : null;
        Sprite attack = attackPath != null ? new Sprite(attackPath) : null;
        Sprite hurt = hurtPath != null ? new Sprite(hurtPath) : null;
        Sprite ko = koPath != null ? new Sprite(koPath) : null;
        Sprite jump = jumpPath != null ? new Sprite(jumpPath) : null;

        System.out.println("[DEBUG] idle loaded: " + (idle != null && idle.getImagen() != null));
        System.out.println("[DEBUG] walk loaded: " + (walk != null && walk.getImagen() != null));
        System.out.println("[DEBUG] crouch loaded: " + (crouch != null && crouch.getImagen() != null));
        System.out.println("[DEBUG] attack loaded: " + (attack != null && attack.getImagen() != null));
        System.out.println("[DEBUG] hurt loaded: " + (hurt != null && hurt.getImagen() != null));
        System.out.println("[DEBUG] ko loaded: " + (ko != null && ko.getImagen() != null));
        System.out.println("[DEBUG] jump loaded: " + (jump != null && jump.getImagen() != null));

        // Fallback si el sprite de daño no carga: usar idle para evitar rectángulos de color
        if (hurt != null && hurt.getImagen() == null) {
            hurt = idle;
        }
        jugador.setAssets(personajeId, idle, walk, crouch, attack, hurt, ko, jump, inicio, resp, dano, gameover, saltoAtaque, audioSalto, audioAtaque);
        jugador.setDerrotaFrames(derrotaFrames);
        // Ajustar tamaño base para que ocupen ~mitad de altura y algo más de ancho
        jugador.setBaseSize(200, 300);

        // Si es Ash, cargar sprite alterno de ataque 2
        if ("Ash".equals(personajeId)) {
            Sprite attack2 = new Sprite("Pokemon/Ash/images/ash_atacar2.gif");
            jugador.setAttackAlt(attack2);
        } else {
            jugador.setAttackAlt(null);
        }
    }
}
