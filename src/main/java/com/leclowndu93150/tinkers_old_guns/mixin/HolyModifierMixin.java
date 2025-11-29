package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.tools.modifiers.traits.ranged.HolyModifier;

import javax.annotation.Nullable;

/**
 * Mixin to make HolyModifier (extra damage to undead) work with BulletProjectile.
 * The original method only applies bonus damage when projectile is AbstractArrow.
 */
@Mixin(value = HolyModifier.class, remap = false)
public class HolyModifierMixin {

    @Inject(method = "onProjectileHitEntity", at = @At("RETURN"), cancellable = true)
    private void onProjectileHitEntityBullet(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier,
                                              Projectile projectile, EntityHitResult hit,
                                              @Nullable LivingEntity attacker, @Nullable LivingEntity target,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (target != null && target.getMobType() == MobType.UNDEAD &&
            !(projectile instanceof AbstractArrow) && projectile instanceof BulletProjectile bullet) {
            bullet.setDamage(bullet.getDamage() + modifier.getLevel() / 2.0);
        }
    }
}
