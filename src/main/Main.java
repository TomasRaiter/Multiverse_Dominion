package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.prefs.Preferences;

// panel personalizado con imagen de fondo
@SuppressWarnings("serial")
class BackgroundPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Image backgroundImage;  // imagen de fondo del panel
    
    // constructor con imagen de fondo
    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
    
    // renderiza la imagen de fondo escalada al tamano del panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    
    // cambia la imagen de fondo y repinta el panel
    public void setBackgroundImage(Image img) {
        this.backgroundImage = img;
        repaint();
    }
}

// clase principal del juego - maneja menus y configuracion
public class Main {
    // variables del modo historia
    private static int historiaNivelActual = 0;        // nivel actual en modo historia
    private static long historiaStartMillis = -1L;     // tiempo de inicio de campana
    private static String historiaNombreCampana = "";  // nombre del jugador en campana
    
    // configuracion de testing
    public static final boolean MODO_TEST_BOSS_SUAVE = false;
    // punto de entrada principal del programa
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::mostrarPreMenu);
    }

    // muestra el menu principal con opciones historia, top 3, versus y salir
    public static void mostrarPreMenu() {
        // colores del tema pixel art
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);

        // crear ventana principal en pantalla completa
        JFrame frame = new JFrame("Selecciona el modo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        BackgroundPanel panel = new BackgroundPanel(null);
        panel.setLayout(new GridBagLayout());
        frame.setContentPane(panel);

        // crear titulo del juego
        JLabel titulo = new JLabel("Multiverse Dominion", SwingConstants.CENTER);
        titulo.setForeground(PIXEL_RED);
        titulo.setFont(new Font("Courier New", Font.BOLD, 42));

        // crear botones del menu principal
        JButton btnHistoria = new JButton("Historia");
        JButton btnTop3 = new JButton("Top 3");
        JButton btnPvP = new JButton("Versus");
        JButton btnSalir = new JButton("Salir");
        for (JButton b : new JButton[]{btnHistoria, btnTop3, btnPvP, btnSalir}) {
            b.setFont(new Font("Monospaced", Font.BOLD, 24));
            b.setBackground(PIXEL_RED_DARK);
            b.setForeground(Color.BLACK);
            b.setFocusPainted(false);
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 5),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 4)
            ));
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titulo, gbc);
        gbc.gridy = 1; panel.add(btnHistoria, gbc);
        gbc.gridy = 2; panel.add(btnTop3, gbc);
        gbc.gridy = 3; panel.add(btnPvP, gbc);
        gbc.gridy = 4; panel.add(btnSalir, gbc);

        ImageIcon fondoIntro = cargarIcono("/resources/Intro/intro.png", 1920, 1080);
        if (fondoIntro != null) {
            panel.setBackgroundImage(fondoIntro.getImage());
        } else {

            ImageIcon fondoBG = cargarIcono("/resources/BackGround/aguasEstancadas.png", 1920, 1080);
            if (fondoBG != null) panel.setBackgroundImage(fondoBG.getImage());
        }

        
        // configurar accion del boton historia
        btnHistoria.addActionListener(ae -> {
            // cargar progreso guardado y solicitar nombre
            cargarProgresoHistoria();
            boolean accepted = mostrarNombreCampanaDialog();
            if (!accepted) {
                return;
            }
            frame.dispose();

            // mostrar cinematica de intro si es el primer nivel
            if (historiaNivelActual == 0) {
                String[] textos = new String[]{
                        "En los confines del multiverso... el Emperador extendió su dominio.",
                        "Su arma más temible no fue la fuerza... sino el control mental.",
                        "Uno a uno, los héroes más poderosos fueron esclavizados.",
                        "Pero las cadenas mentales comenzaron a romperse…",
                        "Ahora, los mundos colisionan… y solo los más fuertes se alzarán."
                };
                String base = "/resources/Intro/";
                String[] fondos = new String[]{
                        base + "cinematica-1.png",
                        base + "cinematica-2.png",
                        base + "cinematica-3.png",
                        base + "cinematica-4.png",
                        base + "cinematica-5.png"
                };
                CinematicManager.mostrarCinematicasConFondosBlocking(textos, fondos);
            }

            mostrarMenuYArrancar(true);
        });
        btnPvP.addActionListener(ae -> { frame.dispose(); mostrarMenuYArrancar(false); });
        btnSalir.addActionListener(ae -> { frame.dispose(); System.exit(0); });

        btnTop3.addActionListener(ae -> mostrarTop3Dialog(frame));

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // muestra dialogo de seleccion de personaje para modo historia
    public static String seleccionarPersonajeHistoria(int nivelIndex) {
        // obtener personajes disponibles segun progreso
        String[] opcionesVisibles = obtenerOpcionesJ1Historia();

        JDialog dialog = new JDialog((Frame) null, "Selecciona tu héroe", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(null);

        Color PIXEL_BLACK = new Color(10, 10, 10);
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);
        Font PIXEL_FONT = new Font("Courier New", Font.BOLD, 18);

        BackgroundPanel panel = new BackgroundPanel(null);
        panel.setLayout(new BorderLayout(10, 10));
        dialog.setContentPane(panel);

        String fondoArchivo = switch (nivelIndex) {
            case 0 -> "metroVille.png";
            case 1 -> "batCave.png";
            case 2 -> "pokemonStaduim.png";
            case 3 -> "aldeaKonoha.png";
            case 4 -> "kameHouse.png";
            case 5 -> "aguasEstancadas.png";
            case 6 -> "graneroDeLuke.png";
            case 7 -> "deathStar.png";
            default -> "deathStar.png";
        };
        ImageIcon bgIcon = cargarIcono("/resources/BackGround/" + fondoArchivo, 1920, 1080);
        if (bgIcon != null) panel.setBackgroundImage(bgIcon.getImage());

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(8,8,8,8);
        JLabel titulo = new JLabel("Nivel " + (nivelIndex + 1) + " — Elige tu héroe", SwingConstants.CENTER);
        titulo.setForeground(PIXEL_RED);
        titulo.setFont(new Font("Courier New", Font.BOLD, 28));
        centro.add(titulo, gbc);

        gbc.gridy = 1;
        JComboBox<String> cbJ1 = new JComboBox<>(opcionesVisibles);
        cbJ1.setBackground(PIXEL_BLACK);
        cbJ1.setForeground(PIXEL_RED);
        cbJ1.setFont(PIXEL_FONT);
        cbJ1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3),
                javax.swing.BorderFactory.createLineBorder(PIXEL_BLACK, 2)
        ));
        cbJ1.setOpaque(true);
        centro.add(cbJ1, gbc);

        gbc.gridy = 2;
        JLabel preview = new JLabel("", SwingConstants.CENTER);
        preview.setPreferredSize(new Dimension(520, 380));
        preview.setOpaque(true);
        preview.setBackground(new Color(30, 30, 30, 200));
        preview.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_BLACK, 3)
        ));
        centro.add(preview, gbc);

        panel.add(centro, BorderLayout.CENTER);

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        JButton btnIniciar = new JButton("Iniciar");
        JButton btnMenu = new JButton("Volver al inicio");
        for (JButton b : new JButton[]{btnIniciar, btnMenu}) {
            b.setFont(new Font("Monospaced", Font.BOLD, 18));
            b.setBackground(PIXEL_RED_DARK);
            b.setForeground(Color.BLACK);
            b.setFocusPainted(false);
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                    javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
            ));
            acciones.add(b);
        }
        panel.add(acciones, BorderLayout.SOUTH);

        actualizarPreviewPersonaje(preview, personajeIdFromNombre((String) cbJ1.getSelectedItem()), false);
        cbJ1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarPreviewPersonaje(preview, personajeIdFromNombre((String) e.getItem()), false);
            }
        });

        final String[] resultado = new String[1];
        btnIniciar.addActionListener(ae -> {
            resultado[0] = personajeIdFromNombre((String) cbJ1.getSelectedItem());
            dialog.dispose();
        });
        btnMenu.addActionListener(ae -> {
            resultado[0] = null;
            dialog.dispose();
        });

        dialog.setVisible(true);
        if (resultado[0] == null) {
            mostrarPreMenu();
        }
        return resultado[0];
    }

    // muestra menu de seleccion de personajes y configuracion de partida
    public static void mostrarMenuYArrancar() { mostrarMenuYArrancar(false); }

    // muestra menu de seleccion de personajes y configuracion de partida
    public static void mostrarMenuYArrancar(boolean historia) {
        // configuracion visual del tema
        Color PIXEL_BLACK = new Color(10, 10, 10);
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);
        Font PIXEL_FONT = new Font("Courier New", Font.BOLD, 14);

        // listas de personajes y fondos disponibles
        String[] personajesNombres = {"Darth Vader", "Iron Man", "Mr. Increíble", "Pyke", "Goku", "Batman", "Luke Skywalker", "Naruto", "Ash"};

        String[] fondosNombres = {
                "Bilgewater",
                "Konohagakure",
                "Batcave",
                "Death Star",
                "Luke House",
                "Kame House",
                "Metro Ville",
                "Pokémon Stadium"
        };

        JFrame frame = new JFrame("Selección previa");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        BackgroundPanel mainPanel = new BackgroundPanel(null);
        mainPanel.setLayout(new BorderLayout(10, 10));
        frame.setContentPane(mainPanel);


        JPanel controles = new JPanel(new GridLayout(1, 3, 8, 8));
        controles.setOpaque(false);

        JPanel colIzq = new JPanel(new GridLayout(2, 1, 8, 8)); colIzq.setOpaque(false);
        JPanel colMid = new JPanel(new GridLayout(2, 1, 8, 8)); colMid.setOpaque(false);
        JPanel colDer = new JPanel(new GridLayout(2, 1, 8, 8)); colDer.setOpaque(false);
        JLabel lblJ1 = new JLabel("Jugador 1"); lblJ1.setForeground(PIXEL_RED); lblJ1.setFont(new Font("Courier New", Font.BOLD, 18));
        JLabel lblJ2 = new JLabel(historia ? "Oponente" : "Jugador 2"); lblJ2.setForeground(PIXEL_RED); lblJ2.setFont(new Font("Courier New", Font.BOLD, 18));
        JLabel lblBg = new JLabel("Selecciona un mapa"); lblBg.setForeground(PIXEL_RED); lblBg.setFont(new Font("Courier New", Font.BOLD, 18));
        // crear comboboxes para seleccion de personajes y fondo
        JComboBox<String> cbJ1 = new JComboBox<>(personajesNombres);
        JComboBox<String> cbJ2 = new JComboBox<>(personajesNombres);
        JComboBox<String> cbFondo = new JComboBox<>(fondosNombres); cbFondo.setSelectedItem("Bilgewater");

        // configurar estilo de todos los comboboxes
        @SuppressWarnings("unchecked")
        JComboBox<String>[] combos = new JComboBox[]{cbJ1, cbJ2, cbFondo};
        for (JComboBox<String> cb : combos) {
            cb.setBackground(PIXEL_BLACK);
            cb.setForeground(PIXEL_RED);
            cb.setFont(PIXEL_FONT);
            cb.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3),
                javax.swing.BorderFactory.createLineBorder(PIXEL_BLACK, 2)
            ));
            cb.setOpaque(true);
            cb.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    c.setFont(PIXEL_FONT);
                    if (isSelected) { setBackground(PIXEL_RED_DARK); setForeground(Color.BLACK); }
                    else { setBackground(PIXEL_BLACK); setForeground(PIXEL_RED); }
                    return c;
                }
            });
        }
        colIzq.add(lblJ1); colIzq.add(cbJ1);
        colMid.add(lblBg); colMid.add(cbFondo);
        colDer.add(lblJ2); colDer.add(cbJ2);
        controles.add(colIzq); controles.add(colMid); controles.add(colDer);


        // crear panel de previsualizacion de personajes
        JPanel previews = new JPanel(new GridLayout(1, 2, 10, 10));
        previews.setOpaque(false);
        JLabel prevJ1 = new JLabel("", SwingConstants.CENTER);
        JLabel prevJ2 = new JLabel("", SwingConstants.CENTER);
        Dimension prevSize = new Dimension(520, 380);
        for (JLabel p : new JLabel[]{prevJ1, prevJ2}) {
            p.setPreferredSize(prevSize);
            p.setOpaque(true);
            p.setBackground(new Color(30, 30, 30, 200));
            p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_BLACK, 3)
            ));
        }
        previews.add(prevJ1);
        previews.add(prevJ2);

        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        overlay.add(previews, gbc);

        // mostrar previews iniciales de personajes
        actualizarPreviewPersonaje(prevJ1, "Darth_Vader", false);
        actualizarPreviewPersonaje(prevJ2, "Goku", true);






















        // crear botones de accion
        JButton btnIniciar = new JButton("Iniciar");
        JButton btnSalir = new JButton("Salir");
        for (JButton b : new JButton[]{btnIniciar, btnSalir}) {
            b.setFont(new Font("Monospaced", Font.BOLD, 18));
            b.setBackground(PIXEL_RED_DARK);
            b.setForeground(Color.BLACK);
            b.setFocusPainted(false);
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
            ));
        }
        JPanel acciones = new JPanel();
        acciones.setOpaque(false);

        acciones.add(btnIniciar);
        acciones.add(btnSalir);

        JLabel lblNivel = new JLabel("", SwingConstants.CENTER);
        lblNivel.setOpaque(false);
        lblNivel.setFont(new Font("Monospaced", Font.BOLD, 22));
        lblNivel.setForeground(new Color(170, 0, 0));
        if (historia) {
            lblNivel.setText("Historia - Nivel " + (historiaNivelActual + 1));
        } else {
            lblNivel.setText("");
        }

        JPanel northWrapper = new JPanel();
        northWrapper.setOpaque(false);
        northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
        JPanel spacer = new JPanel(); spacer.setOpaque(false); spacer.setPreferredSize(new Dimension(1, 6));
        northWrapper.add(lblNivel);
        northWrapper.add(spacer);
        northWrapper.add(controles);

        mainPanel.add(northWrapper, BorderLayout.NORTH);
        mainPanel.add(overlay, BorderLayout.CENTER);
        mainPanel.add(acciones, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            String pj1IdInit = personajeIdFromNombre((String) cbJ1.getSelectedItem());
            String pj2IdInit = personajeIdFromNombre((String) cbJ2.getSelectedItem());
            actualizarPreviewPersonaje(prevJ1, pj1IdInit, false);
            actualizarPreviewPersonaje(prevJ2, pj2IdInit, true);
            frame.revalidate();
            frame.repaint();
        });
        String fondoInit = fondoArchivoFromNombre((String) cbFondo.getSelectedItem());







        ImageIcon fondoInitIcon = cargarIcono("/resources/BackGround/" + fondoInit, 1920, 1080);
        if (fondoInitIcon != null) {
            ((BackgroundPanel)frame.getContentPane()).setBackgroundImage(fondoInitIcon.getImage());
        }

        cbJ1.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) actualizarPreviewPersonaje(prevJ1, personajeIdFromNombre((String) e.getItem()), false); });
        cbJ2.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) actualizarPreviewPersonaje(prevJ2, personajeIdFromNombre((String) e.getItem()), true); });
        cbFondo.addItemListener(e -> { 
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String fondoArchivo = fondoArchivoFromNombre((String) e.getItem());
                ImageIcon fondoMenuIcon = cargarIcono("/resources/BackGround/" + fondoArchivo, 1920, 1080);
                if (fondoMenuIcon != null) {
                    ((BackgroundPanel)frame.getContentPane()).setBackgroundImage(fondoMenuIcon.getImage());
                }
            }
        });

        if (historia) {

            String nombreOponente;
            String nombreFondo;
            String[] opcionesJ1;
            switch (historiaNivelActual) {
                case 0 -> {
                    nombreOponente = "Batman";
                    nombreFondo = "Metro Ville";
                }
                case 1 -> {
                    nombreOponente = "Iron Man";
                    nombreFondo = "Batcave";
                }
                case 2 -> {
                    nombreOponente = "Ash";
                    nombreFondo = "Pokémon Stadium";
                }
                case 3 -> {
                    nombreOponente = "Naruto";
                    nombreFondo = "Konohagakure";
                }
                case 4 -> {
                    nombreOponente = "Goku";
                    nombreFondo = "Kame House";
                }
                case 5 -> {
                    nombreOponente = "Pyke";
                    nombreFondo = "Bilgewater";
                }
                case 6 -> {
                    nombreOponente = "Luke Skywalker";
                    nombreFondo = "Luke House";
                }
                default -> {
                    nombreOponente = "Darth Vader";
                    nombreFondo = "Death Star";
                }
            }

            opcionesJ1 = obtenerOpcionesJ1Historia();

            cbJ2.setSelectedItem(nombreOponente);
            cbFondo.setSelectedItem(nombreFondo);
            cbJ2.setEnabled(false);
            lblJ2.setEnabled(true);
            cbFondo.setEnabled(false);
            lblBg.setEnabled(true);

            cbJ1.setModel(new DefaultComboBoxModel<>(opcionesJ1));
            cbJ1.setSelectedItem(opcionesJ1[0]);
        } else {

            cbJ2.setEnabled(true);
            lblJ2.setEnabled(true);
            cbFondo.setEnabled(true);
            lblBg.setEnabled(true);
        }

        btnSalir.addActionListener(ae -> { frame.dispose(); mostrarPreMenu(); });
        btnIniciar.addActionListener(ae -> {
            // obtener selecciones del usuario
            String pj1 = personajeIdFromNombre((String) cbJ1.getSelectedItem());
            String pj2 = personajeIdFromNombre((String) cbJ2.getSelectedItem());
            String fondo = fondoArchivoFromNombre((String) cbFondo.getSelectedItem());
            frame.dispose();

            if (historia) {
                // configurar modo historia con niveles predefinidos
                System.out.println("[Historia] Iniciar combate desde menu. nivelActual=" + (historiaNivelActual) + ", pj1Sel=" + pj1 + ", oppUI=" + pj2 + ", fondoUI=" + fondo);
                Level[] niveles = new Level[]{


                        Level.simple("Batman", "metroVille.png", BotIA.Nivel.FACIL),


                        Level.simple("Iron_Man", "batCave.png", BotIA.Nivel.NORMAL),

                        Level.simple("Ash", "pokemonStaduim.png", BotIA.Nivel.NORMAL),

                        Level.simple("Naruto", "aldeaKonoha.png", BotIA.Nivel.NORMAL),

                        Level.simple("Goku", "kameHouse.png", BotIA.Nivel.DIFICIL),

                        Level.simple("Pyke", "aguasEstancadas.png", BotIA.Nivel.DIFICIL),

                        Level.simple("Luke_Skywalker", "graneroDeLuke.png", BotIA.Nivel.DIFICIL),

                        Level.finalBoss("Darth_Vader", "deathStar.png", BotIA.Nivel.INSANO)
                };
                // crear instancia del juego y configurar modo historia
                Juego juego = new Juego();

                int idx = Math.max(0, Math.min(niveles.length - 1, getHistoriaNivelActual()));
                Level lvlSel = niveles[idx];
                juego.aplicarSeleccionInicial(pj1, lvlSel.getOponenteId(), lvlSel.getFondoArchivo());
                juego.activarStoryMode(niveles);
                System.out.println("[Historia] Juego creado y configurado. Esperando que J2 sea del nivel y fondo del nivel.");
            } else {
                // modo versus - crear juego con selecciones del usuario
                Juego juego = new Juego();
                juego.aplicarSeleccionInicial(pj1, pj2, fondo);
            }
        });

    }

    // muestra dialogo cinematico estilo undertale (no usado actualmente)
    @SuppressWarnings("unused")
    private static void mostrarCinematicaUndertaleBlocking(String texto) {
        JDialog dialog = new JDialog((Frame) null, "Cinemática", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(1000, 300);
        dialog.setLocationRelativeTo(null);
        Color PIXEL_BLACK = new Color(10, 10, 10);
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(0, 0, 0, 200));
        container.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 6),
                javax.swing.BorderFactory.createLineBorder(PIXEL_BLACK, 4)
        ));

        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 22));
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btn = new JButton("Continuar");
        btn.setFont(new Font("Monospaced", Font.BOLD, 18));
        btn.setBackground(PIXEL_RED_DARK);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
        ));
        btn.addActionListener(e -> dialog.dispose());

        container.add(lbl, BorderLayout.CENTER);
        JPanel south = new JPanel(); south.setOpaque(false); south.add(btn);
        container.add(south, BorderLayout.SOUTH);
        dialog.setContentPane(container);
        dialog.setVisible(true);
    }

    // establece el nivel actual del modo historia (0-7)
    public static void setHistoriaNivelActual(int idx) {
        historiaNivelActual = Math.max(0, Math.min(7, idx));
    }

    // obtiene el nivel actual del modo historia
    public static int getHistoriaNivelActual() {
        return historiaNivelActual;
    }

    // guarda el progreso del modo historia en el registro del sistema
    public static void guardarProgresoHistoria() {
        try {
            Preferences prefs = Preferences.userRoot().node("MultiverseDominion");
            prefs.putInt("historiaNivelActual", historiaNivelActual);
            if (historiaStartMillis > 0) {
                prefs.putLong("historiaStartMillis", historiaStartMillis);
            }
            if (historiaNombreCampana != null) {
                prefs.put("historiaNombreCampana", historiaNombreCampana);
            }
            System.out.println("[Historia] Progreso guardado: nivel=" + historiaNivelActual);
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo guardar progreso: " + ex.getMessage());
        }
    }

    // carga el progreso guardado del modo historia
    public static void cargarProgresoHistoria() {
        try {
            Preferences prefs = Preferences.userRoot().node("MultiverseDominion");
            historiaNivelActual = Math.max(0, Math.min(7, prefs.getInt("historiaNivelActual", 0)));
            historiaStartMillis = prefs.getLong("historiaStartMillis", -1L);
            historiaNombreCampana = prefs.get("historiaNombreCampana", "");
            System.out.println("[Historia] Progreso cargado: nivel=" + historiaNivelActual);
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo cargar progreso: " + ex.getMessage());
            historiaNivelActual = 0;
            historiaStartMillis = -1L;
            historiaNombreCampana = "";
        }
    }

    // inicia el temporizador para medir tiempo total de campana
    public static void iniciarTemporizadorHistoria() {
        try {
            if (historiaStartMillis <= 0) {
                historiaStartMillis = System.currentTimeMillis();
                Preferences.userRoot().node("MultiverseDominion").putLong("historiaStartMillis", historiaStartMillis);
                System.out.println("[Historia] Temporizador iniciado: " + historiaStartMillis);
            } else {
                System.out.println("[Historia] Temporizador ya en curso: " + historiaStartMillis);
            }
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo iniciar temporizador: " + ex.getMessage());
        }
    }

    // reinicia el temporizador de campana
    public static void reiniciarTemporizadorHistoria() {
        try {
            historiaStartMillis = System.currentTimeMillis();
            Preferences.userRoot().node("MultiverseDominion").putLong("historiaStartMillis", historiaStartMillis);
            System.out.println("[Historia] Temporizador reiniciado: " + historiaStartMillis);
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo reiniciar temporizador: " + ex.getMessage());
        }
    }

    // obtiene el tiempo de inicio de la campana
    public static long getHistoriaStartMillis() { return historiaStartMillis; }

    // finaliza la campana y registra el tiempo en el top 3
    public static long finalizarCampaniaYRegistrarTiempo() {
        long elapsed = -1L;
        try {
            Preferences prefs = Preferences.userRoot().node("MultiverseDominion");
            long start = prefs.getLong("historiaStartMillis", historiaStartMillis);
            if (start > 0) {
                elapsed = System.currentTimeMillis() - start;
                registrarTiempoEnTop(elapsed);
            }

            historiaStartMillis = -1L;
            prefs.putLong("historiaStartMillis", -1L);
            System.out.println("[Historia] Campaña finalizada. Tiempo total: " + elapsed + " ms");
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo finalizar/registrar tiempo: " + ex.getMessage());
        }
        return elapsed;
    }

    // registra un tiempo completado en el ranking top 3
    private static void registrarTiempoEnTop(long elapsedMillis) {
        try {
            Preferences prefs = Preferences.userRoot().node("MultiverseDominion");
            long b1 = Math.max(0, prefs.getLong("bestTime1", 0));
            long b2 = Math.max(0, prefs.getLong("bestTime2", 0));
            long b3 = Math.max(0, prefs.getLong("bestTime3", 0));
            String n1 = prefs.get("bestName1", "");
            String n2 = prefs.get("bestName2", "");
            String n3 = prefs.get("bestName3", "");

            java.util.List<long[]> entries = new java.util.ArrayList<>();
            if (b1 > 0) entries.add(new long[]{b1, 1});
            if (b2 > 0) entries.add(new long[]{b2, 2});
            if (b3 > 0) entries.add(new long[]{b3, 3});

            entries.add(new long[]{elapsedMillis, 0});
            entries.sort(java.util.Comparator.comparingLong(a -> a[0]));

            long t1 = entries.size() > 0 ? entries.get(0)[0] : 0;
            int i1 = entries.size() > 0 ? (int) entries.get(0)[1] : -1;
            long t2 = entries.size() > 1 ? entries.get(1)[0] : 0;
            int i2 = entries.size() > 1 ? (int) entries.get(1)[1] : -1;
            long t3 = entries.size() > 2 ? entries.get(2)[0] : 0;
            int i3 = entries.size() > 2 ? (int) entries.get(2)[1] : -1;

            String newName = historiaNombreCampana != null && !historiaNombreCampana.isBlank() ? historiaNombreCampana : "Anónimo";
            String nn1 = (i1 == 1 ? n1 : (i1 == 2 ? n2 : (i1 == 3 ? n3 : newName)));
            String nn2 = (i2 == 1 ? n1 : (i2 == 2 ? n2 : (i2 == 3 ? n3 : newName)));
            String nn3 = (i3 == 1 ? n1 : (i3 == 2 ? n2 : (i3 == 3 ? n3 : newName)));

            prefs.putLong("bestTime1", t1);
            prefs.putLong("bestTime2", t2);
            prefs.putLong("bestTime3", t3);
            prefs.put("bestName1", nn1);
            prefs.put("bestName2", nn2);
            prefs.put("bestName3", nn3);
            System.out.println("[Historia] Top actualizado: " + t1 + "("+nn1+")" + ", " + t2 + "("+nn2+")" + ", " + t3 + "("+nn3+")");
        } catch (Exception ex) {
            System.err.println("[Historia] No se pudo actualizar Top 3: " + ex.getMessage());
        }
    }

    public static String[] obtenerTop3Nombres() {
        try {
            Preferences prefs = Preferences.userRoot().node("MultiverseDominion");
            String n1 = prefs.get("bestName1", "");
            String n2 = prefs.get("bestName2", "");
            String n3 = prefs.get("bestName3", "");
            long b1 = Math.max(0, prefs.getLong("bestTime1", 0));
            long b2 = Math.max(0, prefs.getLong("bestTime2", 0));
            long b3 = Math.max(0, prefs.getLong("bestTime3", 0));
            String f1 = b1 > 0 && n1 != null && !n1.isBlank() ? n1 : "Nadie ocupa este puesto aun.";
            String f2 = b2 > 0 && n2 != null && !n2.isBlank() ? n2 : "Nadie ocupa este puesto aun.";
            String f3 = b3 > 0 && n3 != null && !n3.isBlank() ? n3 : "Nadie ocupa este puesto aun.";
            return new String[]{f1, f2, f3};
        } catch (Exception ex) {
            return new String[]{"Nadie ocupa este puesto aun.", "Nadie ocupa este puesto aun.", "Nadie ocupa este puesto aun."};
        }
    }

    public static String formatMillis(long ms) {
        if (ms <= 0) return "-";
        long totalSeconds = ms / 1000;
        long mins = totalSeconds / 60;
        long secs = totalSeconds % 60;
        long hundredths = (ms % 1000) / 10;
        return String.format("%02d:%02d.%02d", mins, secs, hundredths);
    }

    public static String formatMillisHMS(long ms) {
        if (ms <= 0) return "-";
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long mins = (totalSeconds % 3600) / 60;
        long secs = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    private static void mostrarTop3Dialog(Frame owner) {
        JDialog dialog = new JDialog(owner, "Top 3", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 420);
        dialog.setLocationRelativeTo(owner);
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(new Color(0, 0, 0, 220));
        container.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 6),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 5)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titulo = new JLabel("Libertadores veloces del multiverso:", SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Courier New", Font.BOLD, 28));
        gbc.gridy = 0; container.add(titulo, gbc);

        String[] nombres = obtenerTop3Nombres();
        Preferences prefs = Preferences.userRoot().node("MultiverseDominion");
        long b1 = Math.max(0, prefs.getLong("bestTime1", 0));
        long b2 = Math.max(0, prefs.getLong("bestTime2", 0));
        long b3 = Math.max(0, prefs.getLong("bestTime3", 0));
        String t1 = b1 > 0 ? formatMillisHMS(b1) : "-";
        String t2 = b2 > 0 ? formatMillisHMS(b2) : "-";
        String t3 = b3 > 0 ? formatMillisHMS(b3) : "-";
        JLabel l1 = new JLabel("top 1: " + nombres[0] + " — " + t1, SwingConstants.CENTER);
        JLabel l2 = new JLabel("top 2: " + nombres[1] + " — " + t2, SwingConstants.CENTER);
        JLabel l3 = new JLabel("top 3: " + nombres[2] + " — " + t3, SwingConstants.CENTER);
        for (JLabel l : new JLabel[]{l1, l2, l3}) {
            l.setForeground(PIXEL_RED);
            l.setFont(new Font("Monospaced", Font.BOLD, 22));
        }
        gbc.gridy = 1; container.add(l1, gbc);
        gbc.gridy = 2; container.add(l2, gbc);
        gbc.gridy = 3; container.add(l3, gbc);

        JButton cerrar = new JButton("Cerrar");
        cerrar.setFont(new Font("Monospaced", Font.BOLD, 18));
        cerrar.setBackground(PIXEL_RED_DARK);
        cerrar.setForeground(Color.BLACK);
        cerrar.setFocusPainted(false);
        cerrar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
        ));
        cerrar.addActionListener(e -> dialog.dispose());
        gbc.gridy = 4; container.add(cerrar, gbc);

        dialog.setContentPane(container);
        dialog.setVisible(true);
    }

    public static void setHistoriaNombreCampana(String nombre) {
        historiaNombreCampana = nombre != null ? nombre : "";
        try {
            Preferences.userRoot().node("MultiverseDominion").put("historiaNombreCampana", historiaNombreCampana);
        } catch (Exception ignore) {}
    }

    public static String getHistoriaNombreCampana() {
        return historiaNombreCampana != null ? historiaNombreCampana : "";
    }

    public static void mostrarIntroHistoria() {

        boolean accepted = mostrarNombreCampanaDialog();
        if (!accepted) {
            mostrarPreMenu();
            return;
        }
        String[] textos = new String[]{
                "En los confines del multiverso... el Emperador extendió su dominio.",
                "Su arma más temible no fue la fuerza... sino el control mental.",
                "Uno a uno, los héroes más poderosos fueron esclavizados.",
                "Pero las cadenas mentales comenzaron a romperse…",
                "Ahora, los mundos colisionan… y solo los más fuertes se alzarán."
        };
        String base = "/resources/Intro/";
        String[] fondos = new String[]{
                base + "cinematica-1.png",
                base + "cinematica-2.png",
                base + "cinematica-3.png",
                base + "cinematica-4.png",
                base + "cinematica-5.png"
        };
        CinematicManager.mostrarCinematicasConFondosBlocking(textos, fondos);
        mostrarMenuYArrancar(true);
    }

    private static boolean mostrarNombreCampanaDialog() {
        JDialog dialog = new JDialog((Frame) null, "Nombre de campaña", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(720, 240);
        dialog.setLocationRelativeTo(null);
        Color PIXEL_BLACK = new Color(10, 10, 10);
        Color PIXEL_RED = new Color(170, 0, 0);
        Color PIXEL_RED_DARK = new Color(120, 0, 0);

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(Color.BLACK);
        container.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Color.BLACK, 6),
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 5)
        ));

        JLabel titulo = new JLabel("Nombre de la campaña", SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Courier New", Font.BOLD, 26));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        container.add(titulo, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JTextField campo = new JTextField(30);
        String actual = getHistoriaNombreCampana();
        campo.setText(actual);
        campo.setBackground(new Color(20,20,20));
        campo.setForeground(PIXEL_RED);
        campo.setFont(new Font("Monospaced", Font.BOLD, 20));
        campo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3),
                javax.swing.BorderFactory.createLineBorder(PIXEL_BLACK, 2)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.CENTER;
        center.add(campo, gbc);
        container.add(center, BorderLayout.CENTER);

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        JButton aceptar = new JButton("Aceptar");
        JButton cancelar = new JButton("Cancelar");
        for (JButton b : new JButton[]{aceptar, cancelar}) {
            b.setFont(new Font("Monospaced", Font.BOLD, 18));
            b.setBackground(PIXEL_RED_DARK);
            b.setForeground(Color.BLACK);
            b.setFocusPainted(false);
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.BLACK, 4),
                    javax.swing.BorderFactory.createLineBorder(PIXEL_RED, 3)
            ));
            acciones.add(b);
        }
        final Boolean[] acceptedFlag = new Boolean[]{null};
        aceptar.addActionListener(e -> {
            String nombre = campo.getText() != null ? campo.getText().trim() : "";
            if (nombre.isBlank()) nombre = "Anónimo";
            setHistoriaNombreCampana(nombre);
            acceptedFlag[0] = Boolean.TRUE;
            dialog.dispose();
        });
        cancelar.addActionListener(e -> {
            acceptedFlag[0] = Boolean.FALSE;
            dialog.dispose();
        });
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                acceptedFlag[0] = Boolean.FALSE;
            }
        });
        container.add(acciones, BorderLayout.SOUTH);
        dialog.setContentPane(container);
        dialog.setVisible(true);
        return Boolean.TRUE.equals(acceptedFlag[0]);
    }


    public static String[] obtenerOpcionesJ1Historia() {
        java.util.List<String> opciones = new java.util.ArrayList<>();
        opciones.add("Mr. Increíble");
        String[] oponentesPorNivel = new String[]{
                "Batman",
                "Iron Man",
                "Ash",
                "Naruto",
                "Goku",
                "Pyke",
                "Luke Skywalker",
                "Darth Vader"
        };
        int maxUnlock = Math.max(0, Math.min(historiaNivelActual, oponentesPorNivel.length));
        for (int i = 0; i < maxUnlock; i++) {
            opciones.add(oponentesPorNivel[i]);
        }
        return opciones.toArray(new String[0]);
    }

    private static void actualizarPreviewPersonaje(JLabel label, String personajeId, boolean flip) {
        String idleRel = idlePath(personajeId);
        int containerW = label.getWidth() > 0 ? label.getWidth() : label.getPreferredSize().width;
        boolean esGif = esGifPath(idleRel);

        int alturaNormalizada = 320;

        ImageIcon base = esGif
                ? cargarIconoGif("/resources/" + idleRel)
                : cargarIconoEstaticoEscalado("/resources/" + idleRel, containerW, alturaNormalizada);

        System.out.println("[Menu] Preview " + personajeId + " idle=" + idleRel + " loaded=" + (base != null) + " gif=" + esGif);
        if (base != null) {
            if (esGif) {
                int iw = base.getIconWidth();
                int ih = base.getIconHeight();
                if (iw <= 0 || ih <= 0) {

                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setVerticalAlignment(SwingConstants.CENTER);
                    label.setIcon(base);
                    label.setText("");
                    label.setOpaque(true);
                    label.setBackground(new Color(0, 0, 0, 140));
                    label.revalidate();
                    label.repaint();
                    final int contW = containerW;
                    final int normH = alturaNormalizada;
                    final boolean flipF = flip;
                    final ImageIcon baseIcon = base;
                    javax.swing.Timer t = new javax.swing.Timer(150, e -> {
                        int ciw = baseIcon.getIconWidth();
                        int cih = baseIcon.getIconHeight();
                        if (ciw > 0 && cih > 0) {
                            Icon scaled = scaleIconKeepAnimWithFlip(baseIcon, contW, normH, flipF);
                            label.setIcon(scaled);
                            label.revalidate();
                            label.repaint();
                            ((javax.swing.Timer) e.getSource()).stop();
                            System.out.println("[Menu] Preview actualizado tras carga: " + personajeId + " (" + ciw + "x" + cih + ")");
                        }
                    });
                    t.setRepeats(true);
                    t.start();
                    return;
                } else {
                    Icon iconFinal = scaleIconKeepAnimWithFlip(base, containerW, alturaNormalizada, flip);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setVerticalAlignment(SwingConstants.CENTER);
                    label.setIcon(iconFinal);
                }
            } else {

                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setIcon(base);
            }
            label.setText("");
            label.setOpaque(true);
            label.setBackground(new Color(0, 0, 0, 140));
            label.revalidate();
            label.repaint();
        } else {
            label.setIcon(null);
            label.setText("Sin preview");
            label.setOpaque(true);
            label.setBackground(new Color(0, 0, 0, 140));
            label.revalidate();
            label.repaint();
        }
    }

    // actualiza la previsualizacion de fondo (no usado actualmente)
    @SuppressWarnings("unused")
    private static void actualizarPreviewFondo(JLabel label, String fondoArchivo) {
        ImageIcon icon = cargarIcono("/resources/BackGround/" + fondoArchivo, 200, 200);
        if (icon != null) {
            label.setIcon(icon); label.setText("");
        } else {
            label.setIcon(null); label.setText("Sin fondo");
        }
    }

    // carga un icono desde recursos y lo escala al tamano especificado
    private static ImageIcon cargarIcono(String classpathResource, int w, int h) {
        try {
            ImageIcon icon = null;

            java.net.URL url = Main.class.getResource(classpathResource);
            if (url != null) {
                icon = new ImageIcon(url);
                System.out.println("[Menu] Icono por classpath: " + classpathResource);
            } else {
                // fallback: cargar desde directorio src
                java.io.File fSrc = new java.io.File("src" + classpathResource);
                if (fSrc.exists()) {
                    icon = new ImageIcon(fSrc.toURI().toURL());
                    System.out.println("[Menu] Icono desde src/resources: " + fSrc.getPath());
                } else {
                    System.err.println("[Menu] Icono no encontrado: " + classpathResource);
                }
            }

            boolean esGif = classpathResource.toLowerCase().endsWith(".gif");
            if (icon != null) {
                if (esGif) {
                    return icon;
                } else {
                    if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    } else {
                        return icon;
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            System.err.println("Error cargando icono: " + classpathResource + " - " + ex.getMessage());
            return null;
        }
    }

    // verifica si una ruta corresponde a un archivo gif
    private static boolean esGifPath(String path) {
        return path != null && path.toLowerCase().endsWith(".gif");
    }

    private static ImageIcon cargarIconoGif(String classpathResource) {
        try {
            ImageIcon icon = null;
            java.net.URL url = Main.class.getResource(classpathResource);
            if (url != null) {
                icon = new ImageIcon(url);
                System.out.println("[Menu] GIF por classpath: " + classpathResource);
            } else {
                java.io.File fSrc = new java.io.File("src" + classpathResource);
                if (fSrc.exists()) {
                    icon = new ImageIcon(fSrc.toURI().toURL());
                    System.out.println("[Menu] GIF desde src/resources: " + fSrc.getPath());
                } else {
                    System.err.println("[Menu] GIF no encontrado: " + classpathResource);
                }
            }
            return icon;
        } catch (Exception ex) {
            System.err.println("[Menu] Error cargando GIF: " + classpathResource + " - " + ex.getMessage());
            return null;
        }
    }

    private static ImageIcon cargarIconoEstaticoEscalado(String classpathResource, int w, int h) {
        try {
            ImageIcon icon = null;
            java.net.URL url = Main.class.getResource(classpathResource);
            if (url != null) {
                icon = new ImageIcon(url);
                System.out.println("[Menu] PNG/JPG por classpath: " + classpathResource);
            } else {
                java.io.File fSrc = new java.io.File("src" + classpathResource);
                if (fSrc.exists()) {
                    icon = new ImageIcon(fSrc.toURI().toURL());
                    System.out.println("[Menu] PNG/JPG desde src/resources: " + fSrc.getPath());
                } else {
                    System.err.println("[Menu] PNG/JPG no encontrado: " + classpathResource);
                }
            }
            if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
            return icon;
        } catch (Exception ex) {
            System.err.println("[Menu] Error cargando PNG/JPG: " + classpathResource + " - " + ex.getMessage());
            return null;
        }
    }

    // crea un icono volteado horizontalmente (no usado actualmente)
    @SuppressWarnings("unused")
    private static Icon flipIcon(ImageIcon icon) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                int w = icon.getIconWidth();
                int h = icon.getIconHeight();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x + w, y);
                g2.scale(-1, 1);
                g2.drawImage(icon.getImage(), 0, 0, w, h, c);
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return icon.getIconWidth(); }
            @Override
            public int getIconHeight() { return icon.getIconHeight(); }
        };
    }

    // obtiene la ruta del sprite idle de un personaje
    private static String idlePath(String personajeId) {
        return switch (personajeId) {
            case "Darth_Vader" -> "Darth_Vader/images/dartVader_idle.png";
            case "Iron_Man" -> "Iron_Man/images/iron_Man_idle.png";
            case "Mr_Increible" -> "Mr_Increible/images/mrIncreible_idle.png";
            case "Pyke" -> "Pyke/images/pyke_idle.png";
            case "Goku" -> "Goku/images/goku_idle.png";
            case "Batman" -> "Batman/images/batma.idle.png";
            case "Luke_Skywalker" -> "Luke Skywalker/images/luke_idle.png";
            case "Naruto" -> "Naruto/images/naruto_idle.png";
            case "Ash" -> "Pokemon/Ash/images/ash_idle.png";
            default -> null;
        };
    }

    // convierte nombre de personaje mostrado a id interno
    private static String personajeIdFromNombre(String nombre) {
        return switch (nombre) {
            case "Darth Vader" -> "Darth_Vader";
            case "Iron Man" -> "Iron_Man";
            case "Mr. Increíble" -> "Mr_Increible";
            case "Pyke" -> "Pyke";
            case "Goku" -> "Goku";
            case "Batman" -> "Batman";
            case "Luke Skywalker" -> "Luke_Skywalker";
            case "Naruto" -> "Naruto";
            case "Ash" -> "Ash";
            default -> "Darth_Vader";
        };
    }

    // convierte nombre de fondo mostrado a archivo interno
    private static String fondoArchivoFromNombre(String nombre) {
        return switch (nombre) {
            case "Bilgewater" -> "aguasEstancadas.png";
            case "Konohagakure" -> "aldeaKonoha.png";
            case "Batcave" -> "batCave.png";
            case "Death Star" -> "deathStar.png";
            case "Luke House" -> "graneroDeLuke.png";
            case "Kame House" -> "kameHouse.png";
            case "Metro Ville" -> "metroVille.png";
            case "Pokémon Stadium" -> "pokemonStaduim.png";
            default -> "deathStar.png";
        };
    }
    // escala un icono manteniendo animacion (no usado actualmente)
    @SuppressWarnings("unused")
    private static Icon scaleIconKeepAnim(ImageIcon icon, int containerW, int targetH) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                int iw = icon.getIconWidth();
                int ih = icon.getIconHeight();

                if (iw <= 0 || ih <= 0) {
                    icon.paintIcon(c, g, x + (containerW - icon.getIconWidth()) / 2, y + (targetH - icon.getIconHeight()) / 2);
                    return;
                }

                double escalaAltura = (double) targetH / ih;
                int anchoNormalizado = (int) Math.round(iw * escalaAltura);

                int dx = x + (containerW - anchoNormalizado) / 2;
                int dy = y + (targetH - targetH) / 2;

                System.out.println("[Menu] Pintando preview (no flip) iw=" + iw + " ih=" + ih + " escala=" + escalaAltura);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(dx, dy);
                g2.scale(escalaAltura, escalaAltura);

                icon.paintIcon(c, g2, 0, 0);
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return containerW; }
            @Override
            public int getIconHeight() { return targetH; }
        };
    }

    // escala un icono con opcion de volteo horizontal
    @SuppressWarnings("unused")
    private static Icon scaleIconKeepAnimWithFlip(ImageIcon icon, int containerW, int targetH, boolean flip) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                int iw = icon.getIconWidth();
                int ih = icon.getIconHeight();
                if (iw <= 0 || ih <= 0) {
                    icon.paintIcon(c, g, x + (containerW - icon.getIconWidth()) / 2, y + (targetH - icon.getIconHeight()) / 2);
                    return;
                }
                double escalaAltura = (double) targetH / ih;
                int anchoNormalizado = (int) Math.round(iw * escalaAltura);
                int dx = x + (containerW - anchoNormalizado) / 2;
                int dy = y + (targetH - targetH) / 2;

                Graphics2D g2 = (Graphics2D) g.create();
                if (flip) {
                    // aplicar volteo horizontal
                    g2.translate(dx + anchoNormalizado, dy);
                    g2.scale(-escalaAltura, escalaAltura);
                } else {
                    g2.translate(dx, dy);
                    g2.scale(escalaAltura, escalaAltura);
                }
                System.out.println("[Menu] Pintando preview flip=" + flip + " iw=" + iw + " ih=" + ih + " escala=" + escalaAltura);
                icon.paintIcon(c, g2, 0, 0);
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return containerW; }
            @Override
            public int getIconHeight() { return targetH; }
        };
    }

    // centra un icono en un contenedor (no usado actualmente)
    @SuppressWarnings("unused")
    private static Icon centerIconKeepAnim(ImageIcon icon, int containerW, int containerH) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                int iw = icon.getIconWidth();
                int ih = icon.getIconHeight();
                if (iw <= 0 || ih <= 0) {
                    icon.paintIcon(c, g, x, y);
                    return;
                }
                int dx = x + (containerW - iw) / 2;
                int dy = y + (containerH - ih) / 2;
                icon.paintIcon(c, g, dx, dy);
            }
            @Override
            public int getIconWidth() { return containerW; }
            @Override
            public int getIconHeight() { return containerH; }
        };
    }
}




