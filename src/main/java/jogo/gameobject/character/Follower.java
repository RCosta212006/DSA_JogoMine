package jogo.gameobject.character;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import jogo.framework.math.Vec3;

public class Follower extends NPC {
    private Character target;

    private Node npcNode;
    private BetterCharacterControl characterControl;
    private float moveSpeed = 1.5f;
    private float teleportTimer = 0f;
    private final float maxTeleportWait = 2.0f; //Segundos antes do teletransporte
    private final float maxJumpHeight = 1.1f; //Altura máxima que NPC consegue saltar (~1 bloco)
    private final float minJumpNeeded = 0.5f; //Altura mínima que exige salto
    private final float followRange = 50f;
    private final float followRangeSq = followRange * followRange;
    //Altura e raio do personagem (usadas para criar o control e calcular offset ao fazer warp)
    private static final float CHARACTER_HEIGHT = 1.8f;
    private static final float CHARACTER_RADIUS = 0.42f;

    // Referências guardadas para permitir remoção posterior
    private Node rootNodeRef;
    private PhysicsSpace physicsSpaceRef;

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

        //Guarda referências para permitir remover depois sem precisar dos parâmetros
        this.rootNodeRef = rootNode;
        this.physicsSpaceRef = physicsSpace;

        //Cria o node visual e o control físico
        npcNode = new Node("Follower-" + getName());
        characterControl = new BetterCharacterControl(CHARACTER_RADIUS, CHARACTER_HEIGHT, 80f);
        characterControl.setGravity(new Vector3f(0, -24f, 0));
        characterControl.setJumpForce(new Vector3f(0, 400f, 0));

        //Anexa control ao node e node à cena
        npcNode.addControl(characterControl);
        rootNode.attachChild(npcNode);
        physicsSpace.add(characterControl);

        //Posiciona o control e o node na posição lógica atual do modelo.
        //Ajusta Y para o centro do capsule (meia altura) para evitar afundar no chão.
        Vec3 p = this.getPosition();
        Vector3f warpPos = new Vector3f((float) p.x, (float) (p.y + (CHARACTER_HEIGHT * 0.5f)), (float) p.z);
        characterControl.warp(warpPos);
        npcNode.setLocalTranslation(warpPos);
    }


    //Remove o follower da cena e da PhysicsSpace.
    public void detachFromScene(Node rootNode, PhysicsSpace physicsSpace) {
        if (npcNode == null && characterControl == null) return;

        if (characterControl != null) {
            if (physicsSpace != null) {
                try { physicsSpace.remove(characterControl); } catch (Exception ignored) {}
            }
            if (npcNode != null) {
                npcNode.removeControl(characterControl);
            }
            characterControl = null;
        }

        if (npcNode != null) {
            try { rootNode.detachChild(npcNode); } catch (Exception e) {
                try { npcNode.removeFromParent(); } catch (Exception ignored) {}
            }
            npcNode = null;
        }

        //Limpa referências guardadas caso coincidam com os parâmetros fornecidos
        if (this.rootNodeRef == rootNode) this.rootNodeRef = null;
        if (this.physicsSpaceRef == physicsSpace) this.physicsSpaceRef = null;
    }

    /**
     * Remove usando as referências guardadas (útil quando não temos acesso ao root/physics diretamente).
     * Faz remoção "best-effort" e aplica fallback de ocultação caso não consiga destacar imediatamente.
     */
    public void removeFromScene() {
        if (npcNode == null && characterControl == null) return;

        System.out.println("Removendo NPC " + getName() + " da cena");

        //Remove control da physicsSpace guardada, se possível
        if (characterControl != null) {
            if (physicsSpaceRef != null) {
                try { physicsSpaceRef.remove(characterControl); } catch (Exception ignored) {}
            }
            if (npcNode != null) {
                try { npcNode.removeControl(characterControl); } catch (Exception ignored) {}
            }
            characterControl = null;
        }

        //Tenta destacar o node usando a referência ao rootNode, se existir.
        if (npcNode != null) {
            boolean removed = false;
            if (rootNodeRef != null) {
                try {
                    rootNodeRef.detachChild(npcNode);
                    removed = true;
                } catch (Exception ignored) {}
            }
            if (!removed) {
                try {
                    // fallback genérico
                    npcNode.removeFromParent();
                } catch (Exception ignored) {}
            }

            //Fallback final: esconder o nodo para garantir que não é visível
            try {
                npcNode.setCullHint(Spatial.CullHint.Always);
                npcNode.setLocalScale(0f);
            } catch (Exception ignored) {}

            npcNode = null;
        }

        //Limpa referências armazenadas
        rootNodeRef = null;
        physicsSpaceRef = null;
    }

    /**
     * Retorna o node visual do NPC (pode ser usado para unregister em RenderIndex).
     */
    public Node getNpcNode() {
        return npcNode;
    }

    public void warpToModelPosition() {
        if (characterControl != null && npcNode != null) {
            Vec3 p = this.getPosition();
            Vector3f pos = new Vector3f((float) p.x, (float) (p.y + (CHARACTER_HEIGHT * 0.5f)), (float) p.z);
            characterControl.warp(pos);
            //Sincroniza também o node imediatamente
            npcNode.setLocalTranslation(pos);
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

            //Se estiver fora do alcance de follow, não segue nem teletransporta
            if (distSqXZ > followRangeSq) {
                teleportTimer = 0f;
                characterControl.setWalkDirection(Vector3f.ZERO);
                //Sincroniza posição lógica com a spatial para evitar drift
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
                    //Não consegue saltar essa altura: conta tempo e teleporta se exceder
                    teleportTimer += tpf;
                    //Impede movimento enquanto conta
                    characterControl.setWalkDirection(Vector3f.ZERO);

                    //Se exceder o tempo máximo, teletransporta-se
                    if (teleportTimer >= maxTeleportWait) {
                        //Só se teletransporta se a instância for AbleToTeleport
                        if (this instanceof AbleToTeleport) {
                            //Teletransporta-se perto do jogador (pequeno offset)
                            Vector3f localTeletransporte = new Vector3f((float) targetPos.x + 1f, (float) targetPos.y + 0.1f, (float) targetPos.z + 1f);
                            characterControl.warp(localTeletransporte);
                            npcNode.setLocalTranslation(localTeletransporte);
                            this.setPosition(new Vec3(localTeletransporte.x, localTeletransporte.y, localTeletransporte.z));
                        }
                        //Reinicia o timer seja teletransportado ou não para evitar loops
                        teleportTimer = 0f;
                    }
                    //Interrompe o update de movimento
                    return;
                }
            } else {
                //Sem diferença de altura que exija salto -> Reseta contador
                teleportTimer = 0f;
            }

            //Caminhar em XZ em direção ao target
            Vec3 dir = targetPos.subtract(currentPos); //Deslocamento do NPC até o alvo
            Vector3f walk;
            if (Math.abs(dir.x) < 1f && Math.abs(dir.z) < 1f) {
                walk = Vector3f.ZERO;
            } else {
                //Calculo do comprimento horizontal
                float len = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z); //Teorema de Pitágoras

                //Normaliza e multiplica pela velocidade de movimento
                float nx = dir.x / len;
                float nz = dir.z / len;
                walk = new Vector3f(nx * moveSpeed, 0f, nz * moveSpeed);
            }
            //Aplica direção de caminhada ao control
            characterControl.setWalkDirection(walk);

            //Sincroniza a posição do modelo com a física / spatial
            Vector3f worldPos = npcNode.getWorldTranslation();
            this.setPosition(new Vec3(worldPos.x, worldPos.y, worldPos.z));
        }
    }
}