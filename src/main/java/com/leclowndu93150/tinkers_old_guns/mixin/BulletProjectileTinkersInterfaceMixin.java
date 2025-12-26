package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import slimeknights.tconstruct.library.modifiers.entity.ProjectileWithKnockback;

@Mixin(BulletProjectile.class)
public abstract class BulletProjectileTinkersInterfaceMixin implements ProjectileWithKnockback {

    @Shadow
    private int knockback;

    @Override
    public void addKnockback(float amount) {
        this.knockback += (int) amount;
    }
}
