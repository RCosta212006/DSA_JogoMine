package jogo.gameobject.character;

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
}

