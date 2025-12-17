package jogo.gameobject.character;

public abstract class NPC extends Character {

    public NPC(String name) {
        super(name);
    }

    public abstract void update(float tpf);

    public void onInteract(Player player) {
        //Padrão: nada
    }

}
