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
import net.minecraft.core.registries.BuiltInRegistries;
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
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GunAmmoHelper {
    private static final float SCATTER_DAMAGE_MULTIPLIER = 0.2f;
    private static final float BASE_SCATTER_SPREAD = 6.0f;
    /** Angle between multishot projectiles in degrees (matches TC bow behavior) */
    private static final float MULTISHOT_ANGLE_OFFSET = 10.0f;

    private GunAmmoHelper() {}

    /**
     * Checks if the ammo is buckshot by examining its registry name.
     */
    public static boolean isBuckshot(ItemStack ammo) {
        return BuiltInRegistries.ITEM.getKey(ammo.getItem()).getPath().contains("buckshot");
    }

    /**
     * Validates ammo without considering tool modifiers (for backwards compatibility).
     */
    public static boolean isValidAmmo(ItemStack gun, ItemStack ammo, AmmoSize requiredSize) {
        return isValidAmmo(gun, ammo, requiredSize, null);
    }

    /**
     * Validates ammo considering tool modifiers like Scattershot.
     * If the tool has Scattershot modifier, only buckshot ammo is allowed.
     */
    public static boolean isValidAmmo(ItemStack gun, ItemStack ammo, AmmoSize requiredSize, @Nullable IToolStackView tool) {
        if (!(gun.getItem() instanceof ModifiableGunItem) || !(ammo.getItem() instanceof FirearmAmmo)) {
            return false;
        }
        Optional<AmmoSize> ammoSize = AmmoSize.fromStack(ammo);
        if (!ammoSize.map(size -> size == requiredSize).orElse(false)) {
            return false;
        }

        // If tool has Scattershot modifier, require buckshot ammo
        if (tool != null && tool.getModifierLevel(TinkersGunModifiers.SCATTERSHOT.getId()) > 0) {
            if (!isBuckshot(ammo)) {
                return false;
            }
        }

        return true;
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
     * Supports Multishot modifier (fires additional projectiles in a spread pattern).
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

        // Multishot: fires additional projectiles in a spread (1 + 2*level projectiles)
        int multishotLevel = tool.getModifierLevel(TinkerModifiers.multishot.getId());
        int multishotCount = 1 + (2 * multishotLevel);
        float multishotStartAngle = getMultishotStartAngle(multishotCount);

        // Use gun-specific base velocity (matches Old Guns Flintlock values)
        float baseVelocity = (tool.getItem() instanceof ModifiableGunItem gunItem) ? gunItem.getBaseVelocity() : 3.5f;
        float velocity = baseVelocity * ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.VELOCITY);
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
            float inaccuracy = baseInaccuracy * deviationModifier;

            // Fire multiple projectiles for multishot
            int primaryMultishotIndex = multishotCount / 2; // Middle projectile is primary
            for (int multishotIndex = 0; multishotIndex < multishotCount; multishotIndex++) {
                // Create projectiles for this multishot instance
                // If Scattershot is active, create exactly 1 base projectile and let Scattershot handle the count
                // Otherwise use ammo's normal projectile count (buckshot fires multiple)
                List<BulletProjectile> projectiles;
                if (scatterLevel > 0) {
                    projectiles = createSingleProjectile(level, ammoStack, firearmAmmo, shooter);
                } else {
                    projectiles = firearmAmmo.createProjectiles(level, ammoStack, shooter);
                }
                projectiles = applyScattershot(shooter, projectiles, scatterLevel);

                // Calculate multishot angle offset (spread evenly around center)
                float multishotAngle = multishotStartAngle + (MULTISHOT_ANGLE_OFFSET * multishotIndex);

                for (int projectileIndex = 0; projectileIndex < projectiles.size(); projectileIndex++) {
                    BulletProjectile projectile = projectiles.get(projectileIndex);
                    // Primary projectile is the first projectile of the middle multishot of the first shot
                    boolean primary = projectileIndex == 0 && multishotIndex == primaryMultishotIndex && shotIndex == 0;
                    launchProjectile(tool, ammoStack, projectile, shooter, velocity, inaccuracy,
                            effectiveRange * velocity, scatterAngle, waterInertia,
                            modifiers, primary, multishotAngle);
                    level.addFreshEntity(projectile);
                }
            }
        }

        if (firedShots > 0) {
            InteractionHand hand = shooter.getUsedItemHand() != null ? shooter.getUsedItemHand() : InteractionHand.MAIN_HAND;
            ToolDamageUtil.damageAnimated(tool, firedShots, shooter, hand);
            FirearmNBTHelper.refreshFirearmCondition(gun);
        }

        return firedShots;
    }

    /**
     * Gets the starting angle for multishot spread (to center the projectiles).
     * For 3 projectiles: -10°, 0°, +10°
     * For 5 projectiles: -20°, -10°, 0°, +10°, +20°
     */
    private static float getMultishotStartAngle(int count) {
        return -MULTISHOT_ANGLE_OFFSET * (count - 1) / 2.0f;
    }

    /**
     * Launches a projectile with multishot angle offset support.
     * @param multishotAngle The horizontal angle offset for multishot spread (0 for no offset)
     */
    private static void launchProjectile(IToolStackView tool, ItemStack ammoStack, BulletProjectile projectile,
                                         LivingEntity shooter, float velocity, float inaccuracy, float effectiveRange,
                                         float scatterAngle, float waterInertia,
                                         ModifierNBT modifiers, boolean primary, float multishotAngle) {
        projectile.setEffectiveRange(effectiveRange);
        projectile.setLaunchLocation(shooter.position());

        RandomSource random = shooter.getRandom();
        float yaw = shooter.getYRot() + multishotAngle; // Apply multishot horizontal spread
        float pitch = shooter.getXRot();
        if (scatterAngle > 0f) {
            yaw += (random.nextFloat() - 0.5f) * scatterAngle;
            pitch += (random.nextFloat() - 0.5f) * scatterAngle * 0.5f;
        }

        projectile.shootFromRotation(shooter, pitch, yaw, 0.0F, velocity, inaccuracy);
        if (waterInertia != 0.6f) {
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(waterInertia / 0.6f));
        }
        // Apply TC tool damage additively (like TC bows) rather than multiplicatively
        // First add the tool's base PROJECTILE_DAMAGE stat, then apply modifiers (like Power) on top
        float ammoDamage = (float) projectile.getDamage();
        float baseDamage = ammoDamage + tool.getStats().get(ToolStats.PROJECTILE_DAMAGE);
        float finalDamage = ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.PROJECTILE_DAMAGE, baseDamage);
        // Amplify Power modifier effect for guns: TC Power gives +1/level, we want +3/level total
        // So add an extra +2 per Power level
        int powerLevel = tool.getModifierLevel(ModifierIds.power);
        if (powerLevel > 0) {
            finalDamage += powerLevel * 2.0f;
        }
        projectile.setDamage(finalDamage);

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

    /**
     * Creates exactly one projectile from the ammo, ignoring the ammo's projectile count.
     * Used by Scattershot to have full control over projectile count.
     */
    private static List<BulletProjectile> createSingleProjectile(Level level, ItemStack ammoStack, FirearmAmmo firearmAmmo, LivingEntity shooter) {
        // Create all projectiles from the ammo, but only keep the first one
        List<BulletProjectile> allProjectiles = firearmAmmo.createProjectiles(level, ammoStack, shooter);
        if (allProjectiles.isEmpty()) {
            return allProjectiles;
        }
        // Return only the first projectile
        List<BulletProjectile> single = new ArrayList<>(1);
        single.add(allProjectiles.get(0));
        return single;
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
