package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import jogo.util.ProcTextures;
import jogo.voxel.TextureBlockType;
import jogo.voxel.VoxelBlockType;


public class DirtBlockType extends TextureBlockType {

    public DirtBlockType() {
        super("dirt", "Textures/Dirt_craft.png");
    }
    // isSolid() inherits true from base

}
