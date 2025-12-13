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

        // mantém comportamento de Follower (movimentação)
        super.update(tpf);

        // reduz cooldown
        if (cooldownTimer > 0f) cooldownTimer -= tpf;

        // tenta atacar se tiver target válido e cooldown pronto
        var target = getTarget();
        if (target instanceof Player player && cooldownTimer <= 0f) {
            Vec3 myPos = getPosition();
            Vec3 targetPos = target.getPosition();
            if (myPos != null && targetPos != null) {
                // calcular distância escalar entre posições
                float dist = myPos.subtract(targetPos).length();
                if (dist <= attackRange) {
                    // aplica dano simples
                    int current = player.getHealth();
                    int after = Math.max(0, current - attackDamage);
                    player.setHealth(after);
                    cooldownTimer = attackCooldown;
                    System.out.println(getName() + " atacou " + player.getName() + " por " + attackDamage + " pontos. HP agora: " + after);
                }
            }
        }
    }

    @Override
    public void onInteract(Player player) {
        // jogador interage para causar dano ao NPC
        if (player == null) return;
        if (dead) return;

        int damage = 10; // dano por interação
        int before = getHealth();
        int after = Math.max(0, before - damage);
        setHealth(after);
        System.out.println(player.getName() + " causou " + (before - after) + " de dano a " + getName() + ". HP agora: " + after);

        if (after <= 0) {
            System.out.println("Matei " + getName());
            die();
        }
    }

    private void die() {
        if (dead) return;
        dead = true;
        //Remove visual/physics usando método herdado
        removeFromScene();
        //Limpa target para evitar referências
        setTarget(null);
    }
}

