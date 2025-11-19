package jogo.gameobject.character;

import com.jme3.scene.Spatial;
import jogo.ai.FollowControl;

public class Ocelot extends Character {
    private FollowControl followControl;
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

    /**
     * Adiciona um FollowControl ao spatial do ocelot para seguir o target.
     * - ocelotSpatial: o Spatial que representa o ocelot na cena
     * - target: Spatial do jogador
     * - speed: velocidade em unidades/segundo
     * - stopDistance: distância mínima para parar de se aproximar
     */

    public void enableFollow(Spatial ocelotSpatial, Spatial target, float speed, float stopDistance) {
        if (ocelotSpatial == null || target == null) return; //Valida ocelotSpatial e target
        //Atualiza target, speed e stopDistance (reusa o control).
        if (followControl != null) {
            followControl.setTarget(target);
            followControl.setSpeed(speed);
            followControl.setStopDistance(stopDistance);
            return;
        }
        //Se não existir, cria novo FollowControl e adiciona ao spatial
        followControl = new FollowControl(target, speed, stopDistance);
        ocelotSpatial.addControl(followControl);
    }

    public void disableFollow(Spatial ocelotSpatial) {
        if (ocelotSpatial == null || followControl == null) return;
        ocelotSpatial.removeControl(followControl);
        followControl = null;
    }
}
