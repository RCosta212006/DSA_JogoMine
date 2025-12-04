package jogo.gameobject.character;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import jogo.framework.math.Vec3;

public class Follower extends NPC {
    private Character target;

    private Node npcNode;
    private BetterCharacterControl characterControl;
    private float moveSpeed = 1.0f;
    private float stopThreshold = 0.05f;
    private float teleportTimer = 0f;
    private final float maxTeleportWait = 2.0f; // segundos antes do teletransporte
    private final float maxJumpHeight = 1.1f;   // altura máxima que NPC consegue saltar (~1 bloco)
    private final float minJumpNeeded = 0.5f;
    private final float followRange = 9f;
    private final float followRangeSq = followRange * followRange;

    public Follower(String name) {
        super(name);
    }

    public void setTarget(Character target) {
        this.target = target;
    }

    public Character getTarget() {
        return target;
    }

    public void attachToScene(Node rootNode, PhysicsSpace physicsSpace) {
        if (npcNode != null) return;

        npcNode = new Node("Follower-" + getName());
        characterControl = new BetterCharacterControl(0.42f, 1.8f, 80f);
        characterControl.setGravity(new Vector3f(0, -24f, 0));
        characterControl.setJumpForce(new Vector3f(0, 400f, 0));

        npcNode.addControl(characterControl);
        rootNode.attachChild(npcNode);
        physicsSpace.add(characterControl);

        // posiciona o control na posição lógica atual do modelo
        Vec3 p = this.getPosition();
        characterControl.warp(new Vector3f(p.x, p.y, p.z));
    }

    /**
     * Remove o follower da cena e da PhysicsSpace.
     */
    public void detachFromScene(Node rootNode, PhysicsSpace physicsSpace) {
        if (npcNode == null) return;
        if (characterControl != null) {
            physicsSpace.remove(characterControl);
            npcNode.removeControl(characterControl);
            characterControl = null;
        }
        npcNode.removeFromParent();
        npcNode = null;
    }

    public void warpToModelPosition() {
        if (characterControl != null) {
            Vec3 p = this.getPosition();
            characterControl.warp(new Vector3f(p.x, p.y, p.z));
        }
    }

    @Override
    public void update(float tpf) {
        if (target == null) return;
        if (characterControl != null && npcNode != null) {
            //Calcula posições
            Vec3 targetPos = target.getPosition();
            Vec3 currentPos = new Vec3(npcNode.getWorldTranslation().x, npcNode.getWorldTranslation().y, npcNode.getWorldTranslation().z);

            //Verifica diferença de altura para decidir salto ou teletransporte
            float diferencaAltura = (float) (targetPos.y - currentPos.y);

            float dx = (float) (targetPos.x - currentPos.x);
            float dz = (float) (targetPos.z - currentPos.z);
            float distSqXZ = dx * dx + dz * dz;

            // Se estiver fora do alcance de follow, não segue nem teletransporta
            if (distSqXZ > followRangeSq) {
                teleportTimer = 0f;
                characterControl.setWalkDirection(Vector3f.ZERO);
                // sincroniza posição lógica com a spatial para evitar drift
                Vector3f worldPosOut = npcNode.getWorldTranslation();
                this.setPosition(new Vec3(worldPosOut.x, worldPosOut.y, worldPosOut.z));
                return;
            }

            //Se precisa saltar até uma altura que o NPC consegue (até ~1 bloco)
            if (diferencaAltura > minJumpNeeded) {
                if (diferencaAltura <= maxJumpHeight && characterControl.isOnGround()) {
                    //Salta o bloco
                    characterControl.jump();
                    //Não teleporta, reseta o timer
                    teleportTimer = 0f;
                } else if (diferencaAltura > maxJumpHeight) {
                    // Não consegue saltar essa altura: conta tempo e teleporta se exceder
                    teleportTimer += tpf;
                    // impede movimento enquanto conta
                    characterControl.setWalkDirection(Vector3f.ZERO);

                    if (teleportTimer >= maxTeleportWait) {
                        //Só teletransporta se a instância for AbleToTeleport
                        if (this instanceof AbleToTeleport) {
                            // Teletransporta perto do jogador (pequeno offset)
                            Vector3f localTeletransporte = new Vector3f((float) targetPos.x + 1f, (float) targetPos.y + 0.1f, (float) targetPos.z + 1f);
                            characterControl.warp(localTeletransporte);
                            npcNode.setLocalTranslation(localTeletransporte);
                            this.setPosition(new Vec3(localTeletransporte.x, localTeletransporte.y, localTeletransporte.z));
                        }
                        //Reinicia o timer seja teletransportado ou não para evitar loops
                        teleportTimer = 0f;
                    }
                    // interrompe o update de movimento
                    return;
                }
            } else {
                // sem diferença de altura que exija salto -> reseta contador
                teleportTimer = 0f;
            }

            // Caminhar em XZ em direção ao target
            Vec3 dir = targetPos.subtract(currentPos);
            Vector3f walk;
            if (Math.abs(dir.x) < 1f && Math.abs(dir.z) < 1f) {
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
        }
    }
}