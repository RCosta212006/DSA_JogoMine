package jogo.gameobject.character;

public class Ocelot extends Character {
    // Para não dar shadow à classe Character, os atributos de saúde são acessados via métodos get/set.

    //Para testes
    public Ocelot() {
        super("Ocelot");
        setHealth(30);
        setMaxHealth(30);
    }

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
