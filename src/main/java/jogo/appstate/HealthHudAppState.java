package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.jme3.ui.Picture;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import jogo.gameobject.character.Character;
import jogo.gameobject.character.Player;

public class HealthHudAppState extends BaseAppState {

    private final Node guiNode;
    private final AssetManager assetManager;
    private final PlayerAppState playerState;
    private Picture healthPicture;
    private final float margin = 10f;

    private final PropertyChangeListener listener = this::onHealthChanged;
    private String texturePath = "Interface/Heart_container100.png";

    public HealthHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.playerState = playerState;
    }

    @Override
    protected void initialize(Application app) {
        // carrega textura para obter dimensões originais
        Texture tex = assetManager.loadTexture(texturePath);
        Image img = tex.getImage();
        if (img == null) {
            // fallback simples: ainda tenta criar a Picture mesmo sem dimensões
            healthPicture = new Picture("healthPic");
            healthPicture.setImage(assetManager, texturePath, true);
        } else {
            healthPicture = new Picture("healthPic");
            healthPicture.setImage(assetManager, texturePath, true);
        }
        guiNode.attachChild(healthPicture);

        // posiciona/escala de acordo com o ecrã (preservando aspect ratio)
        positionTopRight();

        // liga ao player ou tenta novamente na próxima frame
        attachToPlayerOrDefer();
    }

    private void attachToPlayerOrDefer() {
        Character p = playerState.getPlayer();
        if (p != null) {
            p.addPropertyChangeListener(listener);
        } else {
            getApplication().enqueue((Runnable) () -> {
                Character p2 = playerState.getPlayer();
                if (p2 != null) p2.addPropertyChangeListener(listener);
                positionTopRight();
            });
        }
    }

    private void onHealthChanged(PropertyChangeEvent evt) {
        getApplication().enqueue((Runnable) () -> {
            positionTopRight();
        });
    }

    // Mantém o aspect ratio e coloca no canto superior direito
    private void positionTopRight() {
        SimpleApplication sapp = (SimpleApplication) getApplication();
        int w = sapp.getCamera().getWidth();
        int h = sapp.getCamera().getHeight();

        // máximo de dimensão relativo ao ecrã
        float maxDim = Math.min(w, h) * 0.20f;
        if (maxDim < 32f) maxDim = 32f;

        // obtém dimensão original da imagem para calcular aspect ratio
        Texture tex = assetManager.loadTexture(texturePath);
        Image img = tex.getImage();
        float picW = maxDim;
        float picH = maxDim;
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            float aspect = (float) img.getWidth() / (float) img.getHeight();
            if (aspect >= 1f) { // imagem larga: largura limitada por maxDim
                picW = maxDim;
                picH = maxDim / aspect;
            } else { // imagem alta: altura limitada por maxDim
                picH = maxDim;
                picW = maxDim * aspect;
            }
        }

        healthPicture.setWidth(picW);
        healthPicture.setHeight(picH);

        float picX = w - picW - margin;
        float picY = h - margin - picH; // y é a base da imagem no guiNode
        healthPicture.setLocalTranslation(picX, picY, 0);
    }

    @Override
    public void update(float tpf) {
        int health = playerState.getPlayer().getHealth();
        String newTexturePath = "";

        // Muda a textura dependendo da quantidade de vida
        switch (health) {
            case 100:
                newTexturePath = "Interface/Heart_container100.png";
                break;
            case 90:
                newTexturePath = "Interface/Heart_container90.png";
                break;
            case 80:
                newTexturePath = "Interface/Heart_container80.png";
                break;
            case 70:
                newTexturePath = "Interface/Heart_container70.png";
                break;
            case 60:
                newTexturePath = "Interface/Heart_container60.png";
                break;
            case 50:
                newTexturePath = "Interface/Heart_container50.png";
                break;
            case 40:
                newTexturePath = "Interface/Heart_container40.png";
                break;
            case 30:
                newTexturePath = "Interface/Heart_container30.png";
                break;
            case 20:
                newTexturePath = "Interface/Heart_container20.png";
                break;
            case 10:
                newTexturePath = "Interface/Heart_container10.png";
                break;
            case 0:
                newTexturePath = "Interface/Heart_container0.png";
                break;

        }
            // Muda a textura se newtexture for diferente da atual
            if (!newTexturePath.equals(texturePath)) {
                texturePath = newTexturePath;
                healthPicture.setImage(assetManager, texturePath, true);
                positionTopRight();
            }
    }

    @Override
    protected void cleanup(Application app) {
        if (healthPicture != null) healthPicture.removeFromParent();
        Character p = playerState.getPlayer();
        if (p != null) p.removePropertyChangeListener(listener);
    }

    @Override
    protected void onEnable() {
        positionTopRight();
    }

    @Override
    protected void onDisable() { }
}
