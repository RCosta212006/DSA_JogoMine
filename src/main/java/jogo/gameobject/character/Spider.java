package jogo.gameobject.character;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import jogo.gameobject.Visuals;

public class Spider extends Enemy{
    public Spider(String name) {
        super(name, 10, 2.0f, 1.0f);
        setHealth(50);
        setMaxHealth(50);
    }

    @Override
    public String toString() {
        return getName() + "{Health=" + getHealth() + ", maxHealth=" + getMaxHealth() + "}";
    }

    @Override
    public Spatial getSpatial(AssetManager assetManager) {
        Spatial model = assetManager.loadModel("Models/spider.j3o");
        model.setName(this.getName());
        model.setLocalScale(0.09f); //Ajuste de escala se necessário
        return model;
    }
}
