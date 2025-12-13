package jogo;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.AppSettings;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.simsilica.lemur.GuiGlobals;
import jogo.appstate.*;
import jogo.engine.GameRegistry;
import jogo.engine.RenderIndex;
import jogo.framework.math.Vec3;
import jogo.gameobject.character.*;

import java.util.Random;

/**
 * Main application entry.
 */
public class Jogo extends SimpleApplication {

    private final Random rng = new Random();

    public static void main(String[] args) {
        Jogo app = new Jogo();
        app.setShowSettings(true); // show settings dialog
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Test");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setGammaCorrection(true); // enable sRGB gamma-correct rendering
        app.setSettings(settings);
        app.start();
    }

    private BulletAppState bulletAppState;

    @Override
    public void simpleInitApp() {

        //incializar UI
        GuiGlobals.initialize(this);

        // disable flyCam, we manage camera ourselves
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.6f, 0.75f, 1f, 1f)); // sky-like

        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(false); // toggle off later
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

        irParaMenu();

    }

    public void irParaMenu() {
        // Garantir que não há restos de jogo a correr
        limparEstadosDeJogo();

        // Se viemos do Game Over, garantimos que ele também sai
        stateManager.detach(stateManager.getState(GameOverAppState.class));

        // Anexar o Menu
        stateManager.attach(new MainMenuAppState());
    }


    public void iniciarJogo() {
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

        // AppStates (order matters a bit: input -> world -> render -> interaction -> player)
        InputAppState input = new InputAppState();
        stateManager.attach(input);

        // Engine registry and render layers
        GameRegistry registry = new GameRegistry();
        RenderIndex renderIndex = new RenderIndex();
        stateManager.attach(new RenderAppState(rootNode, assetManager, registry, renderIndex));

        WorldAppState world = new WorldAppState(rootNode, assetManager, physicsSpace, cam, input, registry);
        stateManager.attach(world);

        stateManager.attach(new InteractionAppState(rootNode, cam, input, renderIndex, world));

        // Demo objects
        // Chest chest = new Chest();
        // chest.setPosition(26.5f, world.getRecommendedSpawnPosition().y - 2f, 26.5f);z
        // registry.add(chest);

        PlayerAppState playerState = new PlayerAppState(rootNode, assetManager, cam, input, physicsSpace, world);
        stateManager.attach(playerState);
        stateManager.attach(new HealthHudAppState(guiNode, assetManager, playerState));
        stateManager.attach(new HotBarHudAppState(guiNode, assetManager, playerState));
        stateManager.attach(new InventoryHudAppState(guiNode, assetManager, playerState, input));
        stateManager.attach(new ScoreHudAppState(guiNode,assetManager,playerState));

        // Criar NPCs usando spawn em superfície
        Ocelot ocelot = new Ocelot("Ocelot");
        ocelot.setPosition(162, 27,162);
        registry.add(ocelot);

        Villager villager = new Villager("Villager");
        villager.setPosition(150, 27,162);
        registry.add(villager);

        Zombie zombie = new Zombie("Zombie");
        zombie.setPosition(170, 27,162);
        registry.add(zombie);

        Spider spider = new Spider("Spider");
        spider.setPosition(162, 27,162);
        registry.add(spider);


        NPCAppState npcState = new NPCAppState(rootNode, assetManager, input, physicsSpace, world);
        stateManager.attach(npcState);

        Player player = playerState.getPlayer();
        npcState.setPlayer(player);

        npcState.addFollower((jogo.gameobject.character.Follower) ocelot);
        npcState.addFollower((jogo.gameobject.character.Follower) villager);
        npcState.addFollower((jogo.gameobject.character.Follower) zombie);
        npcState.addFollower((jogo.gameobject.character.Follower) spider);

        // Log após os followers estarem na cena e sincronizados

        configurarEfeitos();

    }

    public void terminarJogo(int finalScore) {
        // Remove todos os estados de jogo para limpar a memória e lógica
        limparEstadosDeJogo();

        // Lança o menu de Game Over
        stateManager.attach(new GameOverAppState(finalScore));
    }

    private void limparEstadosDeJogo() {
        stateManager.detach(stateManager.getState(InputAppState.class));
        stateManager.detach(stateManager.getState(RenderAppState.class));
        stateManager.detach(stateManager.getState(WorldAppState.class));
        stateManager.detach(stateManager.getState(InteractionAppState.class));
        stateManager.detach(stateManager.getState(PlayerAppState.class));
        stateManager.detach(stateManager.getState(HealthHudAppState.class));
        stateManager.detach(stateManager.getState(HotBarHudAppState.class));
        stateManager.detach(stateManager.getState(InventoryHudAppState.class));
        stateManager.detach(stateManager.getState(NPCAppState.class));

        rootNode.detachAllChildren();
        guiNode.detachAllChildren();
        viewPort.clearProcessors();
    }


    private void configurarEfeitos(){
        // Post-processing: SSAO for subtle contact shadows
        try {
            FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
            Class<?> ssaoCls = Class.forName("com.jme3.post.ssao.SSAOFilter");
            Object ssao = ssaoCls.getConstructor(float.class, float.class, float.class, float.class)
                    .newInstance(2.1f, 0.6f, 0.5f, 0.02f); // radius, intensity, scale, bias
            // Add filter via reflection to avoid compile-time dependency
            java.lang.reflect.Method addFilter = FilterPostProcessor.class.getMethod("addFilter", Class.forName("com.jme3.post.Filter"));
            addFilter.invoke(fpp, ssao);
            viewPort.addProcessor(fpp);
        } catch (Exception e) {
            System.out.println("SSAO not available (effects module missing?): " + e.getMessage());
        }
    }

    /**
     * Posiciona o NPC sobre a superfície em X/Z aleatório próximo dos valores fornecidos.
     * - world.findSurfacePosition(...) procura a superfície no VoxelWorld.
     * - registry.add garante que o objeto é registrado para render/logic.
     * Se a procura falhar, usa fallback do world.getRecommendedSpawnPosition().
     */
    private void spawnOnSurface(NPC npc, WorldAppState world, GameRegistry registry, float baseX, float baseZ, float maxOffset) {
        // Variação aleatória em torno da base
        float rx = baseX + (rng.nextFloat() * 2f - 1f) * maxOffset;
        float rz = baseZ + (rng.nextFloat() * 2f - 1f) * maxOffset;

        Vec3 pos = null;
        if (world != null) {
            try {
                pos = world.findSurfacePosition(rx, rz);
            } catch (Exception e) {
                // não falhar se API diferente; cai para fallback
                pos = null;
            }
        }

        if (pos == null) {
            // último recurso: posição padrão
            pos = new Vec3(rx, 20f, rz);
        }

        npc.setPosition(pos);
        registry.add(npc);

        // Log para debug — permite ver no console onde o NPC ficou
        System.out.println("Spawned NPC: " + npc.getName() + " at x=" + pos.x + " y=" + pos.y + " z=" + pos.z);
    }

    private void logNPCPositions(GameRegistry registry) {
        if (registry == null) return;
        var all = registry.getAll();
        System.out.println("=== NPC positions ===");
        for (var obj : all) {
            if (obj instanceof NPC) {
                Vec3 p = obj.getPosition();
                System.out.println(obj.getName() + " -> x=" + p.x + " y=" + p.y + " z=" + p.z);
            }
        }
        System.out.println("=====================");
    }
}
