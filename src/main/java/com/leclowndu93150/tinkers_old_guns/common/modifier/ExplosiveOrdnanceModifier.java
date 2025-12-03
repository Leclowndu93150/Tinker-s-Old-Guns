package com.leclowndu93150.tinkers_old_guns.common.modifier;

import com.leclowndu93150.tinkers_old_guns.config.TOGConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import javax.annotation.Nullable;

/**
 * Explosive Ordnance modifier - creates an explosion on projectile impact.
 * Stacks up to 3 times for bigger explosions.
 */
public class ExplosiveOrdnanceModifier extends Modifier implements ProjectileHitModifierHook {
    private static final float BASE_EXPLOSION_RADIUS = 1.0f;
    private static final float RADIUS_PER_LEVEL = 0.5f;

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT);
    }

    @Override
    public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        if (createExplosion(projectile, modifier.getLevel())) {
            // Discard projectile after explosion to prevent repeated explosions
            projectile.discard();
        }
        return false;
    }

    @Override
    public void onProjectileHitBlock(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, BlockHitResult hit, @Nullable LivingEntity attacker) {
        if (createExplosion(projectile, modifier.getLevel())) {
            // Discard projectile after explosion to prevent repeated explosions
            projectile.discard();
        }
    }

    /**
     * Creates an explosion at the projectile's position.
     * @return true if explosion was created, false if projectile was already removed
     */
    private boolean createExplosion(Projectile projectile, int level) {
        Level world = projectile.level();
        // Don't explode if already removed or on client side
        if (world.isClientSide || projectile.isRemoved()) {
            return false;
        }

        Vec3 pos = projectile.position();
        float radius = BASE_EXPLOSION_RADIUS + (RADIUS_PER_LEVEL * (level - 1));

        // Get explosion interaction mode from config
        Level.ExplosionInteraction interaction = TOGConfig.COMMON.explosiveOrdnanceBlockInteraction.get();

        world.explode(
            projectile.getOwner(),  // Source entity
            pos.x, pos.y, pos.z,    // Position
            radius,                  // Explosion radius
            false,                   // No fire
            interaction              // Configurable block destruction
        );

        return true;
    }
}
