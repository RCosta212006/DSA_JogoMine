package jogo.ai;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Control simples para fazer um Spatial seguir outro Spatial.
 * - target: Spatial a seguir (jogador)
 * - speed: unidades por segundo
 * - stopDistance: distância mínima para parar de se aproximar para não sobrepor
 */

public class FollowControl extends AbstractControl {
    private Spatial target;
    private float speed;
    private float stopDistance; //Evita sobreposição/colisões visuais.

    public FollowControl(Spatial target, float speed, float stopDistance) {
        this.target = target;
        this.speed = speed;
        this.stopDistance = stopDistance;
    }

    public void setTarget(Spatial target) {
        this.target = target;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setStopDistance(float stopDistance) {
        this.stopDistance = stopDistance;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null || target == null) return;

        Vector3f myPos = spatial.getWorldTranslation(); //Coordenadas do spatial que vai ser seguido
        Vector3f targetPos = target.getWorldTranslation(); //Coordenadas do spatial que vai seguir
        Vector3f dir = targetPos.subtract(myPos); //Vetor direção do seguidor para o alvo
        float dist = dir.length();

        if (dist > stopDistance && dist > 0.001f) {
            dir.normalizeLocal();
            spatial.move(dir.mult(speed * tpf)); //Velocidade por segundo convertida para este frame
            //Faz com que o spatial que segue olhe para o alvo (mantém o eixo Y)
            Vector3f lookAtTarget = new Vector3f(targetPos.x, myPos.y, targetPos.z);
            spatial.lookAt(lookAtTarget, Vector3f.UNIT_Y);
        }
    }

    @Override
    protected void controlRender(com.jme3.renderer.RenderManager rm, com.jme3.renderer.ViewPort vp) {
        // nothing
    }
}

