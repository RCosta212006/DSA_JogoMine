package jogo.gameobject.character;


import com.jme3.math.Vector3f;
import jogo.framework.math.Vec3;

public abstract class NPC extends Character {

    public NPC(String name) {
        super(name);
    }

    public abstract void update(float tpf);

    public void onInteract(Player player) {
        // padrão: nada
    }

}
