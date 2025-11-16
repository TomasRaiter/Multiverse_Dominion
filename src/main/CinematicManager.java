package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Gestor de cinemáticas para mostrar secuencias de texto con fondos opcionales
@SuppressWarnings({"serial", "this-escape"})
public class CinematicManager extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // Datos de la cinemática
    private final String[] textos;      // Textos a mostrar secuencialmente
    private final Image[] fondos;       // Fondos opcionales para cada texto
    private int index = 0;              // Índice del texto actual
    private boolean terminado = false;  // Estado de finalización

    // Constructor simple solo con textos
    public CinematicManager(String[] textos) { this(textos, null); }

    // Constructor completo con textos y fondos
    public CinematicManager(String[] textos, Image[] fondos) {
        this.textos = textos != null ? textos : new String[0];
        this.fondos = fondos;
        initializeComponent();

        // Configurar eventos para avanzar la cinemática
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { avanzar(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { avanzar(); }
        });
    }

    // Inicializa los componentes del panel
    private void initializeComponent() {
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    // Avanza al siguiente texto o cierra la cinemática si terminó
    private void avanzar() {
        if (terminado) return;
        index++;
        if (index >= textos.length) {
            terminado = true;
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        } else {
            repaint();
        }
    }

    // Renderiza la cinemática: fondo + caja de texto + texto actual
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();

        // Dibujar fondo (imagen o negro)
        if (fondos != null && index < fondos.length && fondos[index] != null) {
            g.drawImage(fondos[index], 0, 0, w, h, this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
        }

        // Crear caja de texto en la parte inferior
        int boxW = (int) (w * 0.8);
        int boxH = (int) (h * 0.25);
        int bx = (w - boxW) / 2;
        int by = (h - boxH) - (int)(h * 0.08);
        g.setColor(new Color(30,30,30));
        g.fillRect(bx, by, boxW, boxH);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(6));
        g2.setColor(Color.WHITE);
        g2.drawRect(bx, by, boxW, boxH);

        // Renderizar texto actual con ajuste de líneas
        String texto = (index < textos.length) ? textos[index] : "";
        g.setColor(Color.WHITE);
        Font f = new Font("Monospaced", Font.BOLD, Math.max(18, h/36));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int margin = 24;
        int tx = bx + margin;
        int ty = by + margin + fm.getAscent();

        for (String linea : wrapText(texto, fm, boxW - 2*margin)) {
            g.drawString(linea, tx, ty);
            ty += fm.getHeight() + 6;
        }

        // Mostrar instrucciones para continuar
        String hint = "Presiona una tecla o haz clic para continuar";
        Font fh = new Font("Monospaced", Font.PLAIN, Math.max(14, h/48));
        g.setFont(fh);
        FontMetrics fmh = g.getFontMetrics();
        int hx = bx + (boxW - fmh.stringWidth(hint)) / 2;
        int hy = by + boxH - fmh.getDescent() - 10;
        g.drawString(hint, hx, hy);
    }

    // Ajusta el texto a múltiples líneas según el ancho disponible
    private java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null) return lines;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) <= maxWidth) {
                line = new StringBuilder(test);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(w);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }


    // Muestra cinemáticas en pantalla completa (método estático bloqueante)
    public static void mostrarCinematicasBlocking(String[] textos) {
        JDialog d = new JDialog((Frame) null, "Cinemáticas", true);
        d.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        d.setSize(screen.width, screen.height);
        CinematicManager panel = new CinematicManager(textos);
        d.setContentPane(panel);
        d.setBackground(Color.BLACK);

        // Timer para detectar cuando termina la cinemática
        javax.swing.Timer t = new javax.swing.Timer(150, e -> {
            if (panel.isTerminado()) { ((javax.swing.Timer)e.getSource()).stop(); d.dispose(); }
        });
        t.start();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        panel.requestFocusInWindow();
    }


    // Muestra cinemáticas con fondos personalizados (método estático bloqueante)
    public static void mostrarCinematicasConFondosBlocking(String[] textos, String[] fondosClasspath) {
        Image[] imgs = null;
        if (fondosClasspath != null) {
            imgs = new Image[fondosClasspath.length];
            // Cargar imágenes de fondo desde classpath o filesystem
            for (int i = 0; i < fondosClasspath.length; i++) {
                try {
                    String path = fondosClasspath[i];
                    java.net.URL url = CinematicManager.class.getResource(path);
                    if (url != null) {
                        imgs[i] = new ImageIcon(url).getImage();
                    } else {
                        java.io.File fSrc = new java.io.File("src" + path);
                        if (fSrc.exists()) imgs[i] = new ImageIcon(fSrc.toURI().toURL()).getImage();
                    }
                } catch (Exception ignored) {}
            }
        }
        // Crear diálogo en pantalla completa
        JDialog d = new JDialog((Frame) null, "Cinemáticas", true);
        d.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        d.setSize(screen.width, screen.height);
        CinematicManager panel = new CinematicManager(textos, imgs);
        d.setContentPane(panel);
        d.setBackground(Color.BLACK);
        // Timer para detectar finalización
        javax.swing.Timer t = new javax.swing.Timer(150, e -> {
            if (panel.isTerminado()) { ((javax.swing.Timer)e.getSource()).stop(); d.dispose(); }
        });
        t.start();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        panel.requestFocusInWindow();
    }

    // Getter para verificar si la cinemática ha terminado
    public boolean isTerminado() { return terminado; }
}
