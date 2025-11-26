package jogo.gameobject.character;

import jogo.gameobject.item.ItemSlot;

import java.util.List;

public class Player extends Character {
    public final int MAX_HOTBAR_SLOTS = 9;
    List<ItemSlot> Hotbar = new java.util.ArrayList<>(MAX_HOTBAR_SLOTS);

    public Player() {
        super("Player");
    }

    public List<ItemSlot> getHotbar() {
        return Hotbar;
    }


    public void addToHotbar(ItemSlot itemSlot) {
        for (int i = 0; i < MAX_HOTBAR_SLOTS; i++) {
            if (getHotbar().get(i) == null || getHotbar().get(i).getItem().equals(itemSlot.getItem())) {
                getHotbar().set(i, itemSlot);
                getHotbar().get(i).setQuantity(itemSlot.getQuantity() + 1);
                System.out.println("Item adicionado ao hotbar na posicao " + i +"e quantidade " + itemSlot.getQuantity());
                return;
            }else{
                if(i == MAX_HOTBAR_SLOTS -1){
                    System.out.println("Hotbar cheia!");
                }
            }
        }
    }
}
