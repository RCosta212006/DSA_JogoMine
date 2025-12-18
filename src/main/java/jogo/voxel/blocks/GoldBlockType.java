package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import jogo.voxel.TextureBlockType;
import jogo.voxel.VoxelBlockType;

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
