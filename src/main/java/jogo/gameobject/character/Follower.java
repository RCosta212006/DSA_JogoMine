package jogo.gameobject.character;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import jogo.framework.math.Vec3;

public class Follower extends NPC {
    private Character target;

    private Node npcNode;
    private BetterCharacterControl characterControl;
    private float moveSpeed = 2.0f;

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

        if (characterControl != null && npcNode != null) {
            //Compute horizontal direction toward target
            Vec3 targetPos = target.getPosition();
            Vec3 currentPos = new Vec3(npcNode.getWorldTranslation().x, npcNode.getWorldTranslation().y, npcNode.getWorldTranslation().z);
            Vec3 dir = targetPos.subtract(currentPos);
            //Ignorar componente Y para caminhar no plano XZ
            Vector3f walk;
            if (Math.abs(dir.x) < 1e-6 && Math.abs(dir.z) < 1e-6) {
                walk = Vector3f.ZERO;
            } else {
                float len = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                float nx = dir.x / len;
                float nz = dir.z / len;
                walk = new Vector3f(nx * moveSpeed, 0f, nz * moveSpeed);
            }
            characterControl.setWalkDirection(walk);

            //Sincroniza a posição do modelo com a física / spatial
            Vector3f worldPos = npcNode.getWorldTranslation();
            this.setPosition(new Vec3(worldPos.x, worldPos.y, worldPos.z));
        } else {
            // fallback: comportamento antigo (sem física)
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

}