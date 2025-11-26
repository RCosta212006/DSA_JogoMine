package jogo.gameobject.item;

import jogo.voxel.VoxelPalette;

public class BlockItem extends Item{
    private final byte blockID;

    public BlockItem(String name, byte blockID) {
        super(name);
        this.blockID = blockID;
    }


    public byte getBlockID() {
        return blockID;
    }

    public String IDtoName(VoxelPalette palette) {
        return palette.get(blockID).getName();
    }
}
