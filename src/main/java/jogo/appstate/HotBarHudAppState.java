package jogo.appstate;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import jogo.gameobject.character.Character;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.ItemSlot;
import jogo.gameobject.item.ToolItem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class HotBarHudAppState extends BaseAppState {

    private final Node guiNode;
    private final AssetManager assetManager;
    private final PlayerAppState playerState;
    private Node hotbarNode;
    private Picture hotbarPicture;
    private Picture selectedSlotIndicator;
    private Picture heldItemDisplay;

    private final PropertyChangeListener listener = this::onHotbarChange;

    private final int SLOT_COUNT = 9;
    private Picture[] slotIcons = new Picture[SLOT_COUNT];
    private BitmapText[] quantityTexts = new BitmapText[SLOT_COUNT];
    private int currentSelectedSlot = 0;
    private float slotWidth;   //Calculado após escala
    private float slotHeight; //Calculado após escala
    private BitmapFont font;

    public HotBarHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.playerState = playerState;
    }

    @Override
    protected void initialize(com.jme3.app.Application app) {
        hotbarNode = new Node("HotBarNode");
        font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        String texturePath = "Interface/HotBar_craft.png";
        Texture tex = assetManager.loadTexture(texturePath);
        Image img = tex.getImage();
        if (img == null) {
            //Fallback simples: ainda tenta criar a Picture mesmo sem dimensões
            hotbarPicture = new Picture("hotbarPic");
            hotbarPicture.setImage(assetManager, texturePath, true);
        } else {
            hotbarPicture = new Picture("hotbarpic");
            hotbarPicture.setImage(assetManager, texturePath, true);
        }

        heldItemDisplay = new Picture("HeldItemOnScreen");
        heldItemDisplay.setImage(assetManager,"Interface/empty.png",true);
        hotbarNode.attachChild(heldItemDisplay);
        hotbarNode.attachChild(hotbarPicture);


        selectedSlotIndicator = new Picture("selectedSlotIndicator");
        selectedSlotIndicator.setImage(assetManager, "Interface/HotBar_Selected_icon_craft.png", true);
        hotbarNode.attachChild(selectedSlotIndicator);


        //Cria ícones de slot vazios
        for (int i = 0; i < SLOT_COUNT; i++) {
            Picture icon = new Picture("SlotIcon_" + i);
            icon.setWidth(32);   //Tamanho padrão — ajustado mais tarde
            icon.setHeight(32);
            icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);
            hotbarNode.attachChild(icon);
            slotIcons[i] = icon;

            BitmapText quantityText = new BitmapText(font);
            quantityText.setSize(18);
            quantityText.setText("");
            quantityText.setColor(ColorRGBA.White);
            hotbarNode.attachChild(quantityText);
            quantityTexts[i] = quantityText;

        }

        //Posiciona/escala de acordo com o ecrã (preservando aspect ratio)
        positionBottomCenter();

        //Liga ao player ou tenta novamente na próxima frame
        attachToPlayerOrDefer();
        updateSlotIcons();
        guiNode.attachChild(hotbarNode);

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
        //Só reagir a mudanças relacionadas com a hotbar/slots/inventory
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

        //Atualizar posição dos ícones com base nos slots
        for (int i = 0; i < SLOT_COUNT; i++) {

            float iconX = hotbarPicture.getLocalTranslation().x + (i * slotWidth) + (slotWidth / 2f) - (slotIcons[i].getWidth() / 2f);
            float iconY = hotbarPicture.getLocalTranslation().y + (slotHeight / 2f) - (slotIcons[i].getHeight() / 2f);

            slotIcons[i].setPosition(iconX, iconY);

            BitmapText text = quantityTexts[i];
            float textX = hotbarPicture.getLocalTranslation().x + (i * slotWidth) + slotWidth - text.getLineWidth() - 12;
            float textY = hotbarPicture.getLocalTranslation().y + 28; // Ajustar conforme necessário
            text.setLocalTranslation(textX,textY, 1);

        }
        updateSelectedSlotIndicator();

        float heldSize = screenHeight;
        heldItemDisplay.setWidth(heldSize);
        heldItemDisplay.setHeight(heldSize);

        float heldX = screenWidth - heldSize;
        float heldY = 0;

        heldItemDisplay.setPosition(heldX,heldY);
    }

    private void updateSlotIcons() {
        Player p = playerState.getPlayer();
        if (p == null) return;
        for (int i = 0; i < SLOT_COUNT; i++) {
            jogo.gameobject.item.ItemSlot itemSlot = p.getHotbar().get(i);
            Picture icon = slotIcons[i];
            BitmapText quantityText = quantityTexts[i];
            if (itemSlot != null && itemSlot.getItem() != null) {
                String itemTexturePath = itemSlot.getItem().getIconTexturePath();
                icon.setImage(assetManager, itemTexturePath, true);
                int quantity = itemSlot.getQuantity();
                System.out.println("Slot " + i + " quantidade: " + quantity);
                if (quantity > 1) {
                    quantityText.setText(String.valueOf(quantity));
                    quantityText.setColor(ColorRGBA.White);
                } else {
                    quantityText.setText(""); // Não mostrar "1"
                }
            } else {
                icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);
                quantityText.setText("");
            }
            updateQuantityTextPosition(i);
        }
        updateHeldItemVisual();
    }

    private void updateHeldItemVisual(){
        Player p = playerState.getPlayer();
        if (p == null || heldItemDisplay == null) return;

        ItemSlot selected = p.getHotbarSlot(currentSelectedSlot);

        if (selected != null && selected.getItem() instanceof ToolItem) {
            // Mostra o item
            heldItemDisplay.setCullHint(Node.CullHint.Never);
            String path = ((ToolItem) selected.getItem()).getIconHeldTexturePath();
            heldItemDisplay.setImage(assetManager, path, true);
        } else {
            // Esconde se não houver item
            heldItemDisplay.setCullHint(Node.CullHint.Always);
        }

    }

    private void updateQuantityTextPosition(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;

        BitmapText text = quantityTexts[slotIndex];
        float textX = hotbarPicture.getLocalTranslation().x + (slotIndex * slotWidth) + slotWidth - text.getLineWidth() - 12;
        float textY = hotbarPicture.getLocalTranslation().y + 28;
        text.setLocalTranslation(textX, textY, 1);
    }

    @Override
    protected void cleanup(com.jme3.app.Application app) {
        if (hotbarPicture != null) hotbarPicture.removeFromParent();
        if (selectedSlotIndicator != null) selectedSlotIndicator.removeFromParent();
        for (Picture icon : slotIcons) {
            if (icon != null) icon.removeFromParent();
        }
        for (BitmapText text : quantityTexts) {
            if (text != null) text.removeFromParent();
        }
        Character p = playerState.getPlayer();
        if (p != null) p.removePropertyChangeListener(listener);
    }

    @Override
    protected void onEnable() {
        if (hotbarNode != null) guiNode.attachChild(hotbarNode);
    }

    @Override
    protected void onDisable() {
        if (hotbarNode != null) hotbarNode.removeFromParent();
    }
    @Override
    public void update(float tpf) {
        super.update(tpf);

        //Verificar se houve input de seleção de slot
        InputAppState inputState = getState(InputAppState.class);
        if (inputState != null) {
            int selectedSlot = inputState.consumeSelectedSlot();
            if (selectedSlot >= 0 && selectedSlot < SLOT_COUNT) {
                setSelectedSlot(selectedSlot);
            }
        }
    }

    public void setSelectedSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;

        currentSelectedSlot = slotIndex;
        updateSelectedSlotIndicator();
        updateHeldItemVisual();
    }
    public int getSelectedSlot() {
        return currentSelectedSlot;
    }


    private void updateSelectedSlotIndicator() {
        if (selectedSlotIndicator == null) return;

        //Posicionar o indicador sobre o slot selecionado
        float indicatorX = hotbarPicture.getLocalTranslation().x + (currentSelectedSlot * slotWidth);
        float indicatorY = hotbarPicture.getLocalTranslation().y;

        //Ajustar tamanho do indicador para corresponder ao slot
        selectedSlotIndicator.setWidth(slotWidth);
        selectedSlotIndicator.setHeight(slotHeight);
        selectedSlotIndicator.setPosition(indicatorX, indicatorY);
    }
}