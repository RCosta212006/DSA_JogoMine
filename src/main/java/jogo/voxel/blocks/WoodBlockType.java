package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import jogo.voxel.TextureBlockType;
import jogo.voxel.VoxelBlockType;

public class WoodBlockType extends TextureBlockType {
    public WoodBlockType() {
        super("wood", "Textures/Wood_craft.png");
    }
    // isSolid() inherits true from base
}
