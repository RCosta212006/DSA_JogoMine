package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.util.ProcTextures;
import jogo.voxel.TextureBlockType;
import jogo.voxel.VoxelBlockType;

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
