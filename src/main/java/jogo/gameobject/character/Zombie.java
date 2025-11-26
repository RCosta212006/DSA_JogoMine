package jogo.gameobject.character;

public class Zombie extends Follower{
    public Zombie(String name) {
        super(name);
        setHealth(50);
        setMaxHealth(50);
    }

    @Override
    public String toString() {
        return getName() + "{Health=" + getHealth() + ", maxHealth=" + getMaxHealth() + "}";
    }
}

