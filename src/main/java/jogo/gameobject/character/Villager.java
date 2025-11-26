package jogo.gameobject.character;

public class Villager extends Follower {
    public Villager(String name) {
        super(name);
        setHealth(50);
        setMaxHealth(50);
    }

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
}
