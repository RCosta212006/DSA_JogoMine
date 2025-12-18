package jogo.gameobject.character;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import jogo.gameobject.Visuals;
import jogo.gameobject.crafting.CraftingManager;
import jogo.gameobject.item.ItemSlot;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class Player extends Character {
    //Constantes para o tamanho da hotbar e inventário
    public final int MAX_HOTBAR_SLOTS = 9;
    public final int Max_Inventory_Slots = 27;
    public final int CRAFTING_SLOTS = 4;
    public int score = 0;
    List<ItemSlot> Hotbar = new java.util.ArrayList<>(MAX_HOTBAR_SLOTS);
    List<ItemSlot> Inventory = new java.util.ArrayList<>(Max_Inventory_Slots);
    List<ItemSlot> CraftingGrid = new java.util.ArrayList<>(CRAFTING_SLOTS);
    private ItemSlot craftingResult = null;
    private final CraftingManager craftingManager;


    //Suporte para notificação de mudanças de propriedade
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public Player() {
        super("Player");
        //Inicializa o CraftingManager
        this.craftingManager = new CraftingManager();
        //Inicializa a hotbar com entradas null para evitar IndexOutOfBounds
        for (int i = 0; i < MAX_HOTBAR_SLOTS; i++) Hotbar.add(null);
        for (int i = 0; i < Max_Inventory_Slots; i++) Inventory.add(null);
        for (int i = 0; i < CRAFTING_SLOTS; i++) CraftingGrid.add(null);
    }

    public List<ItemSlot> getHotbar() {
        return Hotbar;
    }

    public List<ItemSlot> getInventory() {
        return Inventory;
    }

    public int getScore() {
        return score;
    }

    //Adiciona pontos ao score do jogador e notifica listeners
    public void addScore(int amount) {
        int oldscore = this.score;
        this.score += amount;

        pcs.firePropertyChange("score", oldscore, this.score);
    }

    //Obtém o item em um slot específico da hotbar
    public ItemSlot getHotbarSlot(int index) {
        if (index >= 0 && index < MAX_HOTBAR_SLOTS) {
            return Hotbar.get(index);
        }
        return null;
    }

    //Verifica se a hotbar está cheia
    public Boolean Hotbarisfull() {
        int slotFull = 0;
        for (int i = 0; i < MAX_HOTBAR_SLOTS; i++) {
            if (Hotbar.get(i) != null) {
                slotFull += 1;
            }
        }
        if (slotFull == 9) {
            return true;
        } else {
            return false;
        }
    }

    //Adiciona item à hotbar, se estiver cheia adiciona ao inventário, se existir faz stack
    public void addToHotbar(ItemSlot itemSlot) {
        System.out.println(Hotbar.size());
        if (itemSlot == null) return;
        for (int i = 0; i < MAX_HOTBAR_SLOTS; i++) {
            if (Hotbar.get(i) == null) {
                Hotbar.set(i, itemSlot);
                pcs.firePropertyChange("hotbar", null, Hotbar);
                System.out.println("Added " + itemSlot.getItem().getName() + " x" + itemSlot.getQuantity() + " to hotbar slot " + i);
                addScore(100);
                return;
            } else {
                ItemSlot existingSlot = Hotbar.get(i);
                if (existingSlot.getItem().getName().equals(itemSlot.getItem().getName())) {
                    existingSlot.setQuantity(existingSlot.getQuantity() + itemSlot.getQuantity());
                    pcs.firePropertyChange("hotbar", null, Hotbar);
                    System.out.println("Stacked " + existingSlot.getItem().getName() + " x" + existingSlot.getQuantity() + " to hotbar slot " + i);
                    addScore(100);
                    return;
                }
            }
        }
        if (Hotbarisfull()) {
            addToInventory(itemSlot);
        }
    }

    //Coloca item num slot específico da hotbar
    public void setHotbarSlot(int index, ItemSlot slot) {
        if (index >= 0 && index < MAX_HOTBAR_SLOTS) {
            Hotbar.set(index, slot);
            // Avisa a HUD para atualizar
            pcs.firePropertyChange("hotbar", null, Hotbar);
        }
    }





    public ItemSlot getInventorySlot(int index) {
        if (index >= 0 && index < Max_Inventory_Slots) {
            return Inventory.get(index);
        }
        return null;
    }

    //coloca item num slot específico do inventário
    public void setInventorySlot(int index, ItemSlot slot) {
        if (index >= 0 && index < Max_Inventory_Slots) {
            Inventory.set(index, slot);
            pcs.firePropertyChange("inventory", null, Inventory);
            addScore(100);
        }
    }

    //adiciona item ao inventário, se já existir faz stack
    public void addToInventory(ItemSlot slot) {
        for (int i = 0; i < Max_Inventory_Slots; i++) {
            if (getInventorySlot(i) == null) {
                Inventory.set(i, slot);
                pcs.firePropertyChange("Inventory", null, Inventory);
                addScore(100);
                return;
            } else if (getInventorySlot(i).getItem().getName().equals(slot.getItem().getName())) {
                getInventorySlot(i).setQuantity(getInventorySlot(i).getQuantity() + slot.getQuantity());
                pcs.firePropertyChange("Inventory", null, Inventory);
                addScore(100);
                return;
            }
        }
    }



    public ItemSlot getCraftingSlot(int index) {
        if (index >= 0 && index < CRAFTING_SLOTS) return CraftingGrid.get(index);
        return null;
    }

    public ItemSlot getCraftingResult() {
        return craftingResult;
    }


    //coloca item num slot específico na grid de crafting
    public void setCraftingSlot(int index, ItemSlot slot) {
        if (index >= 0 && index < CRAFTING_SLOTS) {
            CraftingGrid.set(index, slot);
            pcs.firePropertyChange("crafting", null, CraftingGrid);
            updateCraftingResult();
        }
    }

    //atualiza o resultado do crafting baseado na grid atual
    private void updateCraftingResult() {
        ItemSlot result = this.craftingManager.checkRecipe(CraftingGrid);
        this.craftingResult = result;
        pcs.firePropertyChange("craftingResult", null, craftingResult);
    }

    //realiza o crafting do item, consumindo os ingredientes
    public void craftItem() {
        if (craftingResult == null) return;
        //Consumo de items ao criar
        for (int i = 0; i < CRAFTING_SLOTS; i++) {
            ItemSlot slot = CraftingGrid.get(i);
            if (slot != null) {
                int newQty = slot.getQuantity() - 1;
                if (newQty <= 0) {
                    CraftingGrid.set(i, null);
                } else {
                    slot.setQuantity(newQty);
                }
            }
        }
        // Atualizar a UI e verificar se ainda há itens para outra receita igual
        pcs.firePropertyChange("craftingGrid", null, CraftingGrid);
        updateCraftingResult();
    }

    //consome uma quantidade específica de um item na hotbar
    public void consumeItem(int slotIndex, int amount) {
        if (slotIndex < 0 || slotIndex >= MAX_HOTBAR_SLOTS) return;

        ItemSlot slot = Hotbar.get(slotIndex);
        if (slot != null) {
            int newQuantity = slot.getQuantity() - amount;
            if (newQuantity <= 0) {
                Hotbar.set(slotIndex, null); // Remove o item se acabar
            } else {
                slot.setQuantity(newQuantity);
            }

            pcs.firePropertyChange("hotbar", null, Hotbar);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        return "Player{" +
                "MAX_HOTBAR_SLOTS=" + MAX_HOTBAR_SLOTS +
                ", Hotbar=" + Hotbar.size() +
                '}';
    }

    @Override
    public Spatial getSpatial(AssetManager assetManager) {
        Geometry g = new Geometry(this.getName(), new Cylinder(16, 16, 0.35f, 1.4f, true));
        g.setMaterial(Visuals.colored(assetManager, ColorRGBA.Green));
        return g;
    }
}
