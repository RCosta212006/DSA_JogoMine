package jogo.gameobject.character;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class Villager extends Follower {
    public Villager(String name) {
        super(name);
        setHealth(50);
        setMaxHealth(50);
    }

    //Cura o jogador para 100 de vida ao interagir
    @Override
    public void onInteract(Player player) {
        if (player == null) return;
        player.setHealth(100);
        System.out.println(getName() + " curou " + player.getName() + " para 100 HP");
    }

    @Override
    public String toString() {
        return getName() + "{Health=" + getHealth() + ", maxHealth=" + getMaxHealth() + "}";
    }

    @Override
    public Spatial getSpatial(AssetManager assetManager) {
        Spatial model = assetManager.loadModel("Models/villager.j3o");
        model.setName(this.getName());
        model.setLocalScale(0.09f); //Ajuste de escala se necessário
        return model;
    }
}
