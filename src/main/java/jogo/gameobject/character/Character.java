package jogo.gameobject.character;

import jogo.gameobject.GameObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class Character extends GameObject {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected Character(String name) {
        super(name);
    }

    // Example state hooks students can extend
    private int health = 80; // vida atual do personagem
    private int maxHealth = 100; // limite superior da vida (cap);

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public int getHealthPercent() {
        return (int) Math.round(100.0 * health / Math.max(1, maxHealth));
    }

    public void setHealth(int newHealth) {
        int old = this.health;
        this.health = Math.max(0, Math.min(maxHealth, newHealth));
        if (old != this.health) pcs.firePropertyChange("health", old, this.health);
    }

    public void setMaxHealth(int newMax) {
        int old = this.maxHealth;
        this.maxHealth = Math.max(1, newMax);
        if (this.health > this.maxHealth) {
            setHealth(this.maxHealth);
        }
        if (old != this.maxHealth) pcs.firePropertyChange("maxHealth", old, this.maxHealth);
    }

    public void damage(int amount) {
        if (amount <= 0) return;
        setHealth(this.health - amount);
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        setHealth(this.health + amount);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }




}

