package jogo.gameobject.character;

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
}
