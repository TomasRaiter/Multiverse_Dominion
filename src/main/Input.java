package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Input implements KeyListener {

    public boolean izquierda1, derecha1, agachar1;
    public boolean izquierda2, derecha2, agachar2;
    public boolean ataque1Pulse, ataque2Pulse;
    private boolean ataque1Down, ataque2Down;
    public boolean salto1, salto2, esc; public boolean salto1Pulse, salto2Pulse; private boolean salto1Down, salto2Down;
    // Selección de Pokémon
    public boolean fPulse; private boolean fDown;
    public boolean ePulse; private boolean eDown;
    public boolean navIzqPulse, navDerPulse; private boolean aDown, dDown;
    public boolean cPulse, gPulse, pPulse; private boolean cDown, gDown, pDown;
    // Selección de Pokémon J2
    public boolean lPulse; private boolean lDown;
    public boolean navIzq2Pulse, navDer2Pulse; private boolean leftDown, rightDown;

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        // Jugador 1
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

        // Jugador 2
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
        if (key == KeyEvent.VK_ESCAPE) esc = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        // Jugador 1
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

        // Jugador 2
        if (key == KeyEvent.VK_LEFT) { izquierda2 = false; leftDown = false; }
        if (key == KeyEvent.VK_RIGHT) { derecha2 = false; rightDown = false; }
        if (key == KeyEvent.VK_DOWN) agachar2 = false;
        if (key == KeyEvent.VK_ENTER) {
            ataque2Down = false;
        }
        if (key == KeyEvent.VK_L) { lDown = false; }
        if (key == KeyEvent.VK_W) { salto1 = false; salto1Down = false; }
        if (key == KeyEvent.VK_UP) { salto2 = false; salto2Down = false; }
        if (key == KeyEvent.VK_ESCAPE) esc = false;
        if (key == KeyEvent.VK_E) { eDown = false; }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
