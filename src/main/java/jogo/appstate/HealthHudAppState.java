package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.jme3.ui.Picture;
import jogo.gameobject.character.Character;

public class HealthHudAppState extends BaseAppState {

        private final Node guiNode;
        private final AssetManager assetManager;
        private final PlayerAppState playerState;
        private Picture healthPicture;
        private final float margin = 10f;

        private final PropertyChangeListener listener = this::onHealthChanged;

        public HealthHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState) {
            this.guiNode = guiNode;
            this.assetManager = assetManager;
            this.playerState = playerState;
        }

        @Override
        protected void initialize(Application app) {
            healthPicture.setImage(assetManager, "Textures/ColoredTex/Monkey.png", true);
            healthPicture.setWidth(playerState.getCam().getWidth()/2);
            healthPicture.setHeight(playerState.getCam().getHeight()/2);
            healthPicture.setPosition(playerState.getCam().getWidth()/4, playerState.getCam().getHeight()/4);
            // tenta ligar ao player agora; se não existir, tenta no next-frame via enqueue
            attachToPlayerOrDefer();
            // texto inicial
            positionTopRight();
        }

        private void attachToPlayerOrDefer() {
            Character p = playerState.getPlayer();
            if (p != null) {
                p.addPropertyChangeListener(listener);
            } else {
                // tentamos novamente no thread do motor na próxima frame
                getApplication().enqueue((Runnable) () -> {
                    Character p2 = playerState.getPlayer();
                    if (p2 != null) p2.addPropertyChangeListener(listener);
                    // atualiza HUD caso o player já exista
                    positionTopRight();
                });
            }
        }

        private void onHealthChanged(PropertyChangeEvent evt) {
            // garante que atualização ocorre no thread do jME
            getApplication().enqueue((Runnable) () -> {
                positionTopRight();
            });
        }


        // Coordenadas no guiNode: origem no canto inferior esquerdo.
        // Para alinhar no topo direito calcula-se a posição com base em settings.getWidth() e settings.getHeight().
        private void positionTopRight() {
            SimpleApplication sapp = (SimpleApplication) getApplication();
            int w = sapp.getCamera().getWidth();
            int h = sapp.getCamera().getHeight();
            float x = w - healthPicture.getLineWidth() - margin;
            float y = h - margin;
            healthPicture.setLocalTranslation(x, y, 0);
        }

        @Override
        public void update(float tpf) {
            // nada por-frame: atualizamos apenas quando a propriedade muda
        }

        @Override
        protected void cleanup(Application app) {
            if (healthPicture != null) healthPicture.removeFromParent();
            Character p = playerState.getPlayer();
            if (p != null) p.removePropertyChangeListener(listener);
        }

        @Override
        protected void onEnable() {
            updateText();
            positionTopRight();
        }

        @Override
        protected void onDisable() { }
    }

