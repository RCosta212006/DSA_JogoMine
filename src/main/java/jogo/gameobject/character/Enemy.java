package jogo.gameobject.character;

import jogo.framework.math.Vec3;

public class Enemy extends Follower {

    private final int attackDamage;
    private final float attackRange;
    private final float attackCooldown;
    private float cooldownTimer = 0f;
    private boolean dead = false;

    public Enemy(String name, int attackDamage, float attackCooldown, float attackRange) {
        super(name);
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.attackRange = attackRange;
        setHealth(40);
        setMaxHealth(40);
    }

    @Override
    public void update(float tpf) {
        if (dead) return;

        //Mantém comportamento de Follower (movimentação)
        super.update(tpf);

        //Reduz cooldown
        if (cooldownTimer > 0f) cooldownTimer -= tpf;

        //Tenta atacar se tiver target válido e cooldown pronto
        var target = getTarget();
        //Verifica se o target é um Player
        if (target instanceof Player player && cooldownTimer <= 0f) {
            //Verifica distância
            Vec3 myPos = getPosition();
            //Obtém posição do target
            Vec3 targetPos = target.getPosition();

            //Verifica se ambas posições são válidas
            if (myPos != null && targetPos != null) {
                //Calcular distância escalar entre posições (subtração vetorial seguida de cálculo de comprimento(ΔS = (S_{f}) - (S_{0})))
                float dist = myPos.subtract(targetPos).length();
                if (dist <= attackRange) {
                    //Aplica dano simples
                    int current = player.getHealth();
                    int after = Math.max(0, current - attackDamage);
                    player.setHealth(after);
                    cooldownTimer = attackCooldown;
                    System.out.println(getName() + " atacou " + player.getName() + " por " + attackDamage + " pontos. HP agora: " + after);
                }
            }
        }
    }

    //Lógica de interação
    @Override
    public void onInteract(Player player) {
        //Jogador interage para causar dano ao NPC
        if (player == null) return;
        if (dead) return;

        int damage = 10; //Dano por interação
        int before = getHealth();
        int after = Math.max(0, before - damage);
        setHealth(after);
        System.out.println(player.getName() + " causou " + (before - after) + " de dano a " + getName() + ". HP agora: " + after);

        if (after <= 0) {
            System.out.println("Matei " + getName());
            die();
        }
    }

    //Lógica de morte
    private void die() {
        if (dead) return;
        dead = true;
        //Remove visual/physics usando lógica heradada
        removeFromScene();
        //Limpa target para evitar referências
        setTarget(null);
    }
}

