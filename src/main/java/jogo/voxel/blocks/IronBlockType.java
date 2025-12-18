package jogo.voxel.blocks;

import jogo.voxel.TextureBlockType;

public class IronBlockType extends TextureBlockType {

    public IronBlockType() {
        super("ironore", "Textures/IronOre_craft.png");
    }
    // isSolid() inherits true from base

    /*public Item drop() {
        // Return an Item instance representing the dropped iron block
        return new IronIngotItem();
    }*/

    @Override
    public int getRequiredTier() {
        return 2;
    }
}
