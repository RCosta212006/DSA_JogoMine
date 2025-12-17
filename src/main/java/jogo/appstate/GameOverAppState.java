package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
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

        //Configurar imagem de fundo
        float screenWidth = sapp.getCamera().getWidth();
        float screenHeight = sapp.getCamera().getHeight();

        //Carregar imagem de fundo
        backgroundPicture = new Picture("GameOverBackground");
        try {
            backgroundPicture.setImage(app.getAssetManager(), "Interface/GameOverScreen_craft.png", true);
        } catch (Exception e) {
            System.out.println("ERRO: Imagem de game over não encontrada.");
        }

        //Ajustar tamanho e posição
        backgroundPicture.setWidth(screenWidth);
        backgroundPicture.setHeight(screenHeight);
        backgroundPicture.setPosition(0, 0);
        sapp.getGuiNode().attachChild(backgroundPicture);

        //Configurar UI sobreposta
        window = new Container();
        window.setBackground(null); // Fundo transparente

        //Título
        Label title = window.addChild(new Label("GAME OVER"));
        title.setFontSize(60f); // Reduzi um pouco para caber melhor numa lápide
        title.setColor(ColorRGBA.Black); // Cor de "pedra" para parecer gravado
        title.setTextHAlignment(HAlignment.Center);

        //Pontuação final
        Label scoreLabel = window.addChild(new Label("Final Score: " + finalScore));
        scoreLabel.setFontSize(30f);
        scoreLabel.setColor(ColorRGBA.DarkGray);
        scoreLabel.setTextHAlignment(HAlignment.Center);

        //Adicionar margem
        scoreLabel.setInsets(new Insets3f(10, 0, 20, 0));

        //Botão
        Button restartBtn = window.addChild(new Button("Voltar ao Menu"));
        restartBtn.setFontSize(20f);
        restartBtn.setTextHAlignment(HAlignment.Center);

        //Ação do botão
        restartBtn.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                voltarAoMenu(sapp);
            }
        });

        //Posicionar janela no centro da tela com deslocamentos
        Vector3f size = window.getPreferredSize();

        //Centro da tela
        float centroXScreen = screenWidth / 2;
        float centroYScreen = screenHeight / 2;

        //Calcula posição do canto superior esquerdo da janela
        float alvoX = centroXScreen + deslocamentoHorizontal;
        float alvoY = centroYScreen + deslocamentoVertical;

        //Ajusta para o canto superior esquerdo da janela
        float finalX = alvoX - (size.x / 2);
        float finalY = alvoY + (size.y / 2);

        //Define a posição final
        window.setLocalTranslation(finalX, finalY, 1f);

        sapp.getGuiNode().attachChild(window);
    }

    private void voltarAoMenu(SimpleApplication app) {
        getStateManager().detach(this);
        if (app instanceof Jogo) {
            ((Jogo) app).goToMenu();
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