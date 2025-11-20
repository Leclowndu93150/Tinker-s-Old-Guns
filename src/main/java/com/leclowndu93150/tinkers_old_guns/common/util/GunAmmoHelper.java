package com.leclowndu93150.tinkers_old_guns.common.util;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import com.leclowndu93150.tinkers_old_guns.common.TinkersGunStats;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableGunItem;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import com.zach2039.oldguns.api.ammo.Ammo;
import com.zach2039.oldguns.api.ammo.FirearmAmmo;
import com.zach2039.oldguns.api.firearm.util.FirearmNBTHelper;
import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GunAmmoHelper {
    private static final float SCATTER_DAMAGE_MULTIPLIER = 0.6f;
    private static final float BASE_SCATTER_SPREAD = 6.0f;

    private GunAmmoHelper() {}

    public static boolean isValidAmmo(ItemStack gun, ItemStack ammo, AmmoSize requiredSize) {
        if (!(gun.getItem() instanceof ModifiableGunItem) || !(ammo.getItem() instanceof FirearmAmmo)) {
            return false;
        }
        Optional<AmmoSize> ammoSize = AmmoSize.fromStack(ammo);
        return ammoSize.map(size -> size == requiredSize).orElse(false);
    }

    public static int getAmmoCapacity(IToolStackView tool) {
        int capacity = 1;
        if (tool.getItem() instanceof ModifiableGunItem gunItem) {
            capacity = gunItem.getBaseAmmoCapacity();
        }
        int extra = tool.getModifierLevel(TinkersGunModifiers.EXTRA_BARREL.getId());
        if (extra > 0) {
            capacity += extra;
        }
        return Math.max(1, capacity);
    }

    public static int getCurrentAmmoCount(ItemStack gun) {
        return FirearmNBTHelper.peekNBTTagAmmoCount(gun);
    }

    public static boolean loadAmmo(ItemStack gun, ItemStack ammo) {
        ToolStack tool = ToolStack.from(gun);
        int capacity = getAmmoCapacity(tool);
        if (FirearmNBTHelper.peekNBTTagAmmoCount(gun) >= capacity) {
            return false;
        }
        FirearmNBTHelper.pushNBTTagAmmo(gun, ammo.copy());
        return true;
    }

    /**
     * Fires projectiles from the provided gun/tool combination.
     * @return number of ammo items consumed
     */
    public static int fireProjectiles(ToolStack tool, ItemStack gun, Level level, LivingEntity shooter) {
        int available = getCurrentAmmoCount(gun);
        if (available == 0) {
            return 0;
        }

        int scatterLevel = tool.getModifierLevel(TinkersGunModifiers.SCATTERSHOT.getId());
        boolean fireAll = tool.getModifierLevel(TinkersGunModifiers.SIMULTANEOUS.getId()) > 0;
        int shotsToFire = fireAll ? available : 1;
        int firedShots = 0;

        float velocity = ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.VELOCITY);
        float damageModifier = ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.PROJECTILE_DAMAGE);
        float waterInertia = ConditionalStatModifierHook.getModifiedStat(tool, shooter, TinkersGunStats.WATER_INERTIA);
        float baseInaccuracy = ModifierUtil.getInaccuracy(tool, shooter);
        float scatterAngle = getScatterAngle(scatterLevel);
        ModifierNBT modifiers = tool.getModifiers();

        for (int shotIndex = 0; shotIndex < shotsToFire; shotIndex++) {
            ItemStack ammoStack = FirearmNBTHelper.popNBTTagAmmo(gun);
            if (ammoStack.isEmpty() || !(ammoStack.getItem() instanceof FirearmAmmo firearmAmmo)) {
                break;
            }
            firedShots++;

            Ammo ammoData = ammoStack.getItem() instanceof Ammo ammo ? ammo : null;
            float effectiveRange = ammoData != null ? ammoData.getProjectileEffectiveRange() : 16f;
            float deviationModifier = ammoData != null ? ammoData.getProjectileDeviationModifier() : 1f;
            List<BulletProjectile> projectiles = firearmAmmo.createProjectiles(level, ammoStack, shooter);
            projectiles = applyScattershot(shooter, projectiles, scatterLevel);

            float inaccuracy = baseInaccuracy * deviationModifier;
            for (int projectileIndex = 0; projectileIndex < projectiles.size(); projectileIndex++) {
                BulletProjectile projectile = projectiles.get(projectileIndex);
                boolean primary = projectileIndex == 0 && shotIndex == 0;
                launchProjectile(tool, ammoStack, projectile, shooter, velocity, inaccuracy, effectiveRange * velocity, scatterAngle, damageModifier, waterInertia, modifiers, primary);
                level.addFreshEntity(projectile);
            }
        }

        if (firedShots > 0) {
            InteractionHand hand = shooter.getUsedItemHand() != null ? shooter.getUsedItemHand() : InteractionHand.MAIN_HAND;
            ToolDamageUtil.damageAnimated(tool, firedShots, shooter, hand);
            FirearmNBTHelper.refreshFirearmCondition(gun);
        }

        return firedShots;
    }

    private static void launchProjectile(IToolStackView tool, ItemStack ammoStack, BulletProjectile projectile, LivingEntity shooter, float velocity, float inaccuracy, float effectiveRange, float scatterAngle, float damageModifier, float waterInertia, ModifierNBT modifiers, boolean primary) {
        projectile.setEffectiveRange(effectiveRange);
        projectile.setLaunchLocation(shooter.position());

        RandomSource random = shooter.getRandom();
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        if (scatterAngle > 0f) {
            yaw += (random.nextFloat() - 0.5f) * scatterAngle;
            pitch += (random.nextFloat() - 0.5f) * scatterAngle * 0.5f;
        }

        projectile.shootFromRotation(shooter, pitch, yaw, 0.0F, velocity, inaccuracy);
        if (waterInertia != 0.6f) {
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(waterInertia / 0.6f));
        }
        projectile.setDamage(projectile.getDamage() * damageModifier);

        projectile.getCapability(EntityModifierCapability.CAPABILITY).ifPresent(cap -> {
            if (cap.getModifiers().isEmpty()) {
                cap.setModifiers(modifiers);
            } else {
                cap.setModifiers(ModifierNBT.builder().add(cap.getModifiers()).add(modifiers).build());
            }
        });
        ModDataNBT projectileData = PersistentDataCapability.getOrWarn(projectile);
        for (ModifierEntry entry : modifiers.getModifiers()) {
            entry.getHook(ModifierHooks.PROJECTILE_LAUNCH).onProjectileLaunch(tool, entry, shooter, ammoStack, projectile, null, projectileData, primary);
        }
    }

    private static List<BulletProjectile> applyScattershot(LivingEntity shooter, List<BulletProjectile> projectiles, int scatterLevel) {
        if (scatterLevel <= 0) {
            return projectiles;
        }
        List<BulletProjectile> expanded = new ArrayList<>(projectiles.size() * (1 + scatterLevel * 2));
        int extraPerProjectile = scatterLevel * 2;
        for (BulletProjectile projectile : projectiles) {
            projectile.setDamage(projectile.getDamage() * SCATTER_DAMAGE_MULTIPLIER);
            expanded.add(projectile);
            for (int i = 0; i < extraPerProjectile; i++) {
                expanded.add(cloneProjectile(shooter, projectile));
            }
        }
        return expanded;
    }

    private static BulletProjectile cloneProjectile(LivingEntity shooter, BulletProjectile projectile) {
        BulletProjectile clone = new BulletProjectile(shooter.level(), shooter);
        clone.setProjectileType(projectile.getProjectileType());
        clone.setProjectileSize(projectile.getProjectileSize());
        clone.setEffectiveRange(projectile.getEffectiveRange());
        clone.setDamage(projectile.getDamage());
        clone.setBypassArmorPercentage(projectile.getBypassArmorPercentage());
        clone.setCritArrow(projectile.isCritArrow());
        clone.setPierceLevel(projectile.getPierceLevel());
        clone.setEffectStrength(projectile.getEffectStrength());
        clone.setEffectTicks(projectile.getEffectTicks());
        clone.pickup = projectile.pickup;
        return clone;
    }

    private static float getScatterAngle(int scatterLevel) {
        if (scatterLevel <= 0) {
            return 0f;
        }
        return BASE_SCATTER_SPREAD + (scatterLevel - 1) * 3f;
    }

    public static List<Component> getAmmoTooltip(ToolStack tool, ItemStack stack) {
        List<Component> tooltip = new ArrayList<>();
        int current = getCurrentAmmoCount(stack);
        int capacity = getAmmoCapacity(tool);
        tooltip.add(Component.translatable("tinkers_old_guns.tooltip.ammo", current, capacity).withStyle(ChatFormatting.GRAY));

        if (current > 0) {
            ItemStack ammoStack = FirearmNBTHelper.peekNBTTagAmmo(stack);
            if (!ammoStack.isEmpty()) {
                tooltip.add(Component.translatable("tinkers_old_guns.tooltip.loaded", ammoStack.getHoverName()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return tooltip;
    }
}
