package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import jogo.engine.GameRegistry;
import jogo.engine.RenderIndex;
import jogo.framework.math.Vec3;
import jogo.gameobject.GameObject;
import jogo.gameobject.character.*;
import jogo.gameobject.item.Item;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RenderAppState extends BaseAppState {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final GameRegistry registry;
    private final RenderIndex renderIndex;
    private Node gameNode;
    private final Map<GameObject, Spatial> instances = new HashMap<>();

    public RenderAppState(Node rootNode, AssetManager assetManager, GameRegistry registry, RenderIndex renderIndex) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.registry = registry;
        this.renderIndex = renderIndex;
    }

    @Override
    protected void initialize(Application app) {
        gameNode = new Node("GameObjects");
        rootNode.attachChild(gameNode);
    }

    @Override
    public void update(float tpf) {
        //Ensure each registered object has a spatial and sync position
        var current = registry.getAll();
        Set<GameObject> alive = new HashSet<>(current);

        //Create or update spatials
        for (GameObject obj : current) {
            //If NPC dead -> Force remove any existing spatial and skip creation/sync
            if (obj instanceof NPC npc && npc.getHealth() <= 0) {
                Spatial existing = instances.remove(obj);
                if (existing != null) {
                    renderIndex.unregister(existing);
                    if (existing.getParent() != null) existing.removeFromParent();
                }
                //Also try remove Follower's internal node if present
                if (obj instanceof Follower follower) {
                    Node n = follower.getNpcNode();
                    if (n != null) {
                        try {
                            renderIndex.unregister(n);
                        } catch (Exception ignored) {}
                        if (n.getParent() != null) n.removeFromParent();
                    }
                }
                //Don't create or sync a new spatial for dead NPCs
                continue;
            }

            //Get or create spatial
            Spatial s = instances.get(obj);
            if (s == null) {
                s = createSpatialFor(obj);
                if (s != null) {
                    gameNode.attachChild(s);
                    instances.put(obj, s);
                    renderIndex.register(s, obj);
                }
            }
            if (s != null) {
                Vec3 p = obj.getPosition();
                s.setLocalTranslation(new Vector3f(p.x, p.y, p.z));
            }
        }

        // Cleanup: remove spatials for objects no longer in registry
        var it = instances.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (!alive.contains(e.getKey())) {
                Spatial s = e.getValue();
                renderIndex.unregister(s);
                if (s.getParent() != null) s.removeFromParent();
                it.remove();
            }
        }
    }

    private Spatial createSpatialFor(GameObject obj) {
        try {
            return obj.getSpatial(assetManager);
        } catch (Exception e) {
            // Falha ao criar spatial para esse objeto -> log leve e retorna null
            System.err.println("Falha a criar Spatial para " + obj.getName() + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void cleanup(Application app) {
        if (gameNode != null) {
            gameNode.removeFromParent();
            gameNode = null;
        }
        instances.clear();
    }

    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}
