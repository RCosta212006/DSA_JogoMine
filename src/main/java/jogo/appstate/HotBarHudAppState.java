package jogo.appstate;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import jogo.gameobject.character.Character;
import jogo.gameobject.character.Player;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class HotBarHudAppState extends BaseAppState {

    private final Node guiNode;
    private final AssetManager assetManager;
    private final PlayerAppState playerState;
    private Picture hotbarPicture;

    private final PropertyChangeListener listener = this::onHotbarChange;

    private final int SLOT_COUNT = 9;
    private Picture[] slotIcons = new Picture[SLOT_COUNT];
    private float slotWidth;   // calculado após escala
    private float slotHeight;  // calculado após escala

    public HotBarHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.playerState = playerState;
    }

    @Override
    protected void initialize(com.jme3.app.Application app) {
        String texturePath = "Interface/HotBar_craft.png";
        Texture tex = assetManager.loadTexture(texturePath);
        Image img = tex.getImage();
        if (img == null) {
            // fallback simples: ainda tenta criar a Picture mesmo sem dimensões
            hotbarPicture = new Picture("hotbarPic");
            hotbarPicture.setImage(assetManager, texturePath, true);
        } else {
            hotbarPicture = new Picture("hotbarpic");
            hotbarPicture.setImage(assetManager, texturePath, true);
        }
        guiNode.attachChild(hotbarPicture);
        // cria ícones de slot vazios
        for (int i = 0; i < SLOT_COUNT; i++) {
            Picture icon = new Picture("SlotIcon_" + i);
            icon.setWidth(32);   // tamanho padrão — ajustado mais tarde
            icon.setHeight(32);
            icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);
            guiNode.attachChild(icon);
            slotIcons[i] = icon;
        }

        // posiciona/escala de acordo com o ecrã (preservando aspect ratio)
        positionBottomCenter();

        // liga ao player ou tenta novamente na próxima frame
        attachToPlayerOrDefer();

    }

    private void attachToPlayerOrDefer() {
        jogo.gameobject.character.Character p = playerState.getPlayer();
        if (p != null) {
            p.addPropertyChangeListener(listener);
        } else {
            getApplication().enqueue((Runnable) () -> {
                Character p2 = playerState.getPlayer();
                if (p2 != null) p2.addPropertyChangeListener(listener);
                positionBottomCenter();
            });
        }
    }

    private void onHotbarChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (prop == null) return; // ignorar eventos sem nome
        String lower = prop.toLowerCase();
        // só reagir a mudanças relacionadas com a hotbar/slots/inventory
        if (!(lower.contains("hotbar") || lower.contains("slot") || lower.contains("inventory"))) {
            return;
        }

        getApplication().enqueue((Runnable) () -> {
            updateSlotIcons();
        });
    }

    public void positionBottomCenter() {
        SimpleApplication sapp = (SimpleApplication) getApplication();
        float screenWidth = sapp.getCamera().getWidth();
        float screenHeight = sapp.getCamera().getHeight();

        float imgWidth = hotbarPicture.getWidth();
        float imgHeight = hotbarPicture.getHeight();

        float scaleFactor = 75f / imgWidth; // escala para largura de 200 pixels
        hotbarPicture.setWidth(imgWidth * scaleFactor*8);
        hotbarPicture.setHeight(imgHeight * scaleFactor);

        float xPos = (screenWidth - hotbarPicture.getWidth()) / 2;
        float yPos = (screenHeight - hotbarPicture.getHeight()) / 10;


        hotbarPicture.setPosition(xPos, yPos);

        slotWidth = hotbarPicture.getWidth() / SLOT_COUNT;
        slotHeight = hotbarPicture.getHeight();

        // Atualizar posição dos ícones com base nos slots
        for (int i = 0; i < SLOT_COUNT; i++) {

            float iconX = hotbarPicture.getLocalTranslation().x + (i * slotWidth) + (slotWidth / 2f) - (slotIcons[i].getWidth() / 2f);
            float iconY = hotbarPicture.getLocalTranslation().y + (slotHeight / 2f) - (slotIcons[i].getHeight() / 2f);

            slotIcons[i].setPosition(iconX, iconY);
        }
    }

    private void updateSlotIcons() {
        Player p = playerState.getPlayer();
        if (p == null) return;
        for (int i = 0; i < SLOT_COUNT; i++) {
            jogo.gameobject.item.ItemSlot itemSlot = p.getHotbar().get(i);
            Picture icon = slotIcons[i];
            if (itemSlot != null && itemSlot.getItem() != null) {
                String itemTexturePath = itemSlot.getItem().getIconTexturePath();
                icon.setImage(assetManager, itemTexturePath, true);
            } else {
                icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);
            }
        }
    }

    @Override
    protected void cleanup(com.jme3.app.Application app) {
        if (hotbarPicture != null) hotbarPicture.removeFromParent();
        Character p = playerState.getPlayer();
        if (p != null) p.removePropertyChangeListener(listener);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }
}