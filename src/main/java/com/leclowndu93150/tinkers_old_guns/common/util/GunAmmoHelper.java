package com.leclowndu93150.tinkers_old_guns.common.util;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableGunItem;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import com.zach2039.oldguns.api.ammo.FirearmAmmo;
import com.zach2039.oldguns.api.firearm.util.FirearmNBTHelper;
import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.List;

public class GunAmmoHelper {

    public static boolean isValidAmmo(ItemStack gun, ItemStack ammo, AmmoSize requiredSize) {
        if (!(gun.getItem() instanceof ModifiableGunItem)) {
            return false;
        }
        
        if (!(ammo.getItem() instanceof FirearmAmmo)) {
            return false;
        }
        
        return true;
    }

    public static int getAmmoCapacity(ToolStack tool) {
        int baseCapacity = 1;
        
        ModifierEntry extraBarrel = tool.getModifier(TinkersGunModifiers.EXTRA_BARREL.get());
        if (extraBarrel != null) {
            baseCapacity += extraBarrel.getLevel();
        }
        
        return baseCapacity;
    }

    public static int getCurrentAmmoCount(ItemStack gun) {
        return FirearmNBTHelper.peekNBTTagAmmoCount(gun);
    }

    public static boolean loadAmmo(ItemStack gun, ItemStack ammo) {
        ToolStack tool = ToolStack.from(gun);
        int capacity = getAmmoCapacity(tool);
        int current = getCurrentAmmoCount(gun);
        
        if (current >= capacity) {
            return false;
        }
        
        FirearmNBTHelper.pushNBTTagAmmo(gun, ammo.copy());
        
        return true;
    }

    public static void fireProjectiles(ToolStack tool, Level level, LivingEntity shooter) {
        ItemStack gun = tool.createStack();
        
        if (getCurrentAmmoCount(gun) == 0) {
            return;
        }
        
        ModifierEntry simultaneous = tool.getModifier(TinkersGunModifiers.SIMULTANEOUS.get());
        boolean fireAll = simultaneous != null;
        
        int shotsToFire = fireAll ? getCurrentAmmoCount(gun) : 1;
        
        for (int i = 0; i < shotsToFire; i++) {
            ItemStack ammoStack = FirearmNBTHelper.popNBTTagAmmo(gun);
            if (ammoStack.isEmpty()) break;
            
            if (ammoStack.getItem() instanceof FirearmAmmo) {
                FirearmAmmo ammo = (FirearmAmmo) ammoStack.getItem();
                List<BulletProjectile> projectiles = ammo.createProjectiles(level, ammoStack, shooter);
                
                for (BulletProjectile projectile : projectiles) {
                    applyToolStatsToProjectile(tool, projectile);
                    level.addFreshEntity(projectile);
                }
            }
        }
        
        tool.setDamage(tool.getDamage() + shotsToFire);
    }

    public static void applyToolStatsToProjectile(ToolStack tool, BulletProjectile projectile) {
        float velocityMod = tool.getStats().get(ToolStats.VELOCITY);
        float damageMod = tool.getStats().get(ToolStats.PROJECTILE_DAMAGE);
        float accuracyMod = tool.getStats().get(ToolStats.ACCURACY);
        
        projectile.setDeltaMovement(
            projectile.getDeltaMovement().scale(velocityMod)
        );
        
        projectile.setDamage(projectile.getDamage() * damageMod);
        
        ModifierEntry scattershot = tool.getModifier(TinkersGunModifiers.SCATTERSHOT.get());
        if (scattershot != null) {
            projectile.setDamage(projectile.getDamage() * 0.6f);
        }
    }

    public static List<String> getAmmoTooltip(ToolStack tool) {
        List<String> tooltip = new ArrayList<>();
        ItemStack gun = tool.createStack();
        
        int current = getCurrentAmmoCount(gun);
        int capacity = getAmmoCapacity(tool);
        
        tooltip.add("§7Ammo: §f" + current + " / " + capacity);
        
        if (current > 0) {
            ItemStack ammoStack = FirearmNBTHelper.peekNBTTagAmmo(gun);
            if (!ammoStack.isEmpty() && ammoStack.getItem() instanceof FirearmAmmo) {
                tooltip.add("§7Loaded: §f" + ammoStack.getHoverName().getString());
            }
        }
        
        return tooltip;
    }
}
