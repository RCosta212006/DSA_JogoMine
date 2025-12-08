package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.material.MatParam;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import com.simsilica.lemur.*;
import jogo.Jogo;
import jogo.save.HighScoreManager;
import jogo.save.ScoreEntry;

import java.util.List;

public class MainMenuAppState extends BaseAppState {

    private Container window;
    private Picture backgroundPicture;

    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;
        app.getInputManager().setCursorVisible(true);

        float screenWidth = sapp.getCamera().getWidth();
        float screenHeight = sapp.getCamera().getHeight();

        // 1. Imagem de Fundo
        backgroundPicture = new Picture("MainMenuBackground");
        try {
            // Podes criar uma imagem nova "mainmenu_bg.png" ou usar a mesma
            backgroundPicture.setImage(app.getAssetManager(), "Interface/MainMenu_craft.png", true);
        } catch (Exception e) {
            // Ignorar se não existir
        }
        backgroundPicture.setWidth(screenWidth);
        backgroundPicture.setHeight(screenHeight);
        backgroundPicture.setPosition(0, 0);
        sapp.getGuiNode().attachChild(backgroundPicture);

        // 2. Janela de Menu
        window = new Container();
        window.setBackground(null);

        // Container para os High Scores
        Container scoresContainer = window.addChild(new Container());
        scoresContainer.setBackground(null); // Transparente ou usa um TbtQuadBackgroundComponent se quiseres fundo
        scoresContainer.setInsets(new Insets3f(20, 0, 0, 0)); // Margem topo

        Label scoreTitle = scoresContainer.addChild(new Label("--- Top Scores ---"));
        scoreTitle.setColor(ColorRGBA.DarkGray);
        scoreTitle.setFontSize(25f);
        scoreTitle.setTextHAlignment(HAlignment.Center);

        // Carregar e listar Scores
        List<ScoreEntry> highScores = HighScoreManager.loadScores();

        if (highScores.isEmpty()) {
            Label empty = scoresContainer.addChild(new Label("(Sem registos)"));
            empty.setColor(ColorRGBA.Black);
            empty.setTextHAlignment(HAlignment.Center);
        } else {
            for (ScoreEntry entry : highScores) {
                Label l = scoresContainer.addChild(new Label(entry.toString()));
                l.setColor(ColorRGBA.Black);
                l.setFontSize(20f);
                l.setTextHAlignment(HAlignment.Center);
            }
        }



        // Botão Novo Jogo
        Button newGameBtn = window.addChild(new Button("Novo Jogo"));
        newGameBtn.setFontSize(40f);
        newGameBtn.setColor(ColorRGBA.Black);
        newGameBtn.setTextHAlignment(HAlignment.Center);
        newGameBtn.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                // Inicia o jogo (Gera mundo novo)
                iniciarNovoJogo(sapp);
            }
        });

        // Botão Carregar Jogo (Placeholder)
        Button loadGameBtn = window.addChild(new Button("Carregar Jogo"));
        loadGameBtn.setFontSize(40f);
        loadGameBtn.setColor(ColorRGBA.Black);
        loadGameBtn.setTextHAlignment(HAlignment.Center);
        loadGameBtn.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                System.out.println("Funcionalidade de Carregar Jogo ainda não implementada.");
            }
        });

        // Posicionar
        Vector3f size = window.getPreferredSize();
        window.setLocalTranslation((screenWidth - size.x) / 2, (screenHeight + size.y - 200) / 2, 1f);

        sapp.getGuiNode().attachChild(window);
    }

    private void iniciarNovoJogo(SimpleApplication app) {
        // Remove o menu e chama o método de iniciar no Jogo.java
        getStateManager().detach(this);
        if (app instanceof Jogo) {
            ((Jogo) app).iniciarJogo();
        }
    }


    @Override
    protected void cleanup(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;
        sapp.getGuiNode().detachChild(window);
        sapp.getGuiNode().detachChild(backgroundPicture);
    }

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}
}