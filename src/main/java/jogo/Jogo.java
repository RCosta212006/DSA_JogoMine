package jogo;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.AppSettings;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import jogo.appstate.*;
import jogo.engine.GameRegistry;
import jogo.engine.RenderIndex;
import jogo.gameobject.character.*;

/**
 * Main application entry.
 */
public class Jogo extends SimpleApplication {

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
        // disable flyCam, we manage camera ourselves
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.6f, 0.75f, 1f, 1f)); // sky-like

        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(false); // toggle off later
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

        Ocelot ocelot = new Ocelot("Ocelot");
        ocelot.setPosition(162f, 20f, 162f);
        registry.add(ocelot);

        Villager villager = new Villager("Villager");
        villager.setPosition(169f, 20f, 164f);
        registry.add(villager);

        Zombie zombie = new Zombie("Zombie");
        zombie.setPosition(169f, 20f, 164f);
        registry.add(zombie);

        Spider spider = new Spider("Spider");
        spider.setPosition(169f, 20f, 164f);
        registry.add(spider);

        NPCAppState npcState = new NPCAppState(rootNode, assetManager, input, physicsSpace, world);
        stateManager.attach(npcState);

        Player player = playerState.getPlayer();
        npcState.setPlayer(player);

        npcState.addFollower((jogo.gameobject.character.Follower) ocelot);
        npcState.addFollower((jogo.gameobject.character.Follower) villager);
        npcState.addFollower((jogo.gameobject.character.Follower) zombie);
        npcState.addFollower((jogo.gameobject.character.Follower) spider);

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
}
