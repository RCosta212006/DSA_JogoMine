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
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.VAlignment;
import jogo.Jogo;

public class GameOverAppState extends BaseAppState {

    private Container window;
    private Picture backgroundPicture;
    private float deslocamentoHorizontal = 0f;
    private float deslocamentoVertical = 150f;


    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;
        app.getInputManager().setCursorVisible(true);

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



        // Botão
        Button restartBtn = window.addChild(new Button("Jogar Novamente"));
        restartBtn.setFontSize(20f);
        restartBtn.setTextHAlignment(HAlignment.Center);

        restartBtn.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                reiniciarJogo(sapp);
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

    private void reiniciarJogo(SimpleApplication app) {
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