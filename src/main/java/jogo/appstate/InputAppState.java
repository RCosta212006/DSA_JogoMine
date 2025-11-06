package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class InputAppState extends BaseAppState implements ActionListener, AnalogListener {

    private boolean forward, backward, left, right;
    private boolean sprint;
    private volatile boolean jumpRequested;
    private volatile boolean breakRequested;
    private volatile boolean toggleShadingRequested;
    private volatile boolean respawnRequested;
    private volatile boolean interactRequested;
    private float mouseDX, mouseDY;
    private boolean mouseCaptured = true;

    @Override
    protected void initialize(Application app) {
        var im = app.getInputManager();
        // Movement keys
        im.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        im.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        // Mouse look
        im.addMapping("MouseX+", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        im.addMapping("MouseX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        im.addMapping("MouseY+", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        im.addMapping("MouseY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        // Toggle capture (use TAB, ESC exits app by default)
        im.addMapping("ToggleMouse", new KeyTrigger(KeyInput.KEY_TAB));
        // Break voxel (left mouse)
        im.addMapping("Break", new MouseButtonTrigger(com.jme3.input.MouseInput.BUTTON_LEFT));
        // Toggle shading (L)
        im.addMapping("ToggleShading", new KeyTrigger(KeyInput.KEY_L));
        // Respawn (R)
        im.addMapping("Respawn", new KeyTrigger(KeyInput.KEY_R));
        // Interact (E)
        im.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));

        im.addListener(this, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight", "Jump", "Sprint", "ToggleMouse", "Break", "ToggleShading", "Respawn", "Interact");
        im.addListener(this, "MouseX+", "MouseX-", "MouseY+", "MouseY-");
    }

    @Override
    protected void cleanup(Application app) {
        var im = app.getInputManager();
        im.deleteMapping("MoveForward");
        im.deleteMapping("MoveBackward");
        im.deleteMapping("MoveLeft");
        im.deleteMapping("MoveRight");
        im.deleteMapping("Jump");
        im.deleteMapping("Sprint");
        im.deleteMapping("MouseX+");
        im.deleteMapping("MouseX-");
        im.deleteMapping("MouseY+");
        im.deleteMapping("MouseY-");
        im.deleteMapping("ToggleMouse");
        im.deleteMapping("Break");
        im.deleteMapping("ToggleShading");
        im.deleteMapping("Respawn");
        im.deleteMapping("Interact");
        im.removeListener(this);
    }

    @Override
    protected void onEnable() {
        setMouseCaptured(true);
    }

    @Override
    protected void onDisable() { }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "MoveForward" -> forward = isPressed;
            case "MoveBackward" -> backward = isPressed;
            case "MoveLeft" -> left = isPressed;
            case "MoveRight" -> right = isPressed;
            case "Sprint" -> sprint = isPressed;
            case "Jump" -> {
                if (isPressed) jumpRequested = true;
            }
            case "ToggleMouse" -> {
                if (isPressed) setMouseCaptured(!mouseCaptured);
            }
            case "Break" -> {
                if (isPressed && mouseCaptured) breakRequested = true;
            }
            case "ToggleShading" -> {
                if (isPressed) toggleShadingRequested = true;
            }
            case "Respawn" -> {
                if (isPressed) respawnRequested = true;
            }
            case "Interact" -> {
                if (isPressed && mouseCaptured) interactRequested = true;
            }
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (!mouseCaptured) return;
        switch (name) {
            case "MouseX+" -> mouseDX += value;
            case "MouseX-" -> mouseDX -= value;
            case "MouseY+" -> mouseDY += value;
            case "MouseY-" -> mouseDY -= value;
        }
    }

    public Vector3f getMovementXZ() {
        float fb = (forward ? 1f : 0f) + (backward ? -1f : 0f);
        float lr = (right ? 1f : 0f) + (left ? -1f : 0f);
        return new Vector3f(lr, 0f, -fb); // -fb so forward maps to -Z in JME default
    }

    public boolean isSprinting() {
        return sprint;
    }

    public boolean consumeJumpRequested() {
        boolean jr = jumpRequested;
        jumpRequested = false;
        return jr;
    }

    public boolean consumeBreakRequested() {
        boolean r = breakRequested;
        breakRequested = false;
        return r;
    }

    public boolean consumeToggleShadingRequested() {
        boolean r = toggleShadingRequested;
        toggleShadingRequested = false;
        return r;
    }

    public boolean consumeRespawnRequested() {
        boolean r = respawnRequested;
        respawnRequested = false;
        return r;
    }

    public boolean consumeInteractRequested() {
        boolean r = interactRequested;
        interactRequested = false;
        return r;
    }

    public Vector2f consumeMouseDelta() {
        Vector2f d = new Vector2f(mouseDX, mouseDY);
        mouseDX = 0f;
        mouseDY = 0f;
        return d;
    }

    public void setMouseCaptured(boolean captured) {
        this.mouseCaptured = captured;
        var im = getApplication().getInputManager();
        im.setCursorVisible(!captured);
        // Clear accumulated deltas when switching state
        mouseDX = 0f;
        mouseDY = 0f;
    }

    public boolean isMouseCaptured() {
        return mouseCaptured;
    }
}
