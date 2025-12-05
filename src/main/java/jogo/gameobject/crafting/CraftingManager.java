package jogo.gameobject.crafting;

import jogo.gameobject.item.*;
import jogo.voxel.VoxelPalette;

import java.util.ArrayList;
import java.util.List;

public class CraftingManager {
    private static final List<CraftingRecipe> recipes = new ArrayList<>();

    //Static corre uma vez quando o jogo arranca,Aqui são criadas todas as rcietas e adicionadas a recipes
    static {
        CraftItem stick = new CraftItem("stick");
        BlockItem plank = new BlockItem("woodplank",VoxelPalette.WOODPLANK_ID);
        ToolItem stonePickaxe = new ToolItem("stonepickaxe",2);
        ToolItem diamondPickaxe = new ToolItem("diamondpickaxe",4);
        ToolItem woodPickaxe = new ToolItem("woodpickaxe",1);
        ToolItem ironPickaxe = new ToolItem("ironpickaxe",3);

        //Receitas Stick
        recipes.add(new CraftingRecipe(
                new String[]{"wood", null, "wood", null},//padrão de itens/receita
                stick, //item final
                4 // Quantidade produzida
        ));
        recipes.add(new CraftingRecipe(
                new String[]{null, "wood", null, "wood"},//padrão de itens/receita
                stick, //item final
                4 // Quantidade produzida
        ));

        //Receitas plank
        recipes.add(new CraftingRecipe(
                new String[]{null, null, "wood", null},//padrão de itens/receita
                plank, //item final
                6 // Quantidade produzida
        ));

        //Receitas pickaxe
        recipes.add(new CraftingRecipe(
                new String[]{"wood", "wood", "stick", null},//padrão de itens/receita
                woodPickaxe, //item final
                1 // Quantidade produzida
        ));
        recipes.add(new CraftingRecipe(
                new String[]{"stone", "stone", "stick", null},//padrão de itens/receita
                stonePickaxe, //item final
                1 // Quantidade produzida
        ));
        recipes.add(new CraftingRecipe(
                new String[]{"ironore", "ironore", "stick", null},//padrão de itens/receita
                ironPickaxe, //item final
                1 // Quantidade produzida
        ));
        recipes.add(new CraftingRecipe(
                new String[]{"diamond", "diamond", "stick", null},//padrão de itens/receita
                diamondPickaxe, //item final
                1 // Quantidade produzida
        ));

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
