package jogo.voxel.blocks;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import jogo.voxel.VoxelBlockType;

public class BarrierBlockType extends VoxelBlockType implements Umbreakable {
    public BarrierBlockType() {
        super("barrier");
    }
    // isSolid() inherits true from base


    @Override
    public int getRequiredTier() {
        return 1;
    }

    @Override
    public Material getMaterial(AssetManager assetManager) {
        Texture tex = assetManager.loadTexture("Textures/empty.png");
        Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture("DiffuseMap", tex);
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", ColorRGBA.White);
        m.setColor("Specular", ColorRGBA.White.mult(0.02f)); // reduced specular
        m.setFloat("Shininess", 32f); // tighter, less intense highlight
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        return m;
    }
}
