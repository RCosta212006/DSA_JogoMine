package jogo.voxel.blocks;

import jogo.voxel.TextureBlockType;

public class QuickSandBlockType extends TextureBlockType implements  ReducedMoveSpeed{
    public QuickSandBlockType() {
        super("quicksand", "Textures/Quicksand_craft.png");
    }
    // isSolid() inherits true from base
}
