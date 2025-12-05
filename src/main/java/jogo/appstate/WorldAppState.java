package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import jogo.engine.GameRegistry;
import jogo.gameobject.GameObject;
import jogo.gameobject.character.Follower;
import jogo.gameobject.character.NPC;
import jogo.gameobject.character.Player;
import jogo.gameobject.item.BlockItem;
import jogo.gameobject.item.ItemSlot;
import jogo.gameobject.item.ToolItem;
import jogo.util.Hit;
import jogo.voxel.VoxelBlockType;
import jogo.voxel.VoxelWorld;

import java.util.Optional;

public class WorldAppState extends BaseAppState {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final PhysicsSpace physicsSpace;
    private final Camera cam;
    private final InputAppState input;
    private final GameRegistry registry;
    private PlayerAppState playerAppState;

    // world root for easy cleanup
    private Node worldNode;
    private VoxelWorld voxelWorld;
    private com.jme3.math.Vector3f spawnPosition;
    private NPCAppState npcAppState;


    public WorldAppState(Node rootNode, AssetManager assetManager, PhysicsSpace physicsSpace, Camera cam, InputAppState input, GameRegistry registry) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.physicsSpace = physicsSpace;
        this.cam = cam;
        this.input = input;
        this.registry = registry;
    }

    public void registerPlayerAppState(PlayerAppState playerAppState) {
        this.playerAppState = playerAppState;
    }

    public void registerNPCAppState(NPCAppState npcAppState) {
        this.npcAppState = npcAppState;
    }

    @Override
    protected void initialize(Application app) {
        worldNode = new Node("World");
        rootNode.attachChild(worldNode);

        // Lighting
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.20f)); // slightly increased ambient
        worldNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.35f, -1.3f, -0.25f).normalizeLocal()); // more top-down to reduce harsh contrast
        sun.setColor(ColorRGBA.White.mult(0.85f)); // slightly dimmer sun
        worldNode.addLight(sun);

        // Voxel world 16x16x16 (reduced size for simplicity)
        voxelWorld = new VoxelWorld(assetManager, 320, 32, 320);
        voxelWorld.generateLayers();
        voxelWorld.buildMeshes();
        voxelWorld.clearAllDirtyFlags();
        worldNode.attachChild(voxelWorld.getNode());
        voxelWorld.buildPhysics(physicsSpace);

        // compute recommended spawn
        spawnPosition = voxelWorld.getRecommendedSpawn();
    }

    public com.jme3.math.Vector3f getRecommendedSpawnPosition() {
        return spawnPosition != null ? spawnPosition.clone() : new com.jme3.math.Vector3f(25.5f, 12f, 25.5f);
    }

    public VoxelWorld getVoxelWorld() {
        return voxelWorld;
    }

    @Override
    public void update(float tpf) {
        if (input != null && input.isMouseCaptured() && input.consumeBreakRequested()) {
            var pick = voxelWorld.pickFirstSolid(cam, 6f);
            pick.ifPresent(hit -> {
                VoxelWorld.Vector3i cell = hit.cell;

                // 1. Verificar qual o bloco que estamos a tentar partir
                byte blockId = voxelWorld.getBlock(cell.x, cell.y, cell.z);
                VoxelBlockType blockType = voxelWorld.getPalette().get(blockId);
                int requiredTier = blockType.getRequiredTier();
                // 2. Verificar qual a ferramenta na mão do jogador
                int playerTier = 0; // 0 = Mão vazia

                HotBarHudAppState hud = getState(HotBarHudAppState.class);
                if (hud != null && playerAppState != null) {
                    int slotIndex = hud.getSelectedSlot();
                    ItemSlot heldSlot = playerAppState.getPlayer().getHotbarSlot(slotIndex);

                    if (heldSlot != null && heldSlot.getItem() instanceof ToolItem) {
                        playerTier = ((ToolItem) heldSlot.getItem()).getTier();
                    }
                }

                if (playerTier >= requiredTier) {
                    if (voxelWorld.breakAt(cell.x, cell.y, cell.z, playerAppState.getPlayer())) {
                        voxelWorld.rebuildDirtyChunks(physicsSpace);
                        playerAppState.refreshPhysics();
                    }
                } else{
                    System.out.println("Ferramente demasiado fraca");
                }
            });
        }
        //logica meter blocos
        if (input != null && input.isMouseCaptured() && input.consumePlaceRequested()) {
            // 1. Obter o estado da HUD para saber qual o slot selecionado
            HotBarHudAppState hud = getState(HotBarHudAppState.class);
            if (hud != null && playerAppState != null) {
                int slotIndex = hud.getSelectedSlot();
                Player player = playerAppState.getPlayer();
                // 2. Obter o item do slot
                ItemSlot slot = player.getHotbarSlot(slotIndex);
                // 3. Verificar se é um bloco válido
                if (slot != null && slot.getItem() instanceof BlockItem blockItem) {
                    // 4. Raycast para encontrar onde colocar (alcance de 6 unidades)
                    Optional<Hit> pick = voxelWorld.pickFirstSolid(cam, 6f);
                    if (pick.isPresent()) {
                        Hit hit = pick.get();
                        // 5. Calcular a nova posição: Célula atingida + Normal da face
                        // Ex: Se atingir o topo (Normal 0,1,0), adiciona 1 ao Y.
                        int x = hit.cell.x + (int)hit.normal.x;
                        int y = hit.cell.y + (int)hit.normal.y;
                        int z = hit.cell.z + (int)hit.normal.z;
                        //6. Para simplificar, colocamos apenas se for AIR atualmente.
                        byte currentBlock = voxelWorld.getBlock(x, y, z);
                        if (currentBlock == jogo.voxel.VoxelPalette.AIR_ID) { // Assumindo AIR_ID = 0
                            // 7. Colocar o bloco no mundo
                            voxelWorld.setBlock(x, y, z, blockItem.getBlockID());
                            // 8. Atualizar física e visual
                            voxelWorld.rebuildDirtyChunks(physicsSpace);
                            // 9. Consumir o item do inventário
                            player.consumeItem(slotIndex, 1);
                            // Forçar atualização do PropertyChangeSupport no player se necessário

                            System.out.println("Bloco colocado em: " + x + "," + y + "," + z);
                        }
                    }
                }
            }
        }
        if (input != null && input.consumeToggleShadingRequested()) {
            voxelWorld.toggleRenderDebug();
        }
        for(GameObject obj : this.registry.getAll()) {
            if(obj instanceof Follower) {
                Follower follower = (Follower) obj;
                if (follower.getTarget() == null && playerAppState != null) {
                    follower.setTarget(playerAppState.getPlayer());
                }
            }
            if(obj instanceof NPC){
                ((NPC) obj).update(tpf);
            }
        }
    }

    @Override
    protected void cleanup(Application app) {
        if (worldNode != null) {
            // Remove all physics controls under worldNode
            worldNode.depthFirstTraversal(spatial -> {
                RigidBodyControl rbc = spatial.getControl(RigidBodyControl.class);
                if (rbc != null) {
                    physicsSpace.remove(rbc);
                    spatial.removeControl(rbc);
                }
            });
            worldNode.removeFromParent();
            worldNode = null;
        }
    }

    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }

    public Player getPlayer() {
        return playerAppState != null ? playerAppState.getPlayer() : null;
    }
}
