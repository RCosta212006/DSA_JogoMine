package jogo.gameobject.character;

import jogo.framework.math.Vec3;

public class Enemy extends Follower {

    private final int attackDamage;
    private final float attackRange;
    private final float attackCooldown;
    private float cooldownTimer = 0f;

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
        // interação direta (opcional): inflige dano menor ao ser interagido
        if (player == null) return;
        int current = player.getHealth();
        int after = Math.max(0, current - Math.max(1, attackDamage / 2));
        player.setHealth(after);
        System.out.println(getName() + " reagiu ao toque e causou " + (current - after) + " de dano a " + player.getName());
    }
}

