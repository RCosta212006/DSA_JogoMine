package jogo.gameobject.character;

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

}
