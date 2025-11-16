package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// clase principal del motor de juego - maneja toda la logica de combate, fisica y renderizado
@SuppressWarnings({"serial", "this-escape"})
public class Juego extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    // dimensiones base del juego
    private final int baseWidth = 800;
    private final int baseHeight = 600;

    // componentes principales del juego
    private JFrame ventana;                    // ventana principal
    private Timer timer;                       // timer principal del game loop
    private Jugador jugador1, jugador2;       // los dos jugadores
    private Input input;                       // gestor de entrada de teclado
    
    // estado general del juego
    private boolean juegoTerminado = false;    // si el juego ha terminado
    private String mensajeVictoria = "";       // mensaje del ganador
    private boolean enCuentaRegresiva = true;  // si esta en cuenta regresiva inicial
    private int contadorRegresivo = 3;         // contador 3-2-1-fight
    private int contadorFrames = 0;            // frames para la cuenta regresiva
    private JButton botonReiniciar;            // boton para reiniciar
    private JButton botonCambiar;              // boton para cambiar personajes
    
    // sistema de pausa
    private boolean enPausa = false;           // si el juego esta pausado
    private boolean escPrev = false;           // estado previo de esc
    
    // sistema de tiempo y cronometria
    private int segundosRestantes = 90;        // segundos restantes en la ronda
    private long cronometroAcumuladoNanos = 0L; // acumulador de nanosegundos
    private boolean tiempoAgotado = false;     // si se agoto el tiempo
    private int drainRatePorFrame = 1;         // dano por frame cuando se agota el tiempo
    
    // sistema de puntuacion (mejor de 3)
    private int victoriasJ1 = 0;               // victorias del jugador 1
    private int victoriasJ2 = 0;               // victorias del jugador 2
    
    // seleccion de personajes
    
    // sistema de intros de personajes
    private boolean introJ1Terminada = false;  // si termino la intro del j1
    private boolean introJ2Terminada = false;  // si termino la intro del j2
    private int framesEsperaIntroJ1 = 0;       // frames de espera para intro j1
    private int framesDelayAudioJ2 = 0;        // delay para audio de intro j2
    
    // sistema de esperas post-combate
    private boolean enEsperaPostMatch = false;  // espera despues del match completo
    private int framesEsperaPostMatch = 0;      // frames de espera post-match
    private boolean enEsperaPostRonda = false;  // espera despues de cada ronda
    private int framesEsperaPostRonda = 0;      // frames de espera post-ronda
    
    // sistema de animaciones de ko (removido - no se usaba)
    
    // sistema de fondos
    private Image fondoImg = null;
    private String fondoSel = "deathStar.png";

    // sistema de seleccion de pokemon para ash
    private Sprite selAshBase = null;
    private Sprite selAshC = null;
    private Sprite selAshG = null;
    private Sprite selAshP = null;
    private boolean seleccionActivaJ1 = false;
    private int seleccionIndexJ1 = 0;
    private String seleccionPokemonJ1 = null;
    private boolean seleccionActivaJ2 = false;
    private int seleccionIndexJ2 = 0;
    private String seleccionPokemonJ2 = null;
    
    // estado de pokemon derrotados (charizard, greninja, pikachu)
    private boolean muertoC = false;
    private boolean muertoG = false;
    private boolean muertoP = false;
    
    // sistema de mecanicas de ash y pokemon para jugador 1
    private boolean ashSaliendoJ1 = false;
    private int dirSalidaAshJ1 = -1;
    private boolean pokemonActivoJ1 = false;
    private int pokemonVidaJ1 = 25;
    private boolean pokemonVolviendoJ1 = false;
    private int shrinkAlturaJ1 = 300;
    private int esperaAudioPokemonKOJ1 = 0;
    private boolean ashRegresandoJ1 = false;
    
    // sistema de mecanicas de ash y pokemon para jugador 2
    private boolean ashSaliendoJ2 = false;
    private int dirSalidaAshJ2 = 1;
    private boolean pokemonActivoJ2 = false;
    private int pokemonVidaJ2 = 25;
    private boolean pokemonVolviendoJ2 = false;
    private int shrinkAlturaJ2 = 300;
    private int esperaAudioPokemonKOJ2 = 0;
    private boolean ashRegresandoJ2 = false;

    // sistema de modo historia
    private boolean storyMode = false;
    private Level[] nivelesHistoria = null;
    private int indiceNivel = 0;
    private BotIA botIA = null;
    private boolean bossFinalActivo = false;
    private int bossCycleFrames = 0;
    private int bossInvulFrames = 0;

    // constructor: inicializa la ventana y componentes del juego
    public Juego() {
        // configurar ventana principal
        ventana = new JFrame("Multiverse Dominion");
        ventana.setSize(baseWidth, baseHeight);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(true);
        ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // configurar sistema de entrada (teclado y mouse)
        input = new Input();
        ventana.addKeyListener(input);
        try {
            this.addKeyListener(input);
            this.setFocusable(true);
            this.setFocusTraversalKeysEnabled(false);
            this.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override 
                public void mousePressed(java.awt.event.MouseEvent e) {
                    try {
                        requestFocusInWindow();
                        if (ventana != null) ventana.requestFocus();
                    } catch (Exception ignored) {}
                }
            });
            SwingUtilities.invokeLater(() -> {
                try {
                    this.requestFocusInWindow();
                    if (ventana != null) ventana.requestFocus();
                } catch (Exception ignored) {}
            });
        } catch (Exception ignore) {}

    // inicializar timer del juego
    SwingUtilities.invokeLater(() -> {
        try {
            if (timer != null && !timer.isRunning()) {
                timer.start();
            }
            System.out.println("[DEBUG] Post-UI: timer running=" + (timer != null && timer.isRunning()));
        } catch (Exception ignored) {}
    });

    // crear jugadores por defecto
    jugador1 = new Jugador("J1", 100, 500, Color.BLUE);
    jugador2 = new Jugador("J2", 500, 500, Color.RED);

    // aplicar personajes por defecto
    aplicarPersonaje(jugador1, "Darth_Vader");
    aplicarPersonaje(jugador2, "Darth_Vader");

    // configurar fondo y timer principal
    cargarFondo(fondoSel);
    timer = new Timer(16, this);
    timer.start();
    System.out.println("[DEBUG] Timer creado y arrancado");

    // cargar sprites de seleccion de pokemon para ash
    try {
        selAshBase = new Sprite("Pokemon/Ash/images/ash_seleccion.png");
        selAshC = new Sprite("Pokemon/Ash/images/ash_seleccion_c.png");
        selAshG = new Sprite("Pokemon/Ash/images/ash_seleccion_g.png");
        selAshP = new Sprite("Pokemon/Ash/images/ash_seleccion_p.png");
    } catch (Exception ex) {
        System.out.println("[WARN] imagenes de seleccion ash no cargadas: " + ex.getMessage());
    }

    // finalizar configuracion de ventana despues de inicializacion completa
    ventana.add(this);
    ventana.setVisible(true);
}

    // metodo principal de renderizado - dibuja todo el juego
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // calcular escalado para diferentes resoluciones
        float scaleX = getWidth() / (float) baseWidth;
        float scaleY = getHeight() / (float) baseHeight;

        // definir colores y fuentes del tema pixel art
        Color PIXEL_BLACK = new Color(10,10,10);
        Color PIXEL_RED = new Color(170,0,0);
        Color PIXEL_RED_DARK = new Color(120,0,0);
        Font PIXEL_FONT = new Font("Courier New", Font.BOLD, Math.max(10, (int)(14 * scaleY)));

        // dibujar fondo del escenario
        if (fondoImg != null) {
            g.drawImage(fondoImg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(PIXEL_BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // dibujar suelo de combate
        int sueloY = (int) (500 * scaleY);
        int sueloH = (int) (100 * scaleY);
        Color GRAY_MID = new Color(90,90,90);
        g.setColor(GRAY_MID);
        g.fillRect(0, sueloY, getWidth(), sueloH);
        g.setColor(Color.BLACK);
        g.fillRect(0, sueloY - 2, getWidth(), 2);
        g.fillRect(0, sueloY - 5, getWidth(), 2);

        // verificar que los jugadores esten inicializados
        if (jugador1 == null || jugador2 == null) {
            g.setColor(PIXEL_RED);
            int fontInit = Math.max(12, (int) (20 * scaleY));
            g.setFont(new Font("Monospaced", Font.PLAIN, fontInit));
            g.drawString("Inicializando...", 20, 40);
            return;
        }

        // dibujar barras de vida de los jugadores
        int margin = (int) (20 * scaleX);
        int barW = (int) (300 * scaleX);
        int barH = (int) (20 * scaleY);
        int barY = (int) (15 * scaleY);
        int j1BarX = margin;
        int j2BarX = getWidth() - margin - barW;
        g.setFont(PIXEL_FONT);
        g.setColor(Color.BLACK);
        g.fillRect(j1BarX - 6, barY - 6, barW + 12, barH + 12);
        g.fillRect(j2BarX - 6, barY - 6, barW + 12, barH + 12);
        double vidaMaxJ1 = Math.max(1, jugador1.getVidaMax());
        double vidaMaxJ2 = Math.max(1, jugador2.getVidaMax());
        int j1Len = Math.max(0, Math.min(barW, (int)(barW * (jugador1.getVida() / vidaMaxJ1))));
        int j2Len = Math.max(0, Math.min(barW, (int)(barW * (jugador2.getVida() / vidaMaxJ2))));
        g.setColor(PIXEL_RED);
        g.fillRect(j1BarX, barY, j1Len, barH);
        g.fillRect(j2BarX, barY, j2Len, barH);
        // dibujar barras de vida de pokemon activos
        if (pokemonActivoJ1) {
            int sep = Math.max(4, (int)(6 * scaleY));
            int barH2 = Math.max(6, (int)(10 * scaleY));
            int barY2 = barY + barH + sep;
            g.setColor(Color.BLACK);
            g.fillRect(j1BarX - 4, barY2 - 4, barW + 8, barH2 + 8);
            int pLen = Math.max(0, (int)(barW * (pokemonVidaJ1 / 25.0)));
            g.setColor(PIXEL_RED);
            g.fillRect(j1BarX, barY2, pLen, barH2);
        }
        if (pokemonActivoJ2) {
            int sep = Math.max(4, (int)(6 * scaleY));
            int barH2 = Math.max(6, (int)(10 * scaleY));
            int barY2 = barY + barH + sep;
            g.setColor(Color.BLACK);
            g.fillRect(j2BarX - 4, barY2 - 4, barW + 8, barH2 + 8);
            int pLen2 = Math.max(0, (int)(barW * (pokemonVidaJ2 / 25.0)));
            g.setColor(PIXEL_RED);
            g.fillRect(j2BarX, barY2, pLen2, barH2);
        }
        
        // dibujar caja del temporizador
        int boxWTop = Math.max(70, (int)(90 * scaleX));
        int boxHTop = Math.max(30, (int)(40 * scaleY));
        int cxTop = (getWidth() - boxWTop) / 2;
        int cyTop = barY - (int)(5 * scaleY);
        g.setColor(PIXEL_RED);
        g.fillRect(cxTop, cyTop, boxWTop, boxHTop);
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
        
        // dibujar marcador de victorias
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

        // configurar direccion de los sprites segun posicion
        try {
            int centroJ1 = jugador1.getX() + Math.max(10, jugador1.getBounds().width) / 2;
            int centroJ2 = jugador2.getX() + Math.max(10, jugador2.getBounds().width) / 2;
            boolean j1MasDerecha = centroJ1 > centroJ2;
            jugador1.setFlipHorizontal(j1MasDerecha);
            jugador2.setFlipHorizontal(!j1MasDerecha);
        } catch (Exception ignored) {}

        // dibujar jugadores
        jugador1.dibujar(g, this, scaleX, scaleY);
        jugador2.dibujar(g, this, scaleX, scaleY);

        // dibujar interfaz de seleccion de pokemon para jugador 1
        if (seleccionActivaJ1) {
            g.setColor(new Color(0,0,0,150));
            g.fillRect(0, 0, getWidth(), getHeight());
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
            if (!dibujadoConImagen) {
                g.setColor(new Color(20,20,20));
                g.fillRoundRect(ox, oy, overlayW, overlayH, 20, 20);
                g2.setColor(new Color(170,0,0));
                g2.setStroke(new java.awt.BasicStroke(4));
                g2.drawRoundRect(ox, oy, overlayW, overlayH, 20, 20);

                String titulo = "Selecciona tu Pokémon";
                g.setColor(Color.WHITE);
                g.setFont(new Font("Courier New", Font.BOLD, Math.max(18, (int)(26 * scaleY))));
                FontMetrics fmtTit = g.getFontMetrics();
                int tx = ox + (overlayW - fmtTit.stringWidth(titulo)) / 2;
                int ty = oy + Math.max(30, (int)(40 * scaleY));
                g.drawString(titulo, tx, ty);

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
                    char letra = (i==0?'C':(i==1?'G':'P'));
                    g.setColor(sel ? Color.WHITE : new Color(200,200,200));
                    g.setFont(new Font("Courier New", Font.BOLD, Math.max(30, (int)(44 * scaleY))));
                    FontMetrics fmo = g.getFontMetrics();
                    int lx = bx + (bw - fmo.stringWidth(String.valueOf(letra))) / 2;
                    int ly = baseY + fmo.getAscent()/2 - 6;
                    g.drawString(String.valueOf(letra), lx, ly);
                    if (muerto) {
                        g2.setColor(new Color(170,0,0));
                        g2.setStroke(new java.awt.BasicStroke(4));
                        int pad = Math.max(10, (int)(12 * scaleY));
                        g2.drawLine(bx + pad, baseY - bh/2 + pad, bx + bw - pad, baseY + bh/2 - pad);
                        g2.drawLine(bx + pad, baseY + bh/2 - pad, bx + bw - pad, baseY - bh/2 + pad);
                    }
                }
            }
            g.setColor(new Color(170,0,0));
            g.setFont(new Font("Courier New", Font.BOLD, Math.max(12, (int)(18 * scaleY))));
            String help = "F: abrir | A/D: navegar | E: cancelar | R: confirmar";
            FontMetrics fmh = g.getFontMetrics();
            int hx = (getWidth() - fmh.stringWidth(help)) / 2;
            int hy = oy + overlayH + Math.max(20, (int)(24 * scaleY));
            g.drawString(help, hx, Math.min(hy, getHeight() - 10));
        }

        // dibujar interfaz de seleccion de pokemon para jugador 2
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

        // dibujar mensaje de victoria si el juego termino
        if (juegoTerminado) {
            int fontSize2 = Math.max(18, (int) (50 * scaleY));
            g.setFont(new Font("Monospaced", Font.BOLD, fontSize2));
            FontMetrics fm = g.getFontMetrics();
            int textoX = (getWidth() - fm.stringWidth(mensajeVictoria)) / 2;
            int textoY = getHeight() / 2;

            g.setColor(Color.WHITE);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    if (dx != 0 || dy != 0) {
                        g.drawString(mensajeVictoria, textoX + dx, textoY + dy);
                    }
                }
            }
            g.setColor(Color.RED);
            g.drawString(mensajeVictoria, textoX, textoY);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
        // manejar pausa con tecla esc
        if (input.esc && !escPrev) {
            enPausa = !enPausa;
            if (enPausa) {
                mostrarMenuPausa();
            }
        }
        escPrev = input.esc;

        // reinicializar ia si es necesario en modo historia
        if (storyMode && botIA == null) {
            try {
                botIA = new BotIA();
                if (nivelesHistoria != null && indiceNivel >= 0 && indiceNivel < nivelesHistoria.length) {
                    Level lvl = nivelesHistoria[indiceNivel];
                    botIA.setOverrides(
                        lvl.getOverrideRangoAtaque(),
                        lvl.getOverrideVelAcercamiento(),
                        lvl.getOverrideCooldownAtaque(),
                        lvl.getOverrideProbSalto(),
                        lvl.getOverrideProbAgachar(),
                        lvl.getOverrideDistEvadir(),
                        lvl.getOverrideAgresividad()
                    );
                }
                System.out.println("[Historia][IA] BotIA re-inicializada correctamente tras ser nula.");
            } catch (Throwable t) {
                System.err.println("[Historia][IA] No se pudo re-inicializar BotIA: " + t.getMessage());
            }
        }

        // manejar mecanicas del jefe final (invulnerabilidad ciclica)
        if (storyMode && bossFinalActivo && !enCuentaRegresiva && !tiempoAgotado && !juegoTerminado) {
            if (bossInvulFrames > 0) {
                bossInvulFrames--;
            } else {
                bossCycleFrames++;
                if (bossCycleFrames >= 1250) {
                    bossInvulFrames = 312;
                    bossCycleFrames = 0;
                }
            }
        }

        // manejar espera despues de cada ronda
        if (enEsperaPostRonda) {
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

        // manejar espera despues del match completo
        if (enEsperaPostMatch) {
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

                if (storyMode) {
                    boolean ganoJ1 = victoriasJ1 > victoriasJ2;
                    if (ganoJ1) {
                        if (nivelesHistoria != null && indiceNivel + 1 < nivelesHistoria.length) {

                            try {
                              String unlocked = nombreDisplayDesdeId(nivelesHistoria[indiceNivel].getOponenteId());
                                mostrarAlertaHistoriaPixel(
                                        "¡Nuevo personaje desbloqueado: " + unlocked + "!",
                                        "Historia",
                                        new String[]{"OK"},
                                        "OK",
                                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                                );
                            } catch (Exception ignored) {}

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

                                Main.setHistoriaNivelActual(indiceNivel + 1);
                                Main.guardarProgresoHistoria();
                                Main.mostrarMenuYArrancar(true);
                            } else {

                                Main.setHistoriaNivelActual(indiceNivel + 1);
                                Main.guardarProgresoHistoria();
                                Main.mostrarPreMenu();
                            }
                            return;
                        } else {

                            try {
                                String[] textosFin = new String[]{
                                        "HAS LIBERADO EL MULTIVERSO.",
                                        "GRACIAS POR JUGAR."
                                };
                                CinematicManager.mostrarCinematicasBlocking(textosFin);

                                Main.finalizarCampaniaYRegistrarTiempo();
                                int opt = mostrarPantallaFinalNegraHistoria();
                                jugador1.stopAllAudio();
                                jugador2.stopAllAudio();
                                if (timer != null) {
                                    try { timer.stop(); } catch (Exception ignore) {}
                                }
                                ventana.dispose();
                                if (opt == 0) {

                                    Main.setHistoriaNombreCampana("");
                                    Main.setHistoriaNivelActual(0);
                                    Main.guardarProgresoHistoria();
                                    Main.reiniciarTemporizadorHistoria();
                                    Main.mostrarIntroHistoria();
                                } else {

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

        timer.stop();
                botonReiniciar = new JButton("REINICIAR");
                botonReiniciar.setFocusable(false);
                botonReiniciar.setBounds(getWidth()/2 - 75, getHeight()/2 + 50, 150, 50);

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

                botonCambiar = new JButton("CAMBIAR PERSONAJES");
                botonCambiar.setFocusable(false);
                botonCambiar.setBounds(getWidth()/2 - 125, getHeight()/2 + 110, 250, 45);

                botonCambiar.setBackground(new Color(10,10,10));
                botonCambiar.setForeground(new Color(170,0,0));
                botonCambiar.setFont(new Font("Courier New", Font.BOLD, 16));
                botonCambiar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(170,0,0), 4),
                    javax.swing.BorderFactory.createLineBorder(new Color(10,10,10), 3)
                ));
                botonCambiar.addActionListener(ae -> { 

                    jugador1.stopAllAudio();
                    jugador2.stopAllAudio();
                    ventana.dispose();

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
            return;
        }
		
            if (enCuentaRegresiva) {

                if (!introJ1Terminada) {

                    if (framesEsperaIntroJ1 > 0) {
                        framesEsperaIntroJ1--;
                    } else {
                        introJ1Terminada = true;

                        jugador2.onCountdownStart();
                        try {
                            int audioFramesJ2 = (int) Math.ceil(jugador2.getInicioAudioLengthMicros() / 16000.0);
                            framesDelayAudioJ2 = Math.min(Math.max(audioFramesJ2, 90), 240);
                        } catch (Exception ignored) { framesDelayAudioJ2 = 120; }
                        System.out.println("[INTRO] J1 terminada, iniciando J2. Frames espera J2: " + framesDelayAudioJ2);
                    }
                } else if (!introJ2Terminada) {

                    if (framesDelayAudioJ2 > 0) {
                        framesDelayAudioJ2--;
                    } else {
                        introJ2Terminada = true;
                        System.out.println("[INTRO] J2 terminada, iniciando cuenta regresiva");
                    }
                } else {

                    contadorFrames++;
                    if (contadorFrames >= 30) {
                        contadorFrames = 0;
                        contadorRegresivo--;
                    }
                    if (contadorRegresivo < 0) {
                        enCuentaRegresiva = false;
                        jugador1.onFightStart();
                        jugador2.onFightStart();

                        SwingUtilities.invokeLater(() -> {
                            try {
                                this.setFocusable(true);
                                this.requestFocusInWindow();
                                if (ventana != null) ventana.requestFocus();
                            } catch (Exception ignored) {}
                        });
                        System.out.println("[FIGHT] Cuenta regresiva terminada. Combate iniciado.");
                    }
                }
                repaint();
                return;
            }

        if (!juegoTerminado) {

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

        if (tiempoAgotado || enCuentaRegresiva) {
            jugador1.congelar();
            jugador2.congelar();
            if (tiempoAgotado) {
                System.out.println("[INPUT] Congelado por tiempo agotado.");
            } else if (enCuentaRegresiva) {
                System.out.println("[INPUT] Congelado por cuenta regresiva en curso. contador=" + contadorRegresivo);
            }
        } else {

            // manejar apertura de seleccion de pokemon para jugador 1
            if (input.fPulse && "Ash".equals(jugador1.getPersonajeId()) && !seleccionActivaJ1 && !ashSaliendoJ1 && !pokemonActivoJ1) {
                seleccionActivaJ1 = true;
                seleccionIndexJ1 = 0;
                seleccionPokemonJ1 = "Charizard";
                input.fPulse = false;
            }

            // manejar apertura de seleccion de pokemon para jugador 2
            if (input.lPulse && "Ash".equals(jugador2.getPersonajeId()) && !seleccionActivaJ2 && !ashSaliendoJ2 && !pokemonActivoJ2) {
                seleccionActivaJ2 = true;
                seleccionIndexJ2 = 0;
                seleccionPokemonJ2 = "Charizard";
                input.lPulse = false;
            }

        // manejar navegacion en seleccion de pokemon para jugador 1
        if (seleccionActivaJ1) {
            jugador1.congelar();
            jugador2.congelar();

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

            // cancelar seleccion
            if (input.ePulse) {
                seleccionActivaJ1 = false;
                seleccionPokemonJ1 = null;
                input.ePulse = false;
            }
            seleccionPokemonJ1 = seleccionIndexJ1 == 0 ? "Charizard" : (seleccionIndexJ1 == 1 ? "Greninja" : "Pikachu");
            // confirmar seleccion
            if (input.ataque1Pulse) {
                input.ataque1Pulse = false;

                boolean selMuerto = (seleccionIndexJ1==0 && muertoC) || (seleccionIndexJ1==1 && muertoG) || (seleccionIndexJ1==2 && muertoP);
                if (!selMuerto) {
                    seleccionActivaJ1 = false;

                    jugador1.onGameOver(false);

                    jugador1.setIgnorarLimitesHorizontales(true);
                    ashSaliendoJ1 = true; dirSalidaAshJ1 = -1;
                }
            }
        }
        // manejar navegacion en seleccion de pokemon para jugador 2
        if (seleccionActivaJ2) {
            jugador1.congelar();
            jugador2.congelar();

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
            // confirmar seleccion
            if (input.ataque2Pulse) {
                input.ataque2Pulse = false;
                boolean selMuerto2 = (seleccionIndexJ2==0 && muertoC) || (seleccionIndexJ2==1 && muertoG) || (seleccionIndexJ2==2 && muertoP);
                if (!selMuerto2) {
                    seleccionActivaJ2 = false;

                    jugador2.onGameOver(false);
                    jugador2.setIgnorarLimitesHorizontales(true);
                    ashSaliendoJ2 = true; dirSalidaAshJ2 = 1;
                }
            }
        }

        // animacion de salida de ash para jugador 1
        if (ashSaliendoJ1) {
            jugador1.setX(jugador1.getX() + dirSalidaAshJ1 * 8);
            if (jugador1.getX() < -200) {
                ashSaliendoJ1 = false;
                jugador1.setIgnorarLimitesHorizontales(false);
                aplicarPersonaje(jugador1, seleccionPokemonJ1);
                jugador1.setX(100);
                pokemonActivoJ1 = true;
                pokemonVidaJ1 = 25;
                jugador1.resetParaNuevaRonda();

                jugador1.onCountdownStart();
            }
        }
        // animacion de salida de ash para jugador 2
        if (ashSaliendoJ2) {
            jugador2.setX(jugador2.getX() + dirSalidaAshJ2 * 8);
            if (jugador2.getX() > baseWidth + 200 - jugador2.getBounds().width) {
                ashSaliendoJ2 = false;
                jugador2.setIgnorarLimitesHorizontales(false);
                aplicarPersonaje(jugador2, seleccionPokemonJ2);
                jugador2.setX(500);
                pokemonActivoJ2 = true;
                pokemonVidaJ2 = 25;
                jugador2.resetParaNuevaRonda();

                jugador2.onCountdownStart();
            }
        }

            // manejar controles del jugador 1
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

            // manejar controles del jugador 2 (o ia en modo historia)
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

        // actualizar estado de los jugadores
        jugador1.actualizar();
        jugador2.actualizar();

        if (!juegoTerminado) {

            // aplicar dano por tiempo agotado
            if (tiempoAgotado) {
                jugador1.recibirDano(drainRatePorFrame);
                jugador2.recibirDano(drainRatePorFrame);
            }

        // manejar colisiones de ataques
        if (!tiempoAgotado && !enCuentaRegresiva) {
            if (jugador1.atacando && !jugador1.haGolpeado() && jugador1.getBounds().intersects(jugador2.getBounds())) {
                boolean invulBoss = storyMode && bossFinalActivo && bossInvulFrames > 0;
                boolean esquivandoAgachado = jugador2.isAgachado();
                if (!invulBoss && !esquivandoAgachado) {
                    if (pokemonActivoJ2) {
                        pokemonVidaJ2 = Math.max(0, pokemonVidaJ2 - 5);
                    } else {
                        jugador2.recibirDano(9);
                    }
                }
                jugador1.marcarGolpeado();
            }
            if (jugador2.atacando && !jugador2.haGolpeado() && jugador2.getBounds().intersects(jugador1.getBounds())) {
                int dano = 9;
                if (storyMode && bossFinalActivo && jugador2.getVida() <= (jugador2.getVidaMax() * 0.10)) {

                    dano = jugador1.getVida();
                }
                boolean esquivandoAgachado = jugador1.isAgachado();
                if (!esquivandoAgachado) {
                    if (pokemonActivoJ1) {
                        int danoP = (dano == jugador1.getVida()) ? pokemonVidaJ1 : 5;
                        pokemonVidaJ1 = Math.max(0, pokemonVidaJ1 - danoP);
                    } else {
                        jugador1.recibirDano(dano);
                    }
                }
                jugador2.marcarGolpeado();
            }
        }
        }

        // manejar derrota de pokemon del jugador 1
        if (pokemonActivoJ1 && pokemonVidaJ1 <= 0 && !pokemonVolviendoJ1) {
            jugador1.onGameOver(true);

            if ("Charizard".equals(seleccionPokemonJ1)) muertoC = true;
            else if ("Greninja".equals(seleccionPokemonJ1)) muertoG = true;
            else if ("Pikachu".equals(seleccionPokemonJ1)) muertoP = true;
            pokemonVolviendoJ1 = true;
            dirSalidaAshJ1 = -1;

            jugador1.setIgnorarLimitesHorizontales(true);
            shrinkAlturaJ1 = 300;
            jugador1.setAlturaVisualOverride(shrinkAlturaJ1);

            try {
                int audioFramesKO = (int) Math.ceil(jugador1.getGameOverAudioLengthMicros() / 16000.0);
                esperaAudioPokemonKOJ1 = Math.min(Math.max(audioFramesKO, 90), 240);
            } catch (Exception ignored) { esperaAudioPokemonKOJ1 = 120; }
        }
        // animacion de retorno de pokemon derrotado para jugador 1
        if (pokemonVolviendoJ1) {
            jugador1.setX(jugador1.getX() + dirSalidaAshJ1 * 8);
            if (esperaAudioPokemonKOJ1 > 0) esperaAudioPokemonKOJ1--;
            if (jugador1.getX() < -200 && esperaAudioPokemonKOJ1 <= 0) {
                pokemonVolviendoJ1 = false;
                pokemonActivoJ1 = false;
                aplicarPersonaje(jugador1, "Ash");
                jugador1.setX(-200);

                jugador1.forceIdle();
                jugador1.onFightStart();
                jugador1.clearAlturaVisualOverride();
                jugador1.setIgnorarLimitesHorizontales(false);
                shrinkAlturaJ1 = 300;

                ashRegresandoJ1 = true;
            }
        }

        // animacion de regreso de ash para jugador 1
        if (ashRegresandoJ1) {
            jugador1.setX(jugador1.getX() + 8);
            if (jugador1.getX() >= 100) {
                jugador1.setX(100);
                ashRegresandoJ1 = false;
            }
        }

        // manejar derrota de pokemon del jugador 2
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
        // animacion de retorno de pokemon derrotado para jugador 2
        if (pokemonVolviendoJ2) {
            jugador2.setX(jugador2.getX() + dirSalidaAshJ2 * 8);
            if (esperaAudioPokemonKOJ2 > 0) esperaAudioPokemonKOJ2--;
            if (jugador2.getX() > baseWidth + 200 - jugador2.getBounds().width && esperaAudioPokemonKOJ2 <= 0) {
                pokemonVolviendoJ2 = false;
                pokemonActivoJ2 = false;
                aplicarPersonaje(jugador2, "Ash");
                jugador2.setX(baseWidth + 200);
                jugador2.forceIdle();
                jugador2.onFightStart();
                jugador2.clearAlturaVisualOverride();
                jugador2.setIgnorarLimitesHorizontales(false);
                shrinkAlturaJ2 = 300;

                ashRegresandoJ2 = true;
            }
        }

        // animacion de regreso de ash para jugador 2
        if (ashRegresandoJ2) {
            jugador2.setX(jugador2.getX() - 8);
            if (jugador2.getX() <= 500) {
                jugador2.setX(500);
                ashRegresandoJ2 = false;
            }
        }

        // verificar condiciones de victoria
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

                if (victoriasJ1 > victoriasJ2) {
                    mensajeVictoria = jugador1.getNombre() + " CAMPEON!";
                } else if (victoriasJ2 > victoriasJ1) {
                    mensajeVictoria = jugador2.getNombre() + " CAMPEON!";
                } else {
                    mensajeVictoria = "EMPATE FINAL!";
                }

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

                framesEsperaPostMatch = Math.min(Math.max(audioFrames, 180), 300);
                enEsperaPostMatch = true;
            } else {

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

    // mostrar dialogo de alerta con estilo pixel para modo historia
    private int mostrarAlertaHistoriaPixel(String mensaje, String titulo, String[] opciones, String defaultOption, int messageType) {
        JDialog dialog = new JDialog(ventana, titulo, true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(600, 260);
        dialog.setLocationRelativeTo(ventana);

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

    // mostrar pantalla final negra para modo historia
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
    

    // reiniciar el juego completo
    private void reiniciarJuego() {

        jugador1.setVida(jugador1.getVidaMax());
        jugador2.setVida(jugador2.getVidaMax());
        jugador1.setX(100);
        jugador2.setX(500);

        jugador1.stopAllAudio();
        jugador2.stopAllAudio();

        juegoTerminado = false;
        enCuentaRegresiva = true;
        contadorRegresivo = 3;
        contadorFrames = 0;
        segundosRestantes = obtenerSegundosIniciales();
        cronometroAcumuladoNanos = 0L;
        tiempoAgotado = false;
        mensajeVictoria = "";
        enPausa = false;
        victoriasJ1 = 0;
        victoriasJ2 = 0;

        introJ1Terminada = false;
        introJ2Terminada = false;
        jugador1.onCountdownStart();
        try {
            int framesIntroJ1 = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
            framesEsperaIntroJ1 = Math.min(Math.max(framesIntroJ1, 90), 240);
        } catch (Exception ignored) { framesEsperaIntroJ1 = 120; }
        System.out.println("[DEBUG] framesEsperaIntroJ1 reinicio=" + framesEsperaIntroJ1);

        if (botonReiniciar != null) {
            botonReiniciar.setVisible(false);
            this.remove(botonReiniciar);
        }
        if (botonCambiar != null) {
            botonCambiar.setVisible(false);
            this.remove(botonCambiar);
        }

        timer.start();

        this.setFocusable(true);
        this.requestFocusInWindow();
        ventana.requestFocus();

        repaint();
    }

    // preparar una nueva ronda despues de una victoria/derrota
    private void prepararNuevaRonda() {
        jugador1.setVida(jugador1.getVidaMax());
        jugador2.setVida(jugador2.getVidaMax());
        jugador1.setX(100);
        jugador2.setX(500);

        jugador1.resetParaNuevaRonda();
        jugador2.resetParaNuevaRonda();

        tiempoAgotado = false;
        segundosRestantes = obtenerSegundosIniciales();
        cronometroAcumuladoNanos = 0L;
        enCuentaRegresiva = true;
        contadorRegresivo = 3;
        contadorFrames = 0;
        mensajeVictoria = "";

        introJ1Terminada = false;
        introJ2Terminada = false;
        jugador1.onCountdownStart();
        try {
            int framesIntroJ1b = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
            framesEsperaIntroJ1 = Math.min(Math.max(framesIntroJ1b, 90), 240);
        } catch (Exception ignored) { framesEsperaIntroJ1 = 120; }
        System.out.println("[DEBUG] framesEsperaIntroJ1 ronda=" + framesEsperaIntroJ1);

        if (!timer.isRunning()) timer.start();
        repaint();
    }

    // convertir id de personaje a nombre para mostrar
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

    // activar el modo historia con niveles dados
    public void activarStoryMode(Level[] niveles) {
        this.storyMode = true;
        this.nivelesHistoria = niveles;

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
                this.botIA = null;
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

            this.botIA = null;

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

    // aplicar configuracion del nivel actual del modo historia
    private void aplicarNivelActual() {
        if (nivelesHistoria == null || indiceNivel < 0 || indiceNivel >= nivelesHistoria.length) return;
        Level lvl = nivelesHistoria[indiceNivel];

        String fondo = lvl.getFondoArchivo();
        if (fondo != null && !fondo.isEmpty()) {
            try {
                this.fondoSel = fondo;
                cargarFondo(fondo);
            } catch (Exception ex) {
                System.err.println("[Historia] No se pudo cargar fondo: " + ex.getMessage());
            }
        }

        try {
            aplicarPersonaje(jugador2, lvl.getOponenteId());
            System.out.println("[Historia] Nivel aplicado: fondo=" + lvl.getFondoArchivo() + ", J2=" + jugador2.getNombre());
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo aplicar oponente: " + ex.getMessage());
        }

        bossFinalActivo = lvl.isEsFinalBoss();
        if (!bossFinalActivo) {
            try { jugador1.setVidaMax(100); } catch (Exception ignored) {}
        }

        int vidaJ2 = bossFinalActivo ? 500 : 100;
        String personajeJ2 = jugador2.getPersonajeId();
        if ("Charizard".equals(personajeJ2) || "Greninja".equals(personajeJ2) || "Pikachu".equals(personajeJ2)) {
            vidaJ2 = 25;
        } else if ("Ash".equals(personajeJ2)) {

            vidaJ2 = bossFinalActivo ? 500 : 100;
        }
        try { jugador2.setVidaMax(vidaJ2); } catch (Exception ignored) {}

        String personajeJ1 = jugador1.getPersonajeId();
        if ("Charizard".equals(personajeJ1) || "Greninja".equals(personajeJ1) || "Pikachu".equals(personajeJ1)) {
            try { jugador1.setVidaMax(25); } catch (Exception ignored) {}
        } else if ("Ash".equals(personajeJ1)) {

            try { jugador1.setVidaMax(100); } catch (Exception ignored) {}
        }
        
        jugador1.setVida(jugador1.getVidaMax());
        jugador2.setVida(jugador2.getVidaMax());
        bossCycleFrames = 0;
        bossInvulFrames = 0;

        if (botIA != null) {
            try {
                botIA.setNivel(lvl.getDificultadIA());
                botIA.setOverrides(lvl.getOverrideRangoAtaque(), lvl.getOverrideVelAcercamiento(), lvl.getOverrideCooldownAtaque(),
                        lvl.getOverrideProbSalto(), lvl.getOverrideProbAgachar(), lvl.getOverrideDistEvadir(), lvl.getOverrideAgresividad());
            } catch (Throwable t) {
                System.err.println("[Historia][IA] Falló configuración de IA: " + t.getMessage());

                botIA = null;
            }
        }

        victoriasJ1 = 0;
        victoriasJ2 = 0;
        enCuentaRegresiva = true;
        contadorRegresivo = 3;
        contadorFrames = 0;
        segundosRestantes = obtenerSegundosIniciales();
        cronometroAcumuladoNanos = 0L;
        tiempoAgotado = false;
        mensajeVictoria = "";

        introJ1Terminada = false;
        introJ2Terminada = false;
        try {
            jugador1.onCountdownStart();
            int framesIntroJ1 = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
            framesEsperaIntroJ1 = Math.min(Math.max(framesIntroJ1, 90), 240);
        } catch (Exception ignored) { framesEsperaIntroJ1 = 120; }
        if (!timer.isRunning()) timer.start();
        repaint();
    }

    
    // obtener segundos iniciales segun personaje
    private int obtenerSegundosIniciales() {
        try {
            String oppId = (jugador2 != null) ? jugador2.getPersonajeId() : null;
            if ("Darth_Vader".equals(oppId)) return 150;
        } catch (Exception ignored) {}
        return 90;
    }

    // mostrar menu de pausa
    private void mostrarMenuPausa() {

        Color PIXEL_BLACK = new Color(10,10,10);
        Color PIXEL_RED = new Color(170,0,0);
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

    // mostrar menu de resolucion
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

        this.requestFocusInWindow();
        ventana.requestFocus();
    }

    // mostrar controles del juego
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

    // cambiar resolucion de la ventana
    private void cambiarResolucion(int w, int h) {

        ventana.setExtendedState(JFrame.NORMAL);
        ventana.setSize(w, h);
        this.setPreferredSize(new Dimension(w, h));
        ventana.validate();
        revalidate();
        repaint();

        this.requestFocusInWindow();
        ventana.requestFocus();
    }

    // cargar imagen de fondo
    private void cargarFondo(String archivo) {
        try {

            java.net.URL url = getClass().getResource("/resources/BackGround/" + archivo);
            if (url != null) {
                fondoImg = new ImageIcon(url).getImage();
                return;
            }

            java.io.File fBin = new java.io.File("bin/resources/BackGround/" + archivo);
            if (fBin.exists()) {
                fondoImg = new ImageIcon(fBin.toURI().toURL()).getImage();
                return;
            }

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

    // aplicar seleccion inicial de personajes y fondo
    public void aplicarSeleccionInicial(String pj1, String pj2, String fondo) {
        if (pj1 != null) aplicarPersonaje(jugador1, pj1);
        if (pj2 != null) aplicarPersonaje(jugador2, pj2);
        if (fondo != null) { fondoSel = fondo; cargarFondo(fondoSel); }

        jugador1.resetParaNuevaRonda();
        jugador2.resetParaNuevaRonda();

        introJ1Terminada = false;
        introJ2Terminada = false;
        if (jugador1 != null) {
            jugador1.onCountdownStart();
            try {
                int framesIntroJ1 = (int) Math.ceil(jugador1.getInicioAudioLengthMicros() / 16000.0);
                framesEsperaIntroJ1 = Math.min(Math.max(framesIntroJ1, 90), 240);
            } catch (Exception ignored) { framesEsperaIntroJ1 = 120; }
            System.out.println("[DEBUG] framesEsperaIntroJ1 seleccionInicial=" + framesEsperaIntroJ1);
        }
    }

    @SuppressWarnings("unused")
    private void mostrarSeleccionPersonajes() {
        String[] personajes = {"Darth_Vader","Iron_Man","Mr_Increible","Pyke","Goku","Batman","Luke_Skywalker","Naruto"};
        String personajeSelJ1 = (String) JOptionPane.showInputDialog(ventana, "Jugador 1: elige personaje", "Selección J1", JOptionPane.PLAIN_MESSAGE, null, personajes, personajes[0]);
        String personajeSelJ2 = (String) JOptionPane.showInputDialog(ventana, "Jugador 2: elige personaje", "Selección J2", JOptionPane.PLAIN_MESSAGE, null, personajes, personajes[1]);
        // Este método no se usa actualmente pero se mantiene para compatibilidad futura
    }

    // aplicar configuracion de personaje a un jugador
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
                attackPath = images + "ash_atacar1.gif";
                hurtPath = images + "ash_daño.png";
                koPath = images + "ash_gameOver.gif";
                jumpPath = images + "ash_agachar_saltar.png";

                derrotaFrames = 90;

                inicio = sounds + "Ash_Comienzo.wav";
                gameover = sounds + "Ash_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Charizard" -> {
                images = "Pokemon/Charizard/images/";
                sounds = "Pokemon/Charizard/sounds/";
                idlePath = images + "charizard_idle_caminar.gif";
                walkPath = images + "charizard_idle_caminar.gif";
                crouchPath = images + "charizard.png";
                attackPath = images + "charizard_atacar.gif";
                hurtPath = images + "charizard_daño.png";
                koPath = null;
                jumpPath = images + "charizard.png";
                derrotaFrames = 90;

                inicio = sounds + "Charizard_Comienzo.wav";
                gameover = sounds + "Charizard_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Greninja" -> {
                images = "Pokemon/Greninja/images/";
                sounds = "Pokemon/Greninja/sounds/";
                idlePath = images + "greninja_idle.png";
                walkPath = images + "greninja_caminar.gif";
                crouchPath = images + "greninja_idle.png";
                attackPath = images + "greninja_atacar.gif";
                hurtPath = images + "greninja_daño.png";
                koPath = null;
                jumpPath = images + "greninja_idle.png";
                derrotaFrames = 90;

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

                inicio = sounds + "Pikachu_Comienzo.wav";
                gameover = sounds + "Pikachu_GameOver.wav";
                audioAtaque = null; audioSalto = null;
            }
            case "Darth_Vader" -> {
                images = "Darth_Vader/images/";
                sounds = "Darth_Vader/sounds/";

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
                derrotaFrames = 473;
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
                attackPath = images + "goku_atacar.gif";
                hurtPath = images + "goku_daño.png";
                koPath = images + "goku_gameOver.gif";
                jumpPath = images + "goku_saltar.png";
                inicio = sounds + "goku_comienzo.wav";
                resp = null;
                dano = sounds + "goku_daño_salto.wav";
                gameover = sounds + "goku_gameover.wav";
                saltoAtaque = null;
                audioAtaque = sounds + "goku_ataque.wav";
                audioSalto = sounds + "goku_daño_salto.wav";
                derrotaFrames = 90;
            }
            case "Batman" -> {
                images = "Batman/images/";
                sounds = "Batman/sounds/";
                idlePath = images + "batma.idle.png";
                walkPath = images + "batman_caminar.gif";
                crouchPath = images + "batman_agachar.png";
                attackPath = images + "batman_atacar.gif";
                hurtPath = images + "batman_daño.png";
                koPath = images + "batman_gameOver.gif";
                jumpPath = images + "batman_saltar.png";
                inicio = sounds + "Batman_Comienzo.wav"; 
                resp = null; 
                gameover = sounds + "Batman_GameOver.wav"; 
                saltoAtaque = null;
                audioAtaque = null; audioSalto = null;
                derrotaFrames = 90;
            }
            case "Luke_Skywalker" -> {
                images = "Luke Skywalker/images/";
                sounds = "Luke Skywalker/sounds/";
                idlePath = images + "luke_idle.png";
                walkPath = images + "luke_caminar.gif";
                crouchPath = images + "luke_agachar_saltar.png";
                attackPath = images + "luke_atacar.gif";
                hurtPath = images + "luke_daño.png";
                koPath = images + "luke_gameOver.gif";
                jumpPath = images + "luke_agachar_saltar.png";

                inicio = sounds + "Luke_Comienzo.wav"; 
                resp = null; 
                gameover = sounds + "Luke_GameOver.wav"; 
                saltoAtaque = null;
                audioAtaque = null; audioSalto = null;
                derrotaFrames = 90;
            }
            case "Naruto" -> {
                images = "Naruto/images/";
                sounds = "Naruto/sounds/";
                idlePath = images + "naruto_idle.png";
                walkPath = images + "naruto_caminar.gif";
                crouchPath = images + "naruto_agachar.png";
                attackPath = images + "naruto_atacar.png";
                hurtPath = images + "naruto_daño.png";
                koPath = images + "naruto_gameOver.gif";
                jumpPath = images + "naruto_saltar.png";

                inicio = sounds + "Naruto_Comienzo.wav";
                resp = null;
                dano = null; 
                gameover = sounds + "Naruto_GameOver.wav"; 
                saltoAtaque = null;
                audioAtaque = null; audioSalto = null;
                derrotaFrames = 90;
            }
        }


        if (sounds != null) {
            try {
                dano = sounds + "Daño.wav";
                audioAtaque = sounds + "Golpe.wav";

                saltoAtaque = null;

                audioSalto = null;
            } catch (Exception ignored) {}
        }

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

        if (hurt != null && hurt.getImagen() == null) {
            hurt = idle;
        }
        jugador.setAssets(personajeId, idle, walk, crouch, attack, hurt, ko, jump, inicio, resp, dano, gameover, saltoAtaque, audioSalto, audioAtaque);
        jugador.setDerrotaFrames(derrotaFrames);

        if ("Pikachu".equals(personajeId)) {
            jugador.setBaseSize(100, 150);
        } else {
            jugador.setBaseSize(200, 300);
        }


        if ("Ash".equals(personajeId)) {
            Sprite attack2 = new Sprite("Pokemon/Ash/images/ash_atacar2.gif");
            jugador.setAttackAlt(attack2);
        } else {
            jugador.setAttackAlt(null);
        }
    }
    
}
