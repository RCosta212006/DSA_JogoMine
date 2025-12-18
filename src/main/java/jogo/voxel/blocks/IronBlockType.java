package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import jogo.gameobject.item.Item;
import jogo.voxel.TextureBlockType;
import jogo.voxel.VoxelBlockType;

public class IronBlockType extends TextureBlockType {

    public IronBlockType() {
        super("ironore", "Textures/IronOre_craft.png");
    }
    // isSolid() inherits true from base

    /*public Item drop() {
        // Return an Item instance representing the dropped iron block
        return new IronIngotItem();
    }*/

    @Override
    public int getRequiredTier() {
        return 2;
    }
}
