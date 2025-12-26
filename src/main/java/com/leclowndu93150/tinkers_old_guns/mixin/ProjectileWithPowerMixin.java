package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.entity.ProjectileWithPower;

@Mixin(value = ProjectileWithPower.class, remap = false)
public interface ProjectileWithPowerMixin {

    @Inject(method = "getDamage(Lnet/minecraft/world/entity/projectile/Projectile;)F", at = @At("HEAD"), cancellable = true)
    private static void getDamageBullet(Projectile projectile, CallbackInfoReturnable<Float> cir) {
        if (projectile instanceof BulletProjectile bullet) {
            float damage = (float) Mth.ceil(Mth.clamp(bullet.getDamage() * projectile.getDeltaMovement().length(), 0, Integer.MAX_VALUE));
            cir.setReturnValue(damage);
        }
    }
}
