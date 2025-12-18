package jogo.gameobject.character;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class Ocelot extends Follower implements AbleToTeleport{
    public Ocelot(String name) {
        super(name);
        setHealth(30);
        setMaxHealth(30);
    }

    @Override
    public String toString() {
        return getName() + "{Health=" + getHealth() + ", maxHealth=" + getMaxHealth() + "}";
    }

    @Override
    public Spatial getSpatial(AssetManager assetManager) {
        Spatial model = assetManager.loadModel("Models/ocelot.j3o");
        model.setName(this.getName());
        model.setLocalScale(0.5f); //Ajuste de escala se necessário
        return model;
    }
}
