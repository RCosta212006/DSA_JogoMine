package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import jogo.voxel.TextureBlockType;
import jogo.voxel.VoxelBlockType;

public class BarrierBlockType extends TextureBlockType implements Unbreakable {
    public BarrierBlockType() {
        super("barrier", "Textures/empty.png");
    }
    // isSolid() inherits true from base

    @Override
    public int getRequiredTier() {
        return 1;
    }
}
