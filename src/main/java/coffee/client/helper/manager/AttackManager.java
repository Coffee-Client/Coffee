/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.manager;

import coffee.client.CoffeeMain;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

public class AttackManager {

    public static final long MAX_ATTACK_TIMEOUT = 5000;
    static long lastAttack = 0;
    static LivingEntity lastAttacked;

    public static LivingEntity getLastAttackInTimeRange() {
        if (getLastAttack() + MAX_ATTACK_TIMEOUT < System.currentTimeMillis() || CoffeeMain.client.player == null || CoffeeMain.client.player.isDead()) {
            lastAttacked = null;
        }
        if (lastAttacked != null) {
            if (lastAttacked.getPos().distanceTo(CoffeeMain.client.player.getPos()) > 16 || lastAttacked.isDead()) {
                lastAttacked = null;
            }
        }
        return lastAttacked;
    }

    public static void registerLastAttacked(LivingEntity entity) {
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }
        if (entity.equals(CoffeeMain.client.player)) {
            return;
        }
        lastAttacked = entity;
        lastAttack = System.currentTimeMillis();
    }

    public static long getLastAttack() {
        return lastAttack;
    }
}
