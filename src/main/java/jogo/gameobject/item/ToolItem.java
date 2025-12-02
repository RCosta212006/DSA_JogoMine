package jogo.gameobject.item;

public class ToolItem extends Item{
    public ToolItem (String name){
        super(name);
    }


    public String getIconHeldTexturePath() {
        return "Interface/" + getName() + "_held_item_craft.png" ;
    }
}
