package jogo.voxel;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

//
public abstract class TextureBlockType extends VoxelBlockType {
    private String textureName;

    protected TextureBlockType(String name, String textureName) {
        super(name);
        this.textureName = textureName;
    }

    public String getTextureName() {
        return textureName;
    }

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture tex = assetManager.loadTexture(getTextureName());
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

        Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture("DiffuseMap", tex);
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", ColorRGBA.White);
        m.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        m.setFloat("Shininess", 32f); // tighter, less intense highlight
        return m;
    }
}
