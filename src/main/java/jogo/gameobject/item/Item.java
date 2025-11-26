package jogo.gameobject.item;

import jogo.gameobject.GameObject;

public abstract class Item extends GameObject {

    protected Item(String name) {
        super(name);
    }

    public void onInteract() {
        // Hook for interaction logic (engine will route interactions)
    }

    public String getIconTexturePath() {
        return "Interface/" + getName() + "_item_craft.png";
    }
}
