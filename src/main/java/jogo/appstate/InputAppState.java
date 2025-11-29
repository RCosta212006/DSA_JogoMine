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
    private volatile boolean placeRequested;
    private volatile boolean toggleShadingRequested;
    private volatile boolean respawnRequested;
    private volatile boolean interactRequested;
    private float mouseDX, mouseDY;
    private boolean mouseCaptured = true;
    private volatile int selectedSlotRequested = -1;
    private volatile boolean inventoryToggleRequested;
    private volatile boolean uiUp, uiDown, uiLeft, uiRight, uiSelect;

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
        // Place voxel (right mouse)
        im.addMapping("Place", new MouseButtonTrigger(com.jme3.input.MouseInput.BUTTON_RIGHT));
        // Toggle shading (L)
        im.addMapping("ToggleShading", new KeyTrigger(KeyInput.KEY_L));
        // Respawn (R)
        im.addMapping("Respawn", new KeyTrigger(KeyInput.KEY_R));
        // Interact (E)
        im.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));
        // Hotbar slots (1-9)
        im.addMapping("Slot1", new KeyTrigger(KeyInput.KEY_1));
        im.addMapping("Slot2", new KeyTrigger(KeyInput.KEY_2));
        im.addMapping("Slot3", new KeyTrigger(KeyInput.KEY_3));
        im.addMapping("Slot4", new KeyTrigger(KeyInput.KEY_4));
        im.addMapping("Slot5", new KeyTrigger(KeyInput.KEY_5));
        im.addMapping("Slot6", new KeyTrigger(KeyInput.KEY_6));
        im.addMapping("Slot7", new KeyTrigger(KeyInput.KEY_7));
        im.addMapping("Slot8", new KeyTrigger(KeyInput.KEY_8));
        im.addMapping("Slot9", new KeyTrigger(KeyInput.KEY_9));
        //Inventory and Ui navigation
        im.addMapping("ToggleInventory", new KeyTrigger(KeyInput.KEY_I));
        im.addMapping("UiUp", new KeyTrigger(KeyInput.KEY_UP));
        im.addMapping("UiDown", new KeyTrigger(KeyInput.KEY_DOWN));
        im.addMapping("UiLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        im.addMapping("UiRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        im.addMapping("UiSelect", new KeyTrigger(KeyInput.KEY_RETURN));


        im.addListener(this, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight",
                "Jump", "Sprint", "ToggleMouse", "Break", "Place", "ToggleShading", "Respawn", "Interact",
                "Slot1", "Slot2", "Slot3", "Slot4", "Slot5", "Slot6", "Slot7", "Slot8", "Slot9",
                "ToggleInventory","UiUp","UiDown","UiLeft","UiRight","UiSelect");

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
        im.deleteMapping("Place");
        im.deleteMapping("ToggleShading");
        im.deleteMapping("Respawn");
        im.deleteMapping("Interact");
        im.deleteMapping("Slot1");
        im.deleteMapping("Slot2");
        im.deleteMapping("Slot3");
        im.deleteMapping("Slot4");
        im.deleteMapping("Slot5");
        im.deleteMapping("Slot6");
        im.deleteMapping("Slot7");
        im.deleteMapping("Slot8");
        im.deleteMapping("Slot9");
        im.deleteMapping("ToggleInventory");
        im.deleteMapping("UiUp");
        im.deleteMapping("UiDown");
        im.deleteMapping("UiLeft");
        im.deleteMapping("UiRight");
        im.deleteMapping("UiSelect");
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
            case "Place" -> {
                if (isPressed && mouseCaptured) placeRequested = true;
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
            case "Slot1" -> { if (isPressed) selectedSlotRequested = 0; }
            case "Slot2" -> { if (isPressed) selectedSlotRequested = 1; }
            case "Slot3" -> { if (isPressed) selectedSlotRequested = 2; }
            case "Slot4" -> { if (isPressed) selectedSlotRequested = 3; }
            case "Slot5" -> { if (isPressed) selectedSlotRequested = 4; }
            case "Slot6" -> { if (isPressed) selectedSlotRequested = 5; }
            case "Slot7" -> { if (isPressed) selectedSlotRequested = 6; }
            case "Slot8" -> { if (isPressed) selectedSlotRequested = 7; }
            case "Slot9" -> { if (isPressed) selectedSlotRequested = 8; }

            case "ToggleInventory" -> { if (isPressed) inventoryToggleRequested = true;}
            case "UiUp" -> { if (isPressed) uiUp = true; }
            case "UiDown" -> { if (isPressed) uiDown = true; }
            case "UiLeft" -> { if (isPressed) uiLeft = true; }
            case "UiRight" -> { if (isPressed) uiRight = true; }
            case "UiSelect" -> { if (isPressed) uiSelect = true; }

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

    public boolean consumePlaceRequested() {
        boolean r = placeRequested;
        placeRequested = false;
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
    public int consumeSelectedSlot() {
        int slot = selectedSlotRequested;
        selectedSlotRequested = -1;
        return slot;
    }

    public boolean consumeInventoryToggle() {
        boolean r = inventoryToggleRequested;
        inventoryToggleRequested = false;
        return r;
    }

    public boolean consumeUiUp() { boolean r = uiUp; uiUp = false; return r; }
    public boolean consumeUiDown() { boolean r = uiDown; uiDown = false; return r; }
    public boolean consumeUiLeft() { boolean r = uiLeft; uiLeft = false; return r; }
    public boolean consumeUiRight() { boolean r = uiRight; uiRight = false; return r; }
    public boolean consumeUiSelect() { boolean r = uiSelect; uiSelect = false; return r; }

}
