package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.ItemSlot;

public class InventoryHudAppState extends BaseAppState {
    private final Node guiNode;
    private final AssetManager assetManager;
    private final PlayerAppState playerState;
    private final InputAppState inputState;
    //Node que contem o inventario (para ligar e desligar
    private Node inventoryNode;

    private Picture backgroundPic;
    private Picture selectionCursor;
    private Picture heldItemIcon;
    private BitmapText heldItemQty;

    // Grelha visual: 4 linhas x 9 colunas
    // Linhas 0, 1, 2 = Inventário Principal
    // Linha 3 = Hotbar (fundo)
    private final int ROWS = 4;
    private final int COLS = 9;

    private Picture[][] slotIcons = new Picture[ROWS][COLS];
    private BitmapText[][] slotTexts = new BitmapText[ROWS][COLS];

    // estado da navegação e item segurado
    private int selectedRow = 0;
    private int selectedCol = 0;
    private ItemSlot heldItem = null;

    // escala da HUD
    private  float scaleFactor = 1.0f;
    private float slotSize;
    private boolean inventoryVisible = false;

    public InventoryHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState, InputAppState inputState) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.playerState = playerState;
        this.inputState = inputState;
    }



    @Override
    protected void initialize(Application app){
       inventoryNode = new Node("inventoryGui");
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        //fundo do inventario
        backgroundPic = new Picture("inventoryBackground");
        backgroundPic.setImage(assetManager, "Interface/Inventory_craft.png", true);

        SimpleApplication sapp = (SimpleApplication) app;
        float screenW = sapp.getCamera().getWidth();
        float screenH = sapp.getCamera().getHeight();

        // Escalar a imagem para ocupar, por exemplo, 60% da altura do ecrã
        float imgOrigH = 166f;
        float imgOrigW = 176f;

        // Define escala para que fique visível e nítido (pixel art)
        scaleFactor = (screenH * 0.85f) / imgOrigH;
        float finalW = imgOrigW * scaleFactor;
        float finalH = imgOrigH * scaleFactor;

        backgroundPic.setWidth(finalW);
        backgroundPic.setHeight(finalH);

        // posição no centro do ecrã
        float bgX = (screenW - finalW) / 2f;
        float bgY = (screenH - finalH) / 2f;
        backgroundPic.setPosition(bgX, bgY);
        inventoryNode.attachChild(backgroundPic);

        //calcular grelha de slots
        slotSize = 16 * scaleFactor; // tamanho do slot
        float slotSpacing = 18 * scaleFactor; // espaçamento entre slots
        float girdOffsetX = 7 * scaleFactor;

        //mapear linhas ( linhas 0-2 ivnetario e linhas 3 hotbar(linha mais a baixo))
        float hotbarY = bgY + (7 *scaleFactor);
        float invY = bgY + (38 * scaleFactor);

        //criar Slots
        for ( int r = 0; r < ROWS; r++){
            for ( int c = 0; c < COLS; c++){
                Picture icon = new Picture("Slot_" + r + "_"+ c );
                icon.setWidth(slotSize);
                icon.setHeight(slotSize);
                icon.setImage(assetManager,"Interface/Empty_item_craft.png", true );

                float x= bgX + girdOffsetX + (c * slotSpacing);
                float y;

                if (r == 3){
                    y = hotbarY;
                } else{
                    y = invY + ((2-r) * slotSpacing) + (4 * scaleFactor);
                }

                icon.setPosition(x,y);
                slotIcons[r][c] = icon;
                inventoryNode.attachChild(icon);

                //texto de quantidade
                BitmapText txt = new BitmapText(font);
                txt.setSize(font.getCharSet().getRenderedSize() * 0.8f); // Texto mais pequeno
                txt.setColor(ColorRGBA.White);
                txt.setText("");
                txt.setLocalTranslation(x, y + (slotSize/2), 1);
                slotTexts[r][c] = txt;
                inventoryNode.attachChild(txt);
            }
        }

        // cursor de seleção
        selectionCursor = new Picture("Cursor");
        selectionCursor.setImage(assetManager,"Interface/HotBar_Selected_icon_craft.png",true);

        //aumentar o tamanho do cursor
        float cursorSize = 24 * scaleFactor;
        selectionCursor.setWidth(cursorSize);
        selectionCursor.setHeight(cursorSize);
        selectionCursor.setLocalTranslation(0, 0, 2); // Z=2 para ficar acima
        inventoryNode.attachChild(selectionCursor);

        //Item agarrado
        heldItemIcon = new Picture("HeldItem");
        heldItemIcon.setWidth(slotSize);
        heldItemIcon.setHeight(slotSize);
        heldItemIcon.setLocalTranslation(0, 0, 5); // Z muito alto
        heldItemIcon.setCullHint(Node.CullHint.Always); // Escondido por defeito
        inventoryNode.attachChild(heldItemIcon);

        heldItemQty = new BitmapText(font);
        heldItemQty.setSize(font.getCharSet().getRenderedSize());
        heldItemQty.setColor(ColorRGBA.White);
        heldItemQty.setLocalTranslation(0,0,6);
        inventoryNode.attachChild(heldItemQty);


        setInventoryVisible(false);
    }

    public void setInventoryVisible(boolean visible){
        this.inventoryVisible = visible;
        HotBarHudAppState hotbarHud = getState(HotBarHudAppState.class);
        if (hotbarHud != null) {
            hotbarHud.setEnabled(!visible);
        }
        if (visible) {
            guiNode.attachChild(inventoryNode);
            inputState.setMouseCaptured(false);
        } else{
            guiNode.detachChild(inventoryNode);
            inputState.setMouseCaptured(true);
             if (heldItem != null){
                 heldItem = null;
                 updateHeldItemVisual();
             }
        }
    }

    @Override
    public void update (float tpf){
        if (inputState.consumeInventoryToggle()){
            setInventoryVisible(!inventoryVisible);
        }
        if (!inventoryVisible) return;

        if (inputState.consumeUiUp()) {
            selectedRow--;
            if (selectedRow < 0) selectedRow = ROWS - 1;
        }
        if (inputState.consumeUiDown()) {
            selectedRow++;
            if (selectedRow >= ROWS) selectedRow = 0;
        }
        if (inputState.consumeUiLeft()) {
            selectedCol--;
            if (selectedCol < 0) selectedCol = COLS - 1;
        }
        if (inputState.consumeUiRight()) {
            selectedCol++;
            if (selectedCol >= COLS) selectedCol = 0;
        }
        if (inputState.consumeUiSelect()) {
            handleEnterKey();
        }
        updateSelectionVisuals();
        updateSlotVisuals();
        updateHeldItemVisual();
    }

    private void handleEnterKey(){
        Player p = playerState.getPlayer();
        ItemSlot targetSlot = getSlotAt(selectedRow, selectedCol);

        //caso 1: nao esta nenhum item a ser agarrado
        if (heldItem == null){
            if (targetSlot != null && targetSlot.getItem() != null){
                heldItem = targetSlot;
                setSlotAt(selectedRow,selectedCol,null);// esvazia o slot que pegamos
            }
        }
        // caso 2: item agarrado que queremos largar
        else{
            if (targetSlot == null){
                setSlotAt(selectedRow,selectedCol,heldItem);
                heldItem = null;
            }
            else{
                System.out.println("Slot ocupado!");
            }
        }
    }

    private ItemSlot getSlotAt(int r, int c) {
        Player p = playerState.getPlayer();
        if (r == 3){
            return p.getHotbarSlot(c);
        }else{
            int index = (r * COLS) + c;
            return p.getInventorySlot(index);
        }
    }

    private void setSlotAt(int r, int c, ItemSlot slot){
        Player p = playerState.getPlayer();
        if (r == 3) {
            p.setHotbarSlot(c, slot);
        } else{
            int index = (r * COLS) + c;
            p.setInventorySlot(index, slot);
        }
    }

    private void updateSelectionVisuals(){
        //move o cursor para o slot
        Picture targetIcon = slotIcons[selectedRow][selectedCol];
        Vector3f pos = targetIcon.getLocalTranslation();

        // Centrar o cursor sobre o ícone (o cursor é maior)
        float diff = (selectionCursor.getWidth() - targetIcon.getWidth()) / 2;
        selectionCursor.setLocalTranslation(pos.x - diff, pos.y - diff, 2);

    }

    private void updateHeldItemVisual(){
        if (heldItem != null) {
            heldItemIcon.setCullHint(Node.CullHint.Never);
            // O item agarrado segue o cursor de seleção
            Vector3f cursorWorldPos = selectionCursor.getLocalTranslation();
            // Centrar
            float diff = (selectionCursor.getWidth() - heldItemIcon.getWidth()) / 2;
            heldItemIcon.setLocalTranslation(cursorWorldPos.x + diff, cursorWorldPos.y + diff, 10);

            // Atualizar textura e texto
            heldItemIcon.setImage(assetManager, heldItem.getItem().getIconTexturePath(), true);
            heldItemQty.setText(String.valueOf(heldItem.getQuantity()));
            heldItemQty.setLocalTranslation(cursorWorldPos.x + diff, cursorWorldPos.y + diff, 11);
        } else {
            heldItemIcon.setCullHint(Node.CullHint.Always);
            heldItemQty.setText("");
        }
    }

    private void updateSlotVisuals(){
        for (int r = 0; r < ROWS; r++){
            for ( int c = 0; c < COLS; c++){
                ItemSlot slot = getSlotAt(r,c);
                Picture p = slotIcons[r][c];
                BitmapText t = slotTexts[r][c];

                if(slot != null && slot.getItem() != null){
                    p.setImage(assetManager,slot.getItem().getIconTexturePath(),true);
                    t.setText((String.valueOf(slot.getQuantity())));
                }else{
                    p.setImage(assetManager, "Interface/Empty_item_craft.png", true); // ou transparente
                    t.setText("");
                }

            }
        }
    }



    @Override
    protected void onEnable(){
    }

    @Override
    protected void onDisable(){
        inventoryNode.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {
        inventoryNode.removeFromParent();
    }

}
