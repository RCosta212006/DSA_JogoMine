package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import jogo.framework.math.Vec3;
import jogo.gameobject.character.Follower;
import jogo.gameobject.character.NPC;
import jogo.gameobject.character.Player;

import java.util.HashSet;
import java.util.Set;


public class NPCAppState extends BaseAppState {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final InputAppState input;
    private final PhysicsSpace physicsSpace;
    private final WorldAppState world;
    public NPC getNPC() { return npc; }
    private NPC npc;
    private Player player;

    private Node npcNode;
    private BetterCharacterControl characterControl;
    private final Set<Follower> followers = new HashSet<>();

    public NPCAppState(Node rootNode, AssetManager assetManager, InputAppState input, PhysicsSpace physicsSpace, WorldAppState world) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.input = input;
        this.physicsSpace = physicsSpace;
        this.world = world;
        world.registerNPCAppState(this);
        if (this.world != null) this.world.registerNPCAppState(this);
    }

    // tuning
    private float moveSpeed = 8.0f;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        for (Follower f : followers) {
            f.setTarget(player);
        }
    }

    @Override
    protected void initialize(Application app) {
        if (npcNode != null) return;

        npcNode = new Node("Follower-" + npc.getName());
        characterControl = new BetterCharacterControl(0.42f, 1.8f, 80f);
        //Mesma configuração usada para o jogador
        characterControl.setGravity(new com.jme3.math.Vector3f(0, -24f, 0));
        characterControl.setJumpForce(new com.jme3.math.Vector3f(0, 400f, 0));

        npcNode.addControl(characterControl);
        rootNode.attachChild(npcNode);
        physicsSpace.add(characterControl);

        // posiciona o control na posição atual do modelo
        Vec3 p = this.getPosition();
        characterControl.warp(new Vector3f(p.x, p.y, p.z));
    }

    private Vec3 getPosition() {
        if (npcNode != null) {
            Vector3f worldPos = npcNode.getWorldTranslation();
            return new Vec3(worldPos.x, worldPos.y, worldPos.z);
        }
        return new Vec3(0, 0, 0);
    }

    @Override
    protected void cleanup(Application app) {
        if (npcNode != null) {
            if (characterControl != null) {
                physicsSpace.remove(characterControl);
                npcNode.removeControl(characterControl);
                characterControl = null;
            }
            npcNode.removeFromParent();
            npcNode = null;
        }
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    public void refreshPhysics() {
        if (characterControl != null) {
            physicsSpace.remove(characterControl);
            physicsSpace.add(characterControl);
        }
    }

}
