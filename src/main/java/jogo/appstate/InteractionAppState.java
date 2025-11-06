package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.collision.CollisionResults;
import jogo.engine.RenderIndex;
import jogo.gameobject.GameObject;
import jogo.gameobject.item.Item;
import jogo.voxel.VoxelWorld;

public class InteractionAppState extends BaseAppState {

    private final Node rootNode;
    private final Camera cam;
    private final InputAppState input;
    private final RenderIndex renderIndex;
    private final WorldAppState world;
    private float reach = 5.5f;

    public InteractionAppState(Node rootNode, Camera cam, InputAppState input, RenderIndex renderIndex, WorldAppState world) {
        this.rootNode = rootNode;
        this.cam = cam;
        this.input = input;
        this.renderIndex = renderIndex;
        this.world = world;
    }

    @Override
    protected void initialize(Application app) { }

    @Override
    public void update(float tpf) {
        if (!input.isMouseCaptured()) return;
        if (!input.consumeInteractRequested()) return;

        Vector3f origin = cam.getLocation();
        Vector3f dir = cam.getDirection().normalize();

        // 1) Try to interact with a rendered GameObject (items)
        Ray ray = new Ray(origin, dir);
        ray.setLimit(reach);
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);
        if (results.size() > 0) {
            Spatial hit = results.getClosestCollision().getGeometry();
            GameObject obj = findRegistered(hit);
            if (obj instanceof Item item) {
                item.onInteract();
                System.out.println("Interacted with item: " + obj.getName());
                return; // prefer item interaction if both are hit
            }
        }

        // 2) If no item hit, consider voxel block under crosshair (exercise for students)
        VoxelWorld vw = world != null ? world.getVoxelWorld() : null;
        if (vw != null) {
            vw.pickFirstSolid(cam, reach).ifPresent(cell -> {
                // Leave as exercise: implement voxel interaction (e.g., toggle, place, open menu)
                System.out.println("TODO (exercise): interact with voxel at " + cell.x + "," + cell.y + "," + cell.z);
            });
        }
    }

    private GameObject findRegistered(Spatial s) {
        Spatial cur = s;
        while (cur != null) {
            GameObject obj = renderIndex.lookup(cur);
            if (obj != null) return obj;
            cur = cur.getParent();
        }
        return null;
    }

    @Override
    protected void cleanup(Application app) { }

    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }
}
