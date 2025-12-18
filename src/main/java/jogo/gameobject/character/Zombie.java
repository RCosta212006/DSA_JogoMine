package jogo.gameobject.character;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class Zombie extends Enemy{
    public Zombie(String name) {
        super(name, 20, 3.0f, 2.0f);
        setHealth(50);
        setMaxHealth(50);
    }

    @Override
    public String toString() {
        return getName() + "{Health=" + getHealth() + ", maxHealth=" + getMaxHealth() + "}";
    }

    @Override
    public Spatial getSpatial(AssetManager assetManager) {
        Spatial model = assetManager.loadModel("Models/zombie.j3o");
        model.setName(this.getName());
        model.setLocalScale(1f); //Ajuste de escala se necessário
        return model;
    }
}

