package jogo.gameobject.character;

import jogo.gameobject.GameObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class Character extends GameObject {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected Character(String name) {
        super(name);
    }

    //Vida do personagem
    private int health = 100; // vida atual do personagem
    private int maxHealth = 100; // limite superior da vida (cap);

    //Getters e Setters
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public void setHealth(int newHealth) {
        //Garante que a vida esteja entre 0 e maxHealth
        //Calcula o menor entre maxHealth e newHealth, depois o maior entre 0 e esse resultado; o resultado final é atribuído a this.health
        int old = this.health;
        this.health = Math.max(0, Math.min(maxHealth, newHealth));
        if (old != this.health) pcs.firePropertyChange("health", old, this.health);
    }

    public void setMaxHealth(int newMax) {
        //Garante que o maxHealth seja no mínimo 1
        int old = this.maxHealth;
        this.maxHealth = Math.max(1, newMax);
        if (this.health > this.maxHealth) {
            setHealth(this.maxHealth);
        }
        if (old != this.maxHealth) pcs.firePropertyChange("maxHealth", old, this.maxHealth);
    }

    //Lógica de dano
    public void damage(int amount) {
        if (amount <= 0) return;
        setHealth(this.health - amount);
    }

    //Listeners
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

}

