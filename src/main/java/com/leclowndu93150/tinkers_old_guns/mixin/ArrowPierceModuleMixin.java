package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.tools.modules.ranged.common.ArrowPierceModule;

import javax.annotation.Nullable;

@Mixin(value = ArrowPierceModule.class, remap = false)
public class ArrowPierceModuleMixin {

    @Inject(method = "onProjectileShoot", at = @At("TAIL"))
    private void onProjectileShootBullet(IToolStackView tool, ModifierEntry modifier,
                                          @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile,
                                          @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary,
                                          CallbackInfo ci) {
        ArrowPierceModule self = (ArrowPierceModule) (Object) this;
        if (arrow == null && self.condition().matches(tool, modifier) && projectile instanceof BulletProjectile bullet) {
            int pierceAmount = self.amount().compute(modifier.getEffectiveLevel());
            if (pierceAmount > 0) {
                bullet.setPierceLevel((byte) pierceAmount);
            }
        }
    }
}
