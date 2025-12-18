package jogo.voxel.blocks;

import jogo.voxel.TextureBlockType;

public class GoldBlockType extends TextureBlockType {

    public GoldBlockType() {
        super("goldore", "Textures/GoldOre_craft.png");
    }
    // isSolid() inherits true from base

    @Override
    public int getRequiredTier() {
        return 3;
    }
}
