package jogo.voxel.blocks;

import jogo.voxel.TextureBlockType;

public class DiamondBlockType extends TextureBlockType {

    public DiamondBlockType() {
        super("diamond", "Textures/DiamondOre_craft.png");
    }
    // isSolid() inherits true from base

    @Override
    public int getRequiredTier() {
        return 3;
    }

}

