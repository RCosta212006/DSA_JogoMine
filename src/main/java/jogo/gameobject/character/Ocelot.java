package jogo.gameobject.character;

import com.jme3.scene.Spatial;
import jogo.ai.FollowControl;

public class Ocelot extends Follower {

    // Construtor com nome personalizado
    public Ocelot(String name) {
        super(name);
        setHealth(30);
        setMaxHealth(30);
    }

    @Override
    public String toString() {
        return getName() + "{Health=" + getHealth() + ", maxHealth=" + getMaxHealth() + "}";
    }

}
