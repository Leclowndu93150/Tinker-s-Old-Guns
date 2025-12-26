package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.combat.ConditionalPowerModule;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import javax.annotation.Nullable;

@Mixin(value = ConditionalPowerModule.class, remap = false)
public class ConditionalPowerModuleMixin {

    @Inject(method = "onProjectileHitEntity(Lslimeknights/tconstruct/library/tools/nbt/ModifierNBT;Lslimeknights/tconstruct/library/tools/nbt/ModDataNBT;Lslimeknights/tconstruct/library/modifiers/ModifierEntry;Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"))
    private void onProjectileHitEntityBullet(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier,
                                              Projectile projectile, EntityHitResult hit,
                                              @Nullable LivingEntity attacker, @Nullable LivingEntity target,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (!(projectile instanceof AbstractArrow) && projectile instanceof BulletProjectile bullet) {
            ConditionalPowerModule self = (ConditionalPowerModule) (Object) this;
            if (!self.modifierLevel().test(modifier.getLevel())) {
                return;
            }
            if (!self.target().matches(target) || !self.holder().matches(attacker)) {
                return;
            }
            float currentDamage = (float) bullet.getDamage();
            float bonusDamage = modifier.getEffectiveLevel() * 0.75f;
            bullet.setDamage(currentDamage + bonusDamage);
        }
    }
}
