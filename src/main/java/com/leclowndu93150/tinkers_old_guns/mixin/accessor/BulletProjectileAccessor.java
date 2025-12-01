package com.leclowndu93150.tinkers_old_guns.mixin.accessor;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin to access protected fields in BulletProjectile.
 */
@Mixin(BulletProjectile.class)
public interface BulletProjectileAccessor {

    @Accessor("knockback")
    int getKnockback();
}
