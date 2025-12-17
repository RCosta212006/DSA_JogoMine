//Classe responsável por controlar o comportamento de um NPC e dos seus seguidores (followers) no ciclo de vida do jME, ligando-os à cena, à física e ao jogador.
package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import jogo.gameobject.character.Follower;
import jogo.gameobject.character.Player;
import java.util.HashSet;
import java.util.Set;


public class NPCAppState extends BaseAppState {
    //Usados para anexar/desanexar os followers na cena e espaço físico
    private final Node rootNode;
    private final AssetManager assetManager;

    private final InputAppState input;
    private final PhysicsSpace physicsSpace;
    private final WorldAppState world;
    private Player player;

    //Lista de followers associados a este NPC
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

    public Player getPlayer() {
        return player;
    }

    //Quando o player é definido, todos os followers recebem o novo target
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

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
    }

}
