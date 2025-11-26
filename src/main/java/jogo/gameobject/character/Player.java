package jogo.gameobject.character;

import jogo.gameobject.item.ItemSlot;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class Player extends Character {
    public final int MAX_HOTBAR_SLOTS = 9;
    List<ItemSlot> Hotbar = new java.util.ArrayList<>(MAX_HOTBAR_SLOTS);
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public Player() {
        super("Player");
        // Inicializa a hotbar com entradas null para evitar IndexOutOfBounds
        for (int i = 0; i < MAX_HOTBAR_SLOTS; i++) Hotbar.add(null);
    }

    public List<ItemSlot> getHotbar() {
        return Hotbar;
    }


    public void addToHotbar(ItemSlot itemSlot) {
        System.out.println(Hotbar.size());
        if (itemSlot == null) return;
        for (int i = 0; i < MAX_HOTBAR_SLOTS; i++) {
            if (Hotbar.get(i) == null) {
                Hotbar.set(i, itemSlot);
                pcs.firePropertyChange("hotbar", null, Hotbar);
                System.out.println("Added " + itemSlot.getItem().getName() + " x" + itemSlot.getQuantity() + " to hotbar slot " + i);
                return;
            } else {
                ItemSlot existingSlot = Hotbar.get(i);
                if (existingSlot.getItem().getName().equals(itemSlot.getItem().getName())) {
                    itemSlot.setQuantity(existingSlot.getQuantity() + itemSlot.getQuantity());
                    pcs.firePropertyChange("hotbar", null, Hotbar);
                    System.out.println("Stacked " + itemSlot.getItem().getName() + " x" + itemSlot.getQuantity() + " to hotbar slot " + i);
                    return;
                }
            }
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
}
