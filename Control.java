import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Control extends KeyAdapter {
    // Asynchronous key-state mapping registers
    private boolean keyUp = false;
    private boolean keyDown = false;
    private boolean keyLeft = false;
    private boolean keyRight = false;

    public Control() {
        // Explicit default constructor
    }

    /**
     * Intercepts key-down hardware triggers and maps them to clean state flags.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    keyUp = true;    break;
            case KeyEvent.VK_DOWN:  keyDown = true;  break;
            case KeyEvent.VK_LEFT:  keyLeft = true;  break;
            case KeyEvent.VK_RIGHT: keyRight = true; break;
            default: break;
        }
    }

    /**
     * Intercepts key-up hardware triggers and clears state flags asynchronously.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    keyUp = false;    break;
            case KeyEvent.VK_DOWN:  keyDown = false;  break;
            case KeyEvent.VK_LEFT:  keyLeft = false;  break;
            case KeyEvent.VK_RIGHT: keyRight = false; break;
            default: break;
        }
    }

    /**
     * Resets all structural input state flags back to a resting false baseline.
     */
    public void reset() {
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
    }

    // --- Core Input Vector State Getters ---
    public boolean isKeyUp()    { return keyUp; }
    public boolean isKeyDown()  { return keyDown; }
    public boolean isKeyLeft()  { return keyLeft; }
    public boolean isKeyRight() { return keyRight; }
}