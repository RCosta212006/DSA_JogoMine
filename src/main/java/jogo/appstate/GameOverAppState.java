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

public class GameOverAppState extends BaseAppState {

    private Container window;
    private Picture backgroundPicture;
    private float deslocamentoHorizontal = 0f;
    private float deslocamentoVertical = 150f;
    private int finalScore;

    public GameOverAppState(int score) {
        this.finalScore = score;
    }


    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;
        app.getInputManager().setCursorVisible(true);

        HighScoreManager.saveScore("Player", finalScore);

        float screenWidth = sapp.getCamera().getWidth();
        float screenHeight = sapp.getCamera().getHeight();


        backgroundPicture = new Picture("GameOverBackground");
        try {
            backgroundPicture.setImage(app.getAssetManager(), "Interface/GameOverScreen_craft.png", true);
        } catch (Exception e) {
            System.out.println("ERRO: Imagem de game over não encontrada.");
        }

        backgroundPicture.setWidth(screenWidth);
        backgroundPicture.setHeight(screenHeight);
        backgroundPicture.setPosition(0, 0);
        sapp.getGuiNode().attachChild(backgroundPicture);


        // 2. Configurar UI sobreposta
        window = new Container();
        window.setBackground(null); // Fundo transparente

        // Título
        Label title = window.addChild(new Label("GAME OVER"));
        title.setFontSize(60f); // Reduzi um pouco para caber melhor numa lápide
        title.setColor(ColorRGBA.Black); // Cor de "pedra" para parecer gravado
        title.setTextHAlignment(HAlignment.Center);

        Label scoreLabel = window.addChild(new Label("Final Score: " + finalScore));
        scoreLabel.setFontSize(30f);
        scoreLabel.setColor(ColorRGBA.DarkGray);
        scoreLabel.setTextHAlignment(HAlignment.Center);
        // Adicionar margem
        scoreLabel.setInsets(new Insets3f(10, 0, 20, 0));


        // Botão
        Button restartBtn = window.addChild(new Button("Voltar ao Menu"));
        restartBtn.setFontSize(20f);
        restartBtn.setTextHAlignment(HAlignment.Center);

        restartBtn.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                voltarAoMenu(sapp);
            }
        });



        Vector3f size = window.getPreferredSize();


        float centroXScreen = screenWidth / 2;
        float centroYScreen = screenHeight / 2;


        float alvoX = centroXScreen + deslocamentoHorizontal;
        float alvoY = centroYScreen + deslocamentoVertical;


        float finalX = alvoX - (size.x / 2);
        float finalY = alvoY + (size.y / 2);

        window.setLocalTranslation(finalX, finalY, 1f);

        sapp.getGuiNode().attachChild(window);
    }

    private void voltarAoMenu(SimpleApplication app) {
        getStateManager().detach(this);
        if (app instanceof Jogo) {
            ((Jogo) app).irParaMenu();
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