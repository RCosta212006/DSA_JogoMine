package jogo.gameobject.item;

public class ToolItem extends Item{
    private final int tier;

    public ToolItem (String name,int tier){
        super(name);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }


    public String getIconHeldTexturePath() {
        return "Interface/" + getName() + "_held_item_craft.png" ;
    }
}
