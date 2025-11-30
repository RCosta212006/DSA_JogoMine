package jogo.gameobject.crafting;

import jogo.gameobject.item.Item;
import jogo.gameobject.item.ItemSlot;

public class CraftingRecipe {
    //Array de quatro nomes de itens 2x2 (0=topleft,1=topright,2=bottomleft,4=bottomright)
    private final String[] pattern;
    private final Item resultItem;
    private final int resultQuantity;

    public CraftingRecipe(String[] pattern, Item resultItem, int resultQuantity){
        if(pattern.length != 4) throw new IllegalArgumentException("Pattern must be 4");
        this.pattern = pattern;
        this.resultItem = resultItem;
        this.resultQuantity = resultQuantity;
    }

    public boolean matches(ItemSlot[] grid){
        //Compara padrão da grid de craft com receita e ve se são iguais
        for (int i =0; i < 4;i++){
            String recipeName = pattern[i];
            ItemSlot slot = grid[i];
            if(recipeName == null){
                if(slot != null && slot.getItem() != null)return false;
            }else {
                if (slot == null || slot.getItem()== null) return false;
                if (!slot.getItem().getName().equals(recipeName)) return false;
            }
        }
        return true;
    }

    public ItemSlot getResult(){
        return new ItemSlot(resultItem,resultQuantity);
    }
}
