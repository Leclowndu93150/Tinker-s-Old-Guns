package com.leclowndu93150.tinkers_old_guns.mixin;

import com.zach2039.oldguns.world.entity.BulletProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.Objects;

/**
 * Mixin to apply conditional damage modifiers (Smite, Killager, etc.) to BulletProjectile.
 * These modifiers use ConditionalMeleeDamageModule which only works for melee hits,
 * so we need to manually apply the damage bonuses when bullets hit entities.
 */
@Mixin(value = BulletProjectile.class)
public class BulletProjectileModifierMixin {

    @Unique
    private static final ModifierId SMITE_ID = new ModifierId("tconstruct", "smite");
    @Unique
    private static final ModifierId KILLAGER_ID = new ModifierId("tconstruct", "killager");
    @Unique
    private static final ModifierId ANTIAQUATIC_ID = new ModifierId("tconstruct", "antiaquatic");
    @Unique
    private static final ModifierId BANE_OF_SSSSS_ID = new ModifierId("tconstruct", "bane_of_sssss");
    @Unique
    private static final ModifierId COOLING_ID = new ModifierId("tconstruct", "cooling");
    @Unique
    private static final ModifierId PIERCE_ID = new ModifierId("tconstruct", "pierce");
    @Unique
    private static final ModifierId SUBAQUATIC_ID = new ModifierId("tinkers_old_guns", "subaquatic");

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void applyConditionalDamageModifiers(EntityHitResult result, CallbackInfo ci) {
        BulletProjectile bullet = (BulletProjectile) (Object) this;
        Entity target = result.getEntity();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        bullet.getCapability(EntityModifierCapability.CAPABILITY).ifPresent(cap -> {
            ModifierNBT modifiers = cap.getModifiers();
            if (modifiers.isEmpty()) {
                return;
            }

            double bonusDamage = 0;

            // Smite: +2.0 damage per level vs undead
            int smiteLevel = modifiers.getLevel(SMITE_ID);
            if (smiteLevel > 0 && livingTarget.getMobType() == MobType.UNDEAD) {
                bonusDamage += 2.0 * smiteLevel;
            }

            // Killager: +2.0 damage per level vs illagers and villagers
            int killagerLevel = modifiers.getLevel(KILLAGER_ID);
            if (killagerLevel > 0 && isIllagerOrVillager(livingTarget)) {
                bonusDamage += 2.0 * killagerLevel;
            }

            // Antiaquatic: +2.0 damage per level vs water mobs
            int antiaquaticLevel = modifiers.getLevel(ANTIAQUATIC_ID);
            if (antiaquaticLevel > 0 && livingTarget.getMobType() == MobType.WATER) {
                bonusDamage += 2.0 * antiaquaticLevel;
            }

            // Bane of Sssss: +2.0 damage per level vs arthropods and creepers, + slowness
            int baneLevel = modifiers.getLevel(BANE_OF_SSSSS_ID);
            if (baneLevel > 0 && isArthropodOrCreeper(livingTarget)) {
                bonusDamage += 2.0 * baneLevel;
                // Apply Slowness IV for 20-30 ticks (1-1.5 seconds)
                int duration = 20 + livingTarget.getRandom().nextInt(11);
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3));
            }

            // Cooling: +1.6 damage per level vs fire immune mobs
            int coolingLevel = modifiers.getLevel(COOLING_ID);
            if (coolingLevel > 0 && livingTarget.fireImmune()) {
                bonusDamage += 1.6 * coolingLevel;
            }

            // Pierce: Apply pierce effect (armor reduction)
            int pierceLevel = modifiers.getLevel(PIERCE_ID);
            if (pierceLevel > 0) {
                // Apply pierce effect for 2 ticks per level
                livingTarget.addEffect(new MobEffectInstance(
                        Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("tconstruct", "pierce"))),
                    2, pierceLevel - 1));
            }

            // Apply bonus damage
            if (bonusDamage > 0) {
                bullet.setDamage(bullet.getDamage() + bonusDamage);
            }
        });
    }

    @Unique
    private static boolean isIllagerOrVillager(LivingEntity entity) {
        if (entity.getMobType() == MobType.ILLAGER) {
            return true;
        }
        // Check for villager entity type (villagers don't have a special MobType)
        EntityType<?> type = entity.getType();
        return type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER;
    }

    @Unique
    private static boolean isArthropodOrCreeper(LivingEntity entity) {
        // Check for arthropod mob type
        if (entity.getMobType() == MobType.ARTHROPOD) {
            return true;
        }
        return entity.getType() == EntityType.CREEPER;
    }

    /**
     * Override water inertia to return 1.0 (no slowdown) if the projectile has the Subaquatic modifier.
     * This allows bullets to travel just as far underwater as they would on land.
     */
    @Inject(method = "getWaterInertia", at = @At("HEAD"), cancellable = true, remap = false)
    private void subaquaticWaterInertia(CallbackInfoReturnable<Float> cir) {
        BulletProjectile bullet = (BulletProjectile) (Object) this;
        bullet.getCapability(EntityModifierCapability.CAPABILITY).ifPresent(cap -> {
            ModifierNBT modifiers = cap.getModifiers();
            if (modifiers.getLevel(SUBAQUATIC_ID) > 0) {
                cir.setReturnValue(1.0f);
            }
        });
    }
}
