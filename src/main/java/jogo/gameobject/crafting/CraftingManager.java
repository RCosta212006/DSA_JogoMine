package jogo.gameobject.crafting;

import jogo.gameobject.item.BlockItem;
import jogo.gameobject.item.CraftItem;
import jogo.gameobject.item.Item;
import jogo.gameobject.item.ItemSlot;
import jogo.voxel.VoxelPalette;

import java.util.ArrayList;
import java.util.List;

public class CraftingManager {
    private static final List<CraftingRecipe> recipes = new ArrayList<>();

    //Static corre uma vez quando o jogo arranca,Aqui são criadas todas as rcietas e adicionadas a recipes
    static {
        BlockItem woodblock = new BlockItem("wood", VoxelPalette.WOODBLOCK_ID);
        CraftItem stick = new CraftItem("stick");
        //TODO adicionar mais items
        recipes.add(new CraftingRecipe(
                new String[]{"wood", null, "wood", null},//padrão de itens/receita
                stick, //item final
                4 // Quantidade produzida
        ));
        //TODO adiconar mais recipes
    }

    public static ItemSlot checkRecipe(List<ItemSlot> grid){
        //converte List para Array para validar a receita
        ItemSlot[] gridArr = grid.toArray(new ItemSlot[0]);
        for (CraftingRecipe recipe : recipes ){
            if (recipe.matches(gridArr)){
                return recipe.getResult();
            }
        }
        return null;
    }



}
