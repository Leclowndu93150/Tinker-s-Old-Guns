package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.tools.modifiers.upgrades.ranged.ImpalingModifier;

import javax.annotation.Nullable;

/**
 * Mixin to make ImpalingModifier (arrow pierce) work with BulletProjectile.
 * The original method only applies pierce level to AbstractArrow.
 */
@Mixin(value = ImpalingModifier.class, remap = false)
public class ImpalingModifierMixin {

    @Inject(method = "onProjectileLaunch", at = @At("TAIL"))
    private void onProjectileLaunchBullet(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter,
                                          Projectile projectile, @Nullable AbstractArrow arrow,
                                          ModDataNBT persistentData, boolean primary, CallbackInfo ci) {
        if (arrow == null && projectile instanceof BulletProjectile bullet) {
            bullet.setPierceLevel((byte) modifier.getLevel());
        }
    }
}
