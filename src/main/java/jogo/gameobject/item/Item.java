package jogo.gameobject.item;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import jogo.gameobject.GameObject;
import jogo.gameobject.Visuals;

public abstract class Item extends GameObject {

    protected Item(String name) {
        super(name);
    }

    public void onInteract() {
        // Hook for interaction logic (engine will route interactions)
    }

    public String getIconTexturePath() {
        return "Interface/" + getName() + "_item_craft.png";
    }

    @Override
    public Spatial getSpatial(AssetManager assetManager) {
        Geometry g = new Geometry(this.getName(), new Box(0.3f, 0.3f, 0.3f));
        g.setMaterial(Visuals.colored(assetManager, ColorRGBA.Yellow));
        return g;
    }
}
