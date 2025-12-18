package jogo.voxel.blocks;

import jogo.voxel.TextureBlockType;

public class StoneBlockType extends TextureBlockType {
    public StoneBlockType() {
        super("stone", "Textures/stone_craft.png");
    }
    // isSolid() inherits true from base

    @Override
    public int getRequiredTier() {
        return 1;
    }
}
