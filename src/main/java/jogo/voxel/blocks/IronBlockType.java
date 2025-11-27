package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import jogo.gameobject.item.Item;
import jogo.voxel.VoxelBlockType;

public class IronBlockType extends VoxelBlockType {

    public IronBlockType() {
        super("ironore");
    }
    // isSolid() inherits true from base

    /*public Item drop() {
        // Return an Item instance representing the dropped iron block
        return new IronIngotItem();
    }*/

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture tex = assetManager.loadTexture("Textures/IronOre_craft.png");
        Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture("DiffuseMap", tex);
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", ColorRGBA.White);
        m.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        m.setFloat("Shininess", 32f); // tighter, less intense highlight
        return m;
    }
}
