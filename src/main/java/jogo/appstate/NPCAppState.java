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
        //Não cria followers aqui; são anexados quando addFollower é chamado
    }

    @Override
    public void update(float tpf) {
        for (Follower f : followers) {
            f.update(tpf);
        }
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
        for (Follower f : followers) {
            f.detachFromScene(rootNode, physicsSpace);
        }
        followers.clear();
    }

    public void addFollower(Follower f) {
        if (f == null || followers.contains(f)) return;
        followers.add(f);
        f.attachToScene(rootNode, physicsSpace);
        if (player != null) {
            f.setTarget(player);
            f.warpToModelPosition();
        }
    }

    public void removeFollower(Follower f) {
        if (f == null || !followers.contains(f)) return;
        f.detachFromScene(rootNode, physicsSpace);
        followers.remove(f);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
    }

    public void refreshPhysics() {
        for (Follower f : followers) {
            f.warpToModelPosition();
        }
    }

}
