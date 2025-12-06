package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import jogo.gameobject.character.Player;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ScoreHudAppState extends BaseAppState {
    private final Node guiNode;
    private final AssetManager assetManager;
    private final PlayerAppState playerState;
    private BitmapText scoreText;
    private final PropertyChangeListener listener = this::onScoreChanged;

    public ScoreHudAppState(Node guiNode, AssetManager assetManager, PlayerAppState playerState) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.playerState = playerState;
    }

    @Override
    protected void initialize(Application application) {
        // Carregar fonte
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        scoreText = new BitmapText(font, false);
        scoreText.setSize(font.getCharSet().getRenderedSize() * 2f); // Um pouco maior
        scoreText.setColor(ColorRGBA.Yellow); // Cor destaque
        scoreText.setText("Score: 0");

        guiNode.attachChild(scoreText);

        // Posicionar no canto superior esquerdo
        positionScore();

        // Ligar ao Player para ouvir mudanças
        attachToPlayerOrDefer();

    }
    private void positionScore() {
        SimpleApplication sapp = (SimpleApplication) getApplication();
        int h = sapp.getCamera().getHeight();
        // Margem de 10px da esquerda, e 10px do topo
        scoreText.setLocalTranslation(10, h - 10, 0);
    }

    private void attachToPlayerOrDefer() {
        Player p = playerState.getPlayer();
        if (p != null) {
            p.addPropertyChangeListener(listener);
            // Atualiza texto inicial
            updateScoreText(p.getScore());
        } else {
            // Tenta na próxima frame se o player ainda não estiver pronto
            getApplication().enqueue(this::attachToPlayerOrDefer);
        }
    }

    private void onScoreChanged(PropertyChangeEvent evt) {
        if ("score".equals(evt.getPropertyName())) {
            int newScore = (int) evt.getNewValue();
            updateScoreText(newScore);
        }
    }

    private void updateScoreText(int score) {
        // Garante que corre na thread principal de renderização
        getApplication().enqueue(() -> {
            scoreText.setText("Score: " + score);
        });
    }


    @Override
    protected void cleanup(Application app) {
        if (scoreText != null) scoreText.removeFromParent();
        Player p = playerState.getPlayer();
        if (p != null) {
            p.removePropertyChangeListener(listener);
        }
    }

    @Override
    protected void onEnable() {
        if (scoreText != null) guiNode.attachChild(scoreText);
    }

    @Override
    protected void onDisable() {
        if (scoreText != null) scoreText.removeFromParent();
    }
}
