package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import jogo.voxel.VoxelBlockType;

public class AirBlockType extends VoxelBlockType {
    public AirBlockType() {
        super("air");
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public Material getMaterial(AssetManager assetManager) {
        // Air is invisible, so return null (or could return a transparent material if needed)
        return null;
    }
}
