package engine;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {

    private final boolean[] keys = new boolean[256];

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = false;
    }

    public boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode < keys.length && keys[keyCode];
    }

    // W or UP arrow — move forward
    public boolean isMoveForward() {
        return isKeyDown(KeyEvent.VK_W) || isKeyDown(KeyEvent.VK_UP);
    }

    // S or DOWN arrow — move backward
    public boolean isMoveBack() {
        return isKeyDown(KeyEvent.VK_S) || isKeyDown(KeyEvent.VK_DOWN);
    }

    // A or LEFT arrow — rotate left (turn left)
    public boolean isRotLeft() {
        return isKeyDown(KeyEvent.VK_A) || isKeyDown(KeyEvent.VK_LEFT);
    }

    // D or RIGHT arrow — rotate right (turn right)
    public boolean isRotRight() {
        return isKeyDown(KeyEvent.VK_D) || isKeyDown(KeyEvent.VK_RIGHT);
    }

    public boolean isEscape() {
        return isKeyDown(KeyEvent.VK_ESCAPE);
    }
}
