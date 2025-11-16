package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Gestor de entrada de teclado para controles de ambos jugadores y menús
public class Input implements KeyListener {

    // Controles de movimiento para ambos jugadores
    public boolean izquierda1, derecha1, agachar1;  // Jugador 1: A, D, S
    public boolean izquierda2, derecha2, agachar2;  // Jugador 2: Flechas

    // Controles de ataque (detección de pulso único)
    public boolean ataque1Pulse, ataque2Pulse;      // R y Enter
    private boolean ataque1Down, ataque2Down;       // Estados internos

    // Controles de salto
    public boolean salto1, salto2, esc; 
    public boolean salto1Pulse, salto2Pulse; 
    private boolean salto1Down, salto2Down;         // W y Flecha Arriba

    // Controles especiales para menús de Ash (J1)
    public boolean fPulse; private boolean fDown;   // F: abrir selección Pokémon
    public boolean ePulse; private boolean eDown;   // E: cancelar selección
    public boolean navIzqPulse, navDerPulse;       // A/D: navegar menú
    private boolean aDown, dDown;
    public boolean cPulse, gPulse, pPulse;         // C/G/P: selección directa
    private boolean cDown, gDown, pDown;

    // Controles especiales para menús de Ash (J2)
    public boolean lPulse; private boolean lDown;   // L: abrir selección Pokémon J2
    public boolean navIzq2Pulse, navDer2Pulse;     // Flechas: navegar menú J2
    private boolean leftDown, rightDown;

    // Maneja las teclas presionadas (activar controles)
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Controles del Jugador 1 (WASD + R + F/E para Ash)

        if (key == KeyEvent.VK_A) { izquierda1 = true; if (!aDown) { navIzqPulse = true; aDown = true; } }
        if (key == KeyEvent.VK_D) { derecha1 = true; if (!dDown) { navDerPulse = true; dDown = true; } }
        if (key == KeyEvent.VK_S) agachar1 = true;
        if (key == KeyEvent.VK_R) {
            if (!ataque1Down) {
                ataque1Pulse = true;
                ataque1Down = true;
            }
        }
        if (key == KeyEvent.VK_F) {
            if (!fDown) { fPulse = true; fDown = true; }
        }
        if (key == KeyEvent.VK_E) {
            if (!eDown) { ePulse = true; eDown = true; }
        }
        if (key == KeyEvent.VK_C) { if (!cDown) { cPulse = true; cDown = true; } }
        if (key == KeyEvent.VK_G) { if (!gDown) { gPulse = true; gDown = true; } }
        if (key == KeyEvent.VK_P) { if (!pDown) { pPulse = true; pDown = true; } }

        // Controles del Jugador 2 (Flechas + Enter + L para Ash)

        if (key == KeyEvent.VK_LEFT) { izquierda2 = true; if (!leftDown) { navIzq2Pulse = true; leftDown = true; } }
        if (key == KeyEvent.VK_RIGHT) { derecha2 = true; if (!rightDown) { navDer2Pulse = true; rightDown = true; } }
        if (key == KeyEvent.VK_DOWN) agachar2 = true;
        if (key == KeyEvent.VK_ENTER) {
            if (!ataque2Down) {
                ataque2Pulse = true;
                ataque2Down = true;
            }
        }
        if (key == KeyEvent.VK_L) {
            if (!lDown) { lPulse = true; lDown = true; }
        }
        // Controles de salto
        if (key == KeyEvent.VK_W) {
            salto1 = true;
            if (!salto1Down) {
                salto1Pulse = true;
                salto1Down = true;
            }
        }
        if (key == KeyEvent.VK_UP) {
            salto2 = true;
            if (!salto2Down) {
                salto2Pulse = true;
                salto2Down = true;
            }
        }
        // Control de pausa
        if (key == KeyEvent.VK_ESCAPE) esc = true;
    }

    // Maneja las teclas liberadas (desactivar controles)
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        // Liberar controles del Jugador 1

        if (key == KeyEvent.VK_A) { izquierda1 = false; aDown = false; }
        if (key == KeyEvent.VK_D) { derecha1 = false; dDown = false; }
        if (key == KeyEvent.VK_S) agachar1 = false;
        if (key == KeyEvent.VK_R) {
            ataque1Down = false;
        }
        if (key == KeyEvent.VK_F) { fDown = false; }
        if (key == KeyEvent.VK_C) { cDown = false; }
        if (key == KeyEvent.VK_G) { gDown = false; }
        if (key == KeyEvent.VK_P) { pDown = false; }

        // Liberar controles del Jugador 2

        if (key == KeyEvent.VK_LEFT) { izquierda2 = false; leftDown = false; }
        if (key == KeyEvent.VK_RIGHT) { derecha2 = false; rightDown = false; }
        if (key == KeyEvent.VK_DOWN) agachar2 = false;
        if (key == KeyEvent.VK_ENTER) {
            ataque2Down = false;
        }
        if (key == KeyEvent.VK_L) { lDown = false; }
        
        // Liberar controles de salto y otros
        if (key == KeyEvent.VK_W) { salto1 = false; salto1Down = false; }
        if (key == KeyEvent.VK_UP) { salto2 = false; salto2Down = false; }
        if (key == KeyEvent.VK_ESCAPE) esc = false;
        if (key == KeyEvent.VK_E) { eDown = false; }
    }

    // Método requerido por KeyListener (no usado)
    @Override
    public void keyTyped(KeyEvent e) {
        // No se necesita implementación
    }
}

