package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.common.TinkerDamageTypes;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.tools.modifiers.traits.melee.EnderferenceModifier;

import javax.annotation.Nullable;

/**
 * Mixin to make EnderferenceModifier's Enderman-damage code work with BulletProjectile.
 * The original method only applies special Enderman damage when projectile is AbstractArrow.
 * Endermen normally teleport away from projectiles, but this modifier allows hitting them.
 */
@Mixin(value = EnderferenceModifier.class, remap = false)
public class EnderferenceModifierMixin {

    @Inject(method = "onProjectileHitEntity", at = @At("RETURN"), cancellable = true)
    private void onProjectileHitEntityBullet(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier,
                                              Projectile projectile, EntityHitResult hit,
                                              @Nullable LivingEntity attacker, @Nullable LivingEntity target,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (target != null && target.getType() == EntityType.ENDERMAN &&
            !(projectile instanceof AbstractArrow) && projectile instanceof BulletProjectile bullet) {

            int damage = Mth.ceil(Mth.clamp(bullet.getDeltaMovement().length() * bullet.getDamage(), 0.0D, Integer.MAX_VALUE));
            if (bullet.isCritArrow()) {
                damage = (int) Math.min(target.level().random.nextInt(damage / 2 + 2) + (long) damage, Integer.MAX_VALUE);
            }

            Entity owner = bullet.getOwner();
            DamageSource damageSource = TinkerDamageTypes.source(projectile.level().registryAccess(),
                    TinkerDamageTypes.MELEE_ARROW, projectile, attacker);
            if (attacker != null) {
                attacker.setLastHurtMob(target);
            }

            int remainingFire = target.getRemainingFireTicks();
            if (bullet.isOnFire()) {
                target.setSecondsOnFire(5);
            }

            Level level = bullet.level();
            if (target.hurt(damageSource, (float) damage)) {

                if (bullet.getPierceLevel() <= 0) {
                    bullet.discard();
                }
            } else {
                target.setRemainingFireTicks(remainingFire);
            }

            cir.setReturnValue(true);
        }
    }
}
