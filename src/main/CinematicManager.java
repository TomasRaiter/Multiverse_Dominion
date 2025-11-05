package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Muestra una secuencia de cinemáticas estilo Undertale: fondo oscuro + cuadro de texto.
 */
public class CinematicManager extends JPanel {
    private final String[] textos;
    private final Image[] fondos;
    private int index = 0;
    private boolean terminado = false;

    public CinematicManager(String[] textos) { this(textos, null); }

    public CinematicManager(String[] textos, Image[] fondos) {
        this.textos = textos != null ? textos : new String[0];
        this.fondos = fondos;
        setBackground(Color.BLACK);
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { avanzar(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { avanzar(); }
        });
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();

        // Fondo: imagen si existe para este índice, si no negro
        if (fondos != null && index < fondos.length && fondos[index] != null) {
            g.drawImage(fondos[index], 0, 0, w, h, this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
        }

        // Cuadro de texto estilo Undertale
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

        // Texto centrado con margen
        String texto = (index < textos.length) ? textos[index] : "";
        g.setColor(Color.WHITE);
        Font f = new Font("Monospaced", Font.BOLD, Math.max(18, h/36));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int margin = 24;
        int tx = bx + margin;
        int ty = by + margin + fm.getAscent();
        // Romper líneas si es necesario
        for (String linea : wrapText(texto, fm, boxW - 2*margin)) {
            g.drawString(linea, tx, ty);
            ty += fm.getHeight() + 6;
        }

        // Hint para avanzar
        String hint = "Presiona una tecla o haz clic para continuar";
        Font fh = new Font("Monospaced", Font.PLAIN, Math.max(14, h/48));
        g.setFont(fh);
        FontMetrics fmh = g.getFontMetrics();
        int hx = bx + (boxW - fmh.stringWidth(hint)) / 2;
        int hy = by + boxH - fmh.getDescent() - 10;
        g.drawString(hint, hx, hy);
    }

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

    /**
     * Muestra las cinemáticas en pantalla completa de forma bloqueante.
     */
    public static void mostrarCinematicasBlocking(String[] textos) {
        JDialog d = new JDialog((Frame) null, "Cinemáticas", true);
        d.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        d.setSize(screen.width, screen.height);
        CinematicManager panel = new CinematicManager(textos);
        d.setContentPane(panel);
        d.setBackground(Color.BLACK);
        // Cerrar automáticamente cuando termine
        javax.swing.Timer t = new javax.swing.Timer(150, e -> {
            if (panel.isTerminado()) { ((javax.swing.Timer)e.getSource()).stop(); d.dispose(); }
        });
        t.start();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        panel.requestFocusInWindow();
    }

    /**
     * Muestra cinemáticas con imágenes de fondo por slide.
     * Cada texto se muestra sobre su imagen correspondiente.
     */
    public static void mostrarCinematicasConFondosBlocking(String[] textos, String[] fondosClasspath) {
        Image[] imgs = null;
        if (fondosClasspath != null) {
            imgs = new Image[fondosClasspath.length];
            for (int i = 0; i < fondosClasspath.length; i++) {
                try {
                    String path = fondosClasspath[i];
                    java.net.URL url = CinematicManager.class.getResource(path);
                    if (url != null) {
                        imgs[i] = new ImageIcon(url).getImage();
                    } else {
                        java.io.File fBin = new java.io.File("bin" + path);
                        if (fBin.exists()) {
                            imgs[i] = new ImageIcon(fBin.toURI().toURL()).getImage();
                        } else {
                            java.io.File fSrc = new java.io.File("src" + path);
                            if (fSrc.exists()) imgs[i] = new ImageIcon(fSrc.toURI().toURL()).getImage();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        JDialog d = new JDialog((Frame) null, "Cinemáticas", true);
        d.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        d.setSize(screen.width, screen.height);
        CinematicManager panel = new CinematicManager(textos, imgs);
        d.setContentPane(panel);
        d.setBackground(Color.BLACK);
        javax.swing.Timer t = new javax.swing.Timer(150, e -> {
            if (panel.isTerminado()) { ((javax.swing.Timer)e.getSource()).stop(); d.dispose(); }
        });
        t.start();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        panel.requestFocusInWindow();
    }

    public boolean isTerminado() { return terminado; }
}