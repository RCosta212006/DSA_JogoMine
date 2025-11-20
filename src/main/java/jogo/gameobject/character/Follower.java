package jogo.gameobject.character;

import jogo.framework.math.Vec3;

public class Follower extends NPC {
    private Character target;

    public Follower(String name) {
        super(name);
    }

    public void setTarget(Character target) {
        this.target = target;
    }

    public Character getTarget() {
        return target;
    }

    @Override
    public void update(float tpf) {
        if (target == null) return;

        Vec3 targetPos = target.getPosition();
        Vec3 currentPos = this.getPosition();
        Vec3 direction = targetPos.subtract(currentPos).normalize();

        Vec3 nextPos = new Vec3(
                currentPos.x + direction.x * 0.01f ,
                currentPos.y + direction.y * 0.01f ,
                currentPos.z + direction.z * 0.01f
        );

        this.setPosition(nextPos);

    }

}