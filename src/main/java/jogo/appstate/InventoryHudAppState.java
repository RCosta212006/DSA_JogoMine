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
    //Node que contem o inventario (para ligar e desligar)
    private Node inventoryNode;

    private Picture backgroundPic;
    private Picture selectionCursor;
    private Picture heldItemIcon;
    private BitmapText heldItemQty;
    // Variáveis de Layout (para serem usadas em todos os métodos de setup)
    private float bgX;
    private float bgY;

    //Grelha visual: 4 linhas x 9 colunas
    //Linhas 0, 1, 2 = Inventário Principal
    //Linha 3 = Hotbar (fundo)
    private final int ROWS = 4;
    private final int COLS = 9;
    private Picture[][] slotIcons = new Picture[ROWS][COLS];
    private BitmapText[][] slotTexts = new BitmapText[ROWS][COLS];

    //Estado da navegação e item segurado
    private int selectedRow = 0;
    private int selectedCol = 0;
    private ItemSlot heldItem = null;

    //Escala da HUD
    private  float scaleFactor = 1.0f;
    private float slotSize;
    private boolean inventoryVisible = false;

    //Variáveis Crafting
    private Picture[] craftingIcons = new Picture[4];
    private BitmapText[] craftingTexts = new BitmapText[4];
    private Picture resultIcon;
    private BitmapText resultText;

    //Dividir Inventario por secções
    private static final int SECTION_INVENTORY = 0;
    private static final int SECTION_CRAFTING = 1;
    private static final int SECTION_RESULT = 2;

    private int currentSection = SECTION_INVENTORY;

    public InventoryHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState, InputAppState inputState) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.playerState = playerState;
        this.inputState = inputState;
    }



    @Override
    protected void initialize(Application app) {
        inventoryNode = new Node("inventoryGui");
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        // Configurar tamanhos, escalas e o fundo
        setupBackgroundAndLayout(app);

        // Criar as grelhas de itens
        setupInventoryGrid(font);
        setupCraftingGrid(font);
        setupResultSlot(font);

        // Criar cursor e itens flutuantes
        setupCursorAndHeldItem(font);

        // Começa escondido
        setInventoryVisible(false);
    }

    private void setupBackgroundAndLayout(Application app) {
        backgroundPic = new Picture("inventoryBackground");
        backgroundPic.setImage(assetManager, "Interface/Inventory_craft.png", true);

        SimpleApplication sapp = (SimpleApplication) app;
        float screenW = sapp.getCamera().getWidth();
        float screenH = sapp.getCamera().getHeight();

        // Dados da imagem original
        float imgOrigH = 166f;
        float imgOrigW = 176f;

        // Calcular Escala Global
        scaleFactor = (screenH * 0.85f) / imgOrigH;
        slotSize = 16 * scaleFactor; // Atualiza o tamanho do slot para usar nos outros métodos

        float finalW = imgOrigW * scaleFactor;
        float finalH = imgOrigH * scaleFactor;

        backgroundPic.setWidth(finalW);
        backgroundPic.setHeight(finalH);

        // Calcular e guardar as posições centrais (bgX e bgY) nas variáveis da classe
        bgX = (screenW - finalW) / 2f;
        bgY = (screenH - finalH) / 2f;

        backgroundPic.setPosition(bgX, bgY);
        inventoryNode.attachChild(backgroundPic);
    }


    private void setupInventoryGrid(BitmapFont font) {
        float slotSpacing = 18 * scaleFactor;
        float gridOffsetX = 7 * scaleFactor;

        // Posições base Y
        float hotbarY = bgY + (7 * scaleFactor);
        float invY = bgY + (38 * scaleFactor);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Cria o ícone
                Picture icon = new Picture("Slot_" + r + "_" + c);
                icon.setWidth(slotSize);
                icon.setHeight(slotSize);
                icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);
                // Calcula posições
                float x = bgX + gridOffsetX + (c * slotSpacing);
                float y;

                if (r == 3) {
                    y = hotbarY; // Linha da Hotbar
                } else {
                    y = invY + ((2 - r) * slotSpacing) + (4 * scaleFactor); // Linhas do inventário
                }

                icon.setPosition(x, y);
                slotIcons[r][c] = icon;
                inventoryNode.attachChild(icon);

                // Cria o texto de quantidade
                BitmapText txt = new BitmapText(font);
                txt.setSize(font.getCharSet().getRenderedSize() * 0.8f);
                txt.setColor(ColorRGBA.White);
                txt.setText("");
                // Centralizar texto (Opcional: ajustei ligeiramente a posição Z para 1)
                txt.setLocalTranslation(x, y + (slotSize / 2), 1);
                slotTexts[r][c] = txt;
                inventoryNode.attachChild(txt);
            }
        }
    }
    private void setupCraftingGrid(BitmapFont font) {
        float craftingStartX = bgX + (36 * scaleFactor);
        float craftingStartY = bgY + (130 * scaleFactor);
        float craftSpacing = 18 * scaleFactor;

        for (int i = 0; i < 4; i++) {
            Picture icon = new Picture("CraftSlot_" + i);
            icon.setWidth(slotSize);
            icon.setHeight(slotSize);
            icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);

            int row = i / 2; // 0 ou 1
            int col = i % 2; // 0 ou 1

            float x = craftingStartX + (col * craftSpacing);
            float y = craftingStartY - (row * craftSpacing);

            icon.setPosition(x, y);
            inventoryNode.attachChild(icon);
            craftingIcons[i] = icon;

            BitmapText txt = new BitmapText(font);
            txt.setSize(font.getCharSet().getRenderedSize() * 0.8f);
            txt.setColor(ColorRGBA.White);
            txt.setText("");
            txt.setLocalTranslation(x, y + (slotSize / 2), 1);
            inventoryNode.attachChild(txt);
            craftingTexts[i] = txt;
        }
    }

    private void setupResultSlot(BitmapFont font) {
        float craftingStartY = bgY + (130 * scaleFactor);
        float craftSpacing = 18 * scaleFactor;

        resultIcon = new Picture("ResultSlot");
        resultIcon.setWidth(slotSize);
        resultIcon.setHeight(slotSize);
        resultIcon.setImage(assetManager, "Interface/Empty_item_craft.png", true);

        float resultX = bgX + (134 * scaleFactor);
        float resultY = craftingStartY - (0.5f * craftSpacing);

        resultIcon.setPosition(resultX, resultY);
        inventoryNode.attachChild(resultIcon);

        resultText = new BitmapText(font);
        resultText.setSize(font.getCharSet().getRenderedSize() * 0.8f);
        resultText.setColor(ColorRGBA.White);
        resultText.setText("");
        resultText.setLocalTranslation(resultX, resultY + (slotSize / 2), 1);
        inventoryNode.attachChild(resultText);
    }

    private void setupCursorAndHeldItem(BitmapFont font) {
        // Cursor de seleção
        selectionCursor = new Picture("Cursor");
        selectionCursor.setImage(assetManager, "Interface/HotBar_Selected_icon_craft.png", true);

        float cursorSize = 24 * scaleFactor;
        selectionCursor.setWidth(cursorSize);
        selectionCursor.setHeight(cursorSize);
        selectionCursor.setLocalTranslation(0, 0, 2);
        inventoryNode.attachChild(selectionCursor);

        // Item agarrado (Held Item)
        heldItemIcon = new Picture("HeldItem");
        heldItemIcon.setWidth(slotSize);
        heldItemIcon.setHeight(slotSize);
        heldItemIcon.setLocalTranslation(0, 0, 5); // Z alto para ficar em cima de tudo
        heldItemIcon.setCullHint(Node.CullHint.Always);
        inventoryNode.attachChild(heldItemIcon);

        heldItemQty = new BitmapText(font);
        heldItemQty.setSize(font.getCharSet().getRenderedSize());
        heldItemQty.setColor(ColorRGBA.White);
        heldItemQty.setLocalTranslation(0, 0, 6);
        inventoryNode.attachChild(heldItemQty);
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

        if (inputState.consumeUiUp()) moveSelection(-1, 0);
        if (inputState.consumeUiDown()) moveSelection(1, 0);
        if (inputState.consumeUiLeft()) moveSelection(0, -1);
        if (inputState.consumeUiRight()) moveSelection(0, 1);

        if (inputState.consumeUiSelect()) handleEnterKey();
        if (inputState.consumeUiSplit()) handleSplitKey();
        updateSelectionVisuals();
        updateSlotVisuals();
        updateHeldItemVisual();
    }

    //Lógica para mover a seleção, define selectedRow, selectedCol e currentSection
    private void moveSelection(int dRow, int dCol){
        /*
        Aspeto da grelha, rows = linhas, cols = colunas
        Linhas aumentam para baixo, colunas para a direita
        +-------------------------+
        | Crafting  | Result     |
        | r0 c0 r0 c1 | r0 c0    |
        | r1 c0 r1 c1 |          |
        +-------------------------+
        | Inventory             |
        | r0 c0 ... r0 c8       |
        | r1 c0 ... r1 c8       |
        | r2 c0 ... r2 c8       |
        | r3 c0 ... r3 c8       | hotbar
        +-------------------------+
        * */
        if (currentSection == SECTION_INVENTORY){
            if(dRow < 0 && selectedRow == 0){ // se subir de linha 0 do inventario vai para a secção de crafting
                currentSection = SECTION_CRAFTING;
                selectedRow = 1;
                if (selectedCol > 1) selectedCol = 1;
            }else{
                //Navegação de inventário
                selectedRow += dRow;
                selectedCol += dCol;
                if (selectedRow >= ROWS) selectedRow = 0;
                if (selectedRow < 0) selectedRow = ROWS - 1;
                if (selectedCol >= COLS) selectedCol = 0;
                if (selectedCol < 0) selectedCol = COLS - 1;

            }
        }else if (currentSection == SECTION_CRAFTING){
            int newRow = selectedRow + dRow;
            int newCol = selectedCol + dCol;
            // Se descer da linha 1 do crafting, vai para inventário
            if(newRow > 1){
                currentSection = SECTION_INVENTORY;
                selectedRow = 0;
                return;
            }
            // Se for para a direita na coluna 1, vai para o Resultado
            if (newCol > 1) {
                currentSection = SECTION_RESULT;
                selectedRow = 0;
                selectedCol = 0;
                return;
            }
            if (newRow < 0) newRow = 0; // Topo
            if (newCol < 0) newCol = 0; // Esquerda
            selectedRow = newRow;
            selectedCol = newCol;

        } else if (currentSection == SECTION_RESULT){
            // Se andar para a esquerda, volta para o crafting
            if (dCol < 0) {
                currentSection = SECTION_CRAFTING;
                selectedRow = 0; // Topo direito do crafting
                selectedCol = 1;
            }
            if (dRow > 0) {
                currentSection = SECTION_INVENTORY;
                selectedRow = 0;
                selectedCol = 8; // Canto direito inventário
            }

        }
    }

    //Lógica para o Enter Key
    private void handleEnterKey(){
        Player p = playerState.getPlayer();

        // Lógica para o Slot de Resultado
        if (currentSection == SECTION_RESULT){
            ItemSlot result = p.getCraftingResult();
            if(result != null){
                if(heldItem == null){
                    heldItem = new ItemSlot(result.getItem(), result.getQuantity());
                    p.craftItem();//Consume ingredientes
                    p.addScore(175);
                }else{
                    if (heldItem.getItem().getName().equals(result.getItem().getName())){
                        heldItem.setQuantity(heldItem.getQuantity() + result.getQuantity());
                        p.craftItem();
                    }
                }
            }
            return;
        }

        // Lógica para Inventário e Grid de Crafting
        ItemSlot targetSlot = getSlotAtCurrentSelection();
        if (heldItem == null) {
            if (targetSlot != null && targetSlot.getItem() != null) {
                heldItem = targetSlot;
                setSlotAtCurrentSelection(null);
            }
        } else {
            if (targetSlot == null) {
                setSlotAtCurrentSelection(heldItem);
                heldItem = null;
            } else {
                // Tentar empilhar se for igual
                if (targetSlot.getItem().getName().equals(heldItem.getItem().getName())) {
                    targetSlot.setQuantity(targetSlot.getQuantity() + heldItem.getQuantity());
                    heldItem = null;
                    // Forçar update visual
                    setSlotAtCurrentSelection(targetSlot);
                } else {
                    // Trocar
                    ItemSlot temp = targetSlot;
                    setSlotAtCurrentSelection(heldItem);
                    heldItem = temp;
                }
            }
        }
    }

    //Lógica para dividir o item agarrado
    private void handleSplitKey() {
        if (currentSection == SECTION_RESULT) return;
        if (heldItem == null) return;

        ItemSlot targetSlot = getSlotAtCurrentSelection();
        if (targetSlot == null) {

            ItemSlot singleItem = new ItemSlot(heldItem.getItem(), 1);
            setSlotAtCurrentSelection(singleItem);
            heldItem.setQuantity(heldItem.getQuantity() - 1);

        } else if (targetSlot.getItem().getName().equals(heldItem.getItem().getName())) {
            targetSlot.setQuantity(targetSlot.getQuantity() + 1);
            heldItem.setQuantity(heldItem.getQuantity() - 1);

            setSlotAtCurrentSelection(targetSlot);
        }
        if (heldItem.getQuantity() <= 0) {
            heldItem = null;

        }
        updateHeldItemVisual();
    }

    //Obtém o slot na posição (r,c) do inventário principal, usado em updateSlotVisuals
    private ItemSlot getSlotAt(int r, int c) {
        Player p = playerState.getPlayer();
        if (r == 3){
            return p.getHotbarSlot(c);
        }else{
            int index = (r * COLS) + c;
            return p.getInventorySlot(index);
        }
    }

    //Obtém o slot na posição atual de seleção (Não agarrado)
    private ItemSlot getSlotAtCurrentSelection() {
        Player p = playerState.getPlayer();
        if (currentSection == SECTION_INVENTORY) {
            if (selectedRow == 3) return p.getHotbarSlot(selectedCol);
            else return p.getInventorySlot((selectedRow * COLS) + selectedCol);
        } else if (currentSection == SECTION_CRAFTING) {
            // 2x2 grid: index = row * 2 + col
            return p.getCraftingSlot(selectedRow * 2 + selectedCol);
        }
        return null;
    }

    //Define o slot na posição atual de seleção (Não agarrado)
    private void setSlotAtCurrentSelection(ItemSlot slot) {
        Player p = playerState.getPlayer();
        if (currentSection == SECTION_INVENTORY) {
            if (selectedRow == 3) p.setHotbarSlot(selectedCol, slot);
            else p.setInventorySlot((selectedRow * COLS) + selectedCol, slot);
        } else if (currentSection == SECTION_CRAFTING) {
            p.setCraftingSlot(selectedRow * 2 + selectedCol, slot);
        }

    }

    //Atualiza a posição do cursor de seleção, atraves de selectedRow e selectedCol definidos em moveSelection
    private void updateSelectionVisuals() {
        Picture targetIcon = null;

        if (currentSection == SECTION_INVENTORY) {
            targetIcon = slotIcons[selectedRow][selectedCol];
        } else if (currentSection == SECTION_CRAFTING) {
            int idx = selectedRow * 2 + selectedCol;
            targetIcon = craftingIcons[idx];
        } else if (currentSection == SECTION_RESULT) {
            targetIcon = resultIcon;
        }

        if (targetIcon != null) {
            Vector3f pos = targetIcon.getLocalTranslation();
            float diff = (selectionCursor.getWidth() - targetIcon.getWidth()) / 2;
            selectionCursor.setLocalTranslation(pos.x - diff, pos.y - diff, 2);
        }
    }

    //Update do item agarrado a seguir o cursor
    private void updateHeldItemVisual(){
        if (heldItem != null) {
            heldItemIcon.setCullHint(Node.CullHint.Never);
            //O item agarrado segue o cursor de seleção
            Vector3f cursorWorldPos = selectionCursor.getLocalTranslation();
            //Centrar
            float diff = (selectionCursor.getWidth() - heldItemIcon.getWidth()) / 2;
            heldItemIcon.setLocalTranslation(cursorWorldPos.x + diff, cursorWorldPos.y + diff, 10);

            //Atualizar textura e texto
            heldItemIcon.setImage(assetManager, heldItem.getItem().getIconTexturePath(), true);
            heldItemQty.setText(String.valueOf(heldItem.getQuantity()));
            heldItemQty.setLocalTranslation(cursorWorldPos.x + diff, cursorWorldPos.y + diff, 11);
        } else {
            heldItemIcon.setCullHint(Node.CullHint.Always);
            heldItemQty.setText("");
        }
    }

    //Update das grelhas ao chamar updatesingleslotvisual para cada slot
    private void updateSlotVisuals() {
        Player p = playerState.getPlayer();
        //Atualizar a Grelha do Inventário Principal (ROWS x COLS)
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                ItemSlot slot = getSlotAt(r, c);
                Picture icon = slotIcons[r][c];
                BitmapText text = slotTexts[r][c];


                updateSingleSlotVisual(icon, text, slot);
            }
        }
        //Atualizar a Grelha de Crafting (4 slots)
        for (int i = 0; i < 4; i++) {
            ItemSlot slot = p.getCraftingSlot(i);
            Picture icon = craftingIcons[i];
            BitmapText text = craftingTexts[i];

            updateSingleSlotVisual(icon, text, slot);
        }
        //Atualizar o Slot de Resultado
        ItemSlot resultSlot = p.getCraftingResult();
        updateSingleSlotVisual(resultIcon, resultText, resultSlot);
    }
    //Atualiza a visualização de um slot individual
    private void updateSingleSlotVisual(Picture icon, BitmapText qtyText, ItemSlot slot) {
        if (slot != null && slot.getItem() != null) {
            icon.setImage(assetManager, slot.getItem().getIconTexturePath(), true);

            if (slot.getQuantity() > 1) {
                qtyText.setText(String.valueOf(slot.getQuantity()));
            } else {
                qtyText.setText("");
            }
        } else {
            // Slot vazio: textura vazia e sem texto
            icon.setImage(assetManager, "Interface/Empty_item_craft.png", true);
            qtyText.setText("");
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
