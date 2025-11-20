package com.leclowndu93150.tinkers_old_guns.common.modifier;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;

public class ScattershotModifier extends Modifier implements ProjectileLaunchModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        super.registerHooks(builder);
        builder.addHook(this, ModifierHooks.PROJECTILE_LAUNCH);
    }

    @Override
    public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
        if (projectile instanceof BulletProjectile bullet) {
            Vec3 motion = projectile.getDeltaMovement();
            
            double spreadAngle = 0.2;
            double yaw = Math.atan2(motion.z, motion.x);
            double pitch = Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z));
            
            for (int i = 0; i < 2; i++) {
                double randomYaw = yaw + (shooter.getRandom().nextDouble() - 0.5) * spreadAngle;
                double randomPitch = pitch + (shooter.getRandom().nextDouble() - 0.5) * spreadAngle;
                
                double motionX = Math.cos(randomPitch) * Math.cos(randomYaw);
                double motionY = Math.sin(randomPitch);
                double motionZ = Math.cos(randomPitch) * Math.sin(randomYaw);
                
                double speed = motion.length();
                Vec3 newMotion = new Vec3(motionX, motionY, motionZ).normalize().scale(speed);
                
                BulletProjectile extraBullet = new BulletProjectile(shooter.level(), shooter);
                extraBullet.setPos(bullet.getX(), bullet.getY(), bullet.getZ());
                extraBullet.setDeltaMovement(newMotion);
                extraBullet.setDamage(bullet.getDamage() * 0.6f);
                
                shooter.level().addFreshEntity(extraBullet);
            }
        }
    }

}
