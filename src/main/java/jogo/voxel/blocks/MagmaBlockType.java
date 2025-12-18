package jogo.voxel.blocks;

import jogo.voxel.TextureBlockType;

public class MagmaBlockType extends TextureBlockType {
    public MagmaBlockType() { super("magma", "Textures/magma_craft.png");
    }
    // isSolid() inherits true from base


    @Override
    public int getRequiredTier() {
        return 1;
    }
}
