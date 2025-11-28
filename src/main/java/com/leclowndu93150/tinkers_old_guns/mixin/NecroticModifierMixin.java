package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.tools.modifiers.traits.melee.NecroticModifier;

import javax.annotation.Nullable;

/**
 * Mixin to make NecroticModifier (lifesteal on crit) work with BulletProjectile.
 * The original method only applies lifesteal when projectile is a critical AbstractArrow.
 * For bullets, we apply lifesteal when the bullet has critical flag set.
 */
@Mixin(value = NecroticModifier.class, remap = false)
public class NecroticModifierMixin {

    @Inject(method = "onProjectileHitEntity", at = @At("RETURN"))
    private void onProjectileHitEntityBullet(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier,
                                              Projectile projectile, EntityHitResult hit,
                                              @Nullable LivingEntity attacker, @Nullable LivingEntity target,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (target != null && attacker != null) {
            float percent = 0.05f * modifier.getLevel();
            if (percent > 0) {
                if (!(projectile instanceof AbstractArrow) && projectile instanceof BulletProjectile bullet && bullet.isCritArrow()) {
                    float healAmount = (float) (percent * Math.min(target.getHealth(), bullet.getDamage() * bullet.getDeltaMovement().length()));
                    attacker.heal(healAmount);
                    attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                            Sounds.NECROTIC_HEAL.getSound(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
    }
}
