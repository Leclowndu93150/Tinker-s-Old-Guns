package com.leclowndu93150.tinkers_old_guns.common.item.gun;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import com.leclowndu93150.tinkers_old_guns.common.util.GunAmmoHelper;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import com.zach2039.oldguns.api.ammo.FirearmAmmo;
import com.zach2039.oldguns.init.ModItems;
import com.zach2039.oldguns.init.ModSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.List;
import java.util.function.Predicate;

public abstract class ModifiableGunItem extends ModifiableLauncherItem {
    protected final AmmoSize ammoSize;

    /** Base cooldown after reloading in ticks (4 seconds) */
    private static final int BASE_RELOAD_COOLDOWN_TICKS = 80;

    /** NBT key for per-stack cooldown tracking */
    private static final String TAG_COOLDOWN_END = "tog_cooldown_end";

    /** NBT key for marking ammo as coming from infinity modifier (uses durability instead of consuming) */
    public static final String TAG_INFINITY_AMMO = "tog_infinity";

    public ModifiableGunItem(Properties properties, ToolDefinition toolDefinition, AmmoSize ammoSize) {
        super(properties, toolDefinition);
        this.ammoSize = ammoSize;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> stack.getItem() instanceof FirearmAmmo &&
                AmmoSize.fromStack(stack).map(size -> size == ammoSize).orElse(false);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        ToolStack tool = ToolStack.from(gun);
        if (tool.isBroken()) {
            return InteractionResultHolder.fail(gun);
        }

        // Check per-stack cooldown
        if (isOnCooldown(gun, level)) {
            if (!level.isClientSide) {
                long remaining = getRemainingCooldownTicks(gun, level);
                float seconds = remaining / 20.0f;
                player.displayClientMessage(Component.translatable("tinkers_old_guns.gun.cooldown", String.format("%.1f", seconds)), true);
            }
            return InteractionResultHolder.fail(gun);
        }

        int currentAmmo = GunAmmoHelper.getCurrentAmmoCount(gun);

        // Only allow reload when gun is completely empty
        if (currentAmmo == 0) {
            ItemStack ammoStack = findAmmo(player, gun, tool);
            if (!ammoStack.isEmpty()) {
                int loaded = reloadAll(gun, ammoStack, player, tool);
                if (loaded > 0) {
                    // Apply per-stack post-reload cooldown
                    setCooldown(gun, level, getPostReloadCooldown(tool));
                    return InteractionResultHolder.sidedSuccess(gun, level.isClientSide);
                }
            }
            // No valid ammo to reload
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("tinkers_old_guns.gun.reload.failure"), true);
            }
            return InteractionResultHolder.fail(gun);
        }

        // Gun has ammo, shoot
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(gun);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken() || GunAmmoHelper.getCurrentAmmoCount(stack) == 0) {
            return;
        }

        boolean fired = false;
        if (!level.isClientSide) {
            // Check for water misfire unless Subaquatic modifier is present
            boolean hasSubaquatic = tool.getModifierLevel(TinkersGunModifiers.SUBAQUATIC.getId()) > 0;
            if (!hasSubaquatic && checkWaterMisfire(player, level)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.0f);
                player.displayClientMessage(Component.translatable("tinkers_old_guns.gun.misfire.wet"), true);
                return;
            }

            int shotsFired = GunAmmoHelper.fireProjectiles(tool, stack, level, player);
            if (shotsFired > 0) {
                fired = true;
                // Play Old Guns style firing sounds
                playFireSound(level, player);
            }
        } else {
            fired = true;
        }

        // Spawn particles on client side
        if (fired && level.isClientSide) {
            spawnFireParticles(level, player);
        }

        if (fired) {
            // Apply per-stack cooldown instead of per-item cooldown
            setCooldown(stack, level, getReloadCooldown(tool));
        }
    }

    protected int getReloadCooldown(ToolStack tool) {
        float drawSpeed = tool.getStats().get(ToolStats.DRAW_SPEED);
        return Math.max(1, (int) (20 / drawSpeed * getReloadSpeedMultiplier()));
    }

    /**
     * Gets the cooldown after reloading, scaled by DRAW_SPEED.
     * Base is 2 seconds (40 ticks), reduced by higher draw speed.
     */
    protected int getPostReloadCooldown(ToolStack tool) {
        float drawSpeed = tool.getStats().get(ToolStats.DRAW_SPEED);
        return Math.max(10, (int) (BASE_RELOAD_COOLDOWN_TICKS / drawSpeed * getReloadSpeedMultiplier()));
    }

    /**
     * Reloads all available ammo slots at once.
     * Supports infinity ammo from modifiers (uses durability instead of consuming).
     * @return number of ammo loaded
     */
    public int reloadAll(ItemStack gun, ItemStack ammo, Player player, ToolStack tool) {
        // Check if this is infinity ammo from a modifier
        boolean isInfinityAmmo = ammo.hasTag() && ammo.getTag().getBoolean(TAG_INFINITY_AMMO);

        // For infinity ammo, we don't validate against gun ammo type since the modifier handles that
        if (!isInfinityAmmo && !GunAmmoHelper.isValidAmmo(gun, ammo, ammoSize, tool)) {
            return 0;
        }

        int capacity = GunAmmoHelper.getAmmoCapacity(tool);
        int currentAmmo = GunAmmoHelper.getCurrentAmmoCount(gun);
        int slotsToFill = capacity - currentAmmo;
        int availableAmmo = player.getAbilities().instabuild ? slotsToFill : ammo.getCount();
        int toLoad = Math.min(slotsToFill, availableAmmo);

        if (toLoad <= 0) {
            return 0;
        }

        int loaded = 0;
        for (int i = 0; i < toLoad; i++) {
            if (GunAmmoHelper.loadAmmo(gun, ammo)) {
                loaded++;
                if (!player.getAbilities().instabuild && !isInfinityAmmo) {
                    ammo.shrink(1);
                }
            } else {
                break;
            }
        }

        // For infinity ammo, call the modifier's shrinkAmmo to consume durability
        if (loaded > 0 && isInfinityAmmo && !player.getAbilities().instabuild) {
            String modifierId = ammo.getTag().getString("tog_infinity_modifier");
            for (ModifierEntry entry : tool.getModifierList()) {
                if (entry.getId().toString().equals(modifierId)) {
                    BowAmmoModifierHook hook = entry.getHook(ModifierHooks.BOW_AMMO);
                    hook.shrinkAmmo(tool, entry, player, ammo, loaded);
                    break;
                }
            }
        }

        if (loaded > 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.5f, 1.2f);
        }

        return loaded;
    }

    public boolean reload(ItemStack gun, ItemStack ammo, Player player, ToolStack tool) {
        if (!GunAmmoHelper.isValidAmmo(gun, ammo, ammoSize, tool)) {
            return false;
        }

        if (GunAmmoHelper.loadAmmo(gun, ammo)) {
            if (!player.getAbilities().instabuild) {
                ammo.shrink(1);
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.5f, 1.2f);
            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        ToolStack tool = ToolStack.from(stack);
        if (!tool.isBroken()) {
            List<Component> ammoTooltip = GunAmmoHelper.getAmmoTooltip(tool, stack);
            tooltip.addAll(ammoTooltip);
            
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tinkers_old_guns.gun.ammo_type." + ammoSize.name().toLowerCase())
                    .withStyle(style -> style.withColor(0x7F7F7F)));
        }
    }

    public abstract int getBaseAmmoCapacity();
    protected abstract float getReloadSpeedMultiplier();

    /**
     * Gets the base projectile velocity for this gun type.
     * Matches Old Guns Flintlock velocities so VELOCITY=1.0 equals Old Guns speed.
     */
    public abstract float getBaseVelocity();

    public AmmoSize getAmmoSize() {
        return ammoSize;
    }

    /**
     * Finds ammo for the gun, checking TC modifier hooks first (for Crystalshot etc.),
     * then falling back to player inventory.
     *
     * For infinity modifiers (like Crystalshot), we use actual gun ammo but mark it
     * as infinite so durability is consumed instead of the ammo item.
     */
    private ItemStack findAmmo(Player player, ItemStack gun, ToolStack tool) {
        Predicate<ItemStack> ammoPredicate = getAllSupportedProjectiles();

        // First, check if any modifier provides infinite ammo (like Crystalshot/InfinityModule)
        ItemStack standardAmmo = findStandardAmmo(player, gun, tool);
        for (ModifierEntry entry : tool.getModifierList()) {
            BowAmmoModifierHook hook = entry.getHook(ModifierHooks.BOW_AMMO);
            // Pass empty standardAmmo to force infinity check (infinity modules skip if standard ammo exists)
            ItemStack modifierAmmo = hook.findAmmo(tool, entry, player, ItemStack.EMPTY, ammoPredicate);
            if (!modifierAmmo.isEmpty()) {
                // Modifier wants to provide infinite ammo - use gun-appropriate ammo instead
                // If player has standard ammo, use that; otherwise use default ammo for this gun size
                ItemStack ammoToUse = standardAmmo.isEmpty() ? getDefaultAmmoForSize() : standardAmmo.copy();
                if (!ammoToUse.isEmpty()) {
                    CompoundTag tag = ammoToUse.getOrCreateTag();
                    tag.putBoolean(TAG_INFINITY_AMMO, true);
                    // Store which modifier provided this ammo for shrinkAmmo callback
                    tag.putString("tog_infinity_modifier", entry.getId().toString());
                    return ammoToUse;
                }
            }
        }

        return standardAmmo;
    }

    /**
     * Gets a default ammo item for this gun's ammo size.
     * Used when infinity modifiers are active but player has no ammo in inventory.
     */
    private ItemStack getDefaultAmmoForSize() {
        return switch (ammoSize) {
            case SMALL -> new ItemStack(ModItems.SMALL_IRON_MUSKET_BALL.get());
            case MEDIUM -> new ItemStack(ModItems.MEDIUM_IRON_MUSKET_BALL.get());
            case LARGE -> new ItemStack(ModItems.LARGE_IRON_MUSKET_BALL.get());
        };
    }

    /**
     * Finds standard ammo from player inventory (offhand first, then main inventory).
     */
    private ItemStack findStandardAmmo(Player player, ItemStack gun, ToolStack tool) {
        ItemStack offhand = player.getOffhandItem();
        if (GunAmmoHelper.isValidAmmo(gun, offhand, ammoSize, tool)) {
            return offhand;
        }

        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (!stack.isEmpty() && GunAmmoHelper.isValidAmmo(gun, stack, ammoSize, tool)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    // ==================== Per-Stack Cooldown System ====================

    /**
     * Checks if this specific gun stack is on cooldown.
     */
    private boolean isOnCooldown(ItemStack gun, Level level) {
        CompoundTag tag = gun.getTag();
        if (tag == null || !tag.contains(TAG_COOLDOWN_END)) {
            return false;
        }
        long cooldownEnd = tag.getLong(TAG_COOLDOWN_END);
        return level.getGameTime() < cooldownEnd;
    }

    /**
     * Gets the remaining cooldown ticks for this gun stack.
     */
    private long getRemainingCooldownTicks(ItemStack gun, Level level) {
        CompoundTag tag = gun.getTag();
        if (tag == null || !tag.contains(TAG_COOLDOWN_END)) {
            return 0;
        }
        long cooldownEnd = tag.getLong(TAG_COOLDOWN_END);
        return Math.max(0, cooldownEnd - level.getGameTime());
    }

    /**
     * Sets a cooldown on this specific gun stack.
     */
    private void setCooldown(ItemStack gun, Level level, int ticks) {
        CompoundTag tag = gun.getOrCreateTag();
        tag.putLong(TAG_COOLDOWN_END, level.getGameTime() + ticks);
    }

    /**
     * Gets the remaining cooldown percentage (0.0 to 1.0) for visual display.
     */
    public float getCooldownProgress(ItemStack gun, Level level, int maxCooldownTicks) {
        CompoundTag tag = gun.getTag();
        if (tag == null || !tag.contains(TAG_COOLDOWN_END)) {
            return 0.0f;
        }
        long cooldownEnd = tag.getLong(TAG_COOLDOWN_END);
        long remaining = cooldownEnd - level.getGameTime();
        if (remaining <= 0) {
            return 0.0f;
        }
        return Math.min(1.0f, (float) remaining / maxCooldownTicks);
    }

    private boolean checkWaterMisfire(Player player, Level level) {
        if (player.isInWater()) {
            // Underwater: 90% misfire chance
            return level.random.nextFloat() < 0.9f;
        } else if (player.isInWaterOrRain()) {
            // In rain: 25% misfire chance
            return level.random.nextFloat() < 0.25f;
        }
        return false;
    }

    /**
     * Plays Old Guns style firing sounds based on ammo size.
     * Matches FirearmEffectHelper from Old Guns mod.
     */
    private void playFireSound(Level level, Player player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        java.util.Random rand = new java.util.Random();

        switch (ammoSize) {
            case SMALL -> {
                // Small firearm: just explosion sound with higher pitch
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                        1.0f, 2.5f / (rand.nextFloat() * 0.4f + 1.2f));
            }
            case MEDIUM -> {
                // Medium firearm: bullet shoot + explosion
                level.playSound(null, x, y, z, ModSoundEvents.BULLET_SHOOT.get(), SoundSource.PLAYERS,
                        25.0f, 3.0f / (rand.nextFloat() * 0.6f + 1.2f));
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                        1.0f, 1.5f / (rand.nextFloat() * 0.4f + 1.2f));
            }
            case LARGE -> {
                // Large firearm: louder bullet shoot + deeper explosion
                level.playSound(null, x, y, z, ModSoundEvents.BULLET_SHOOT.get(), SoundSource.PLAYERS,
                        50.0f, 2.2f / (rand.nextFloat() * 0.6f + 1.2f));
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                        1.0f, 1.0f / (rand.nextFloat() * 0.4f + 1.2f));
            }
        }
    }

    /**
     * Spawns Old Guns style smoke and flame particles.
     * Matches FirearmEffectHelper from Old Guns mod.
     */
    private void spawnFireParticles(Level level, Player player) {
        java.util.Random rand = new java.util.Random();

        // Particle count based on ammo size
        int particleCount = switch (ammoSize) {
            case SMALL -> 1 + rand.nextInt(2);   // 1-2 particles
            case MEDIUM -> 2 + rand.nextInt(4);  // 2-5 particles
            case LARGE -> 3 + rand.nextInt(6);   // 3-8 particles
        };

        double posX = player.getX();
        double posY = player.getY() + player.getEyeHeight();
        double posZ = player.getZ();
        float rotationYaw = player.getYRot();
        float rotationPitch = player.getXRot();

        // Calculate hand offset based on which hand is used
        InteractionHand hand = player.getUsedItemHand();
        float offset = (hand == InteractionHand.MAIN_HAND) ? 30.0f : -30.0f;

        for (int i = 0; i < particleCount; i++) {
            // Calculate distance from player (increases per iteration)
            float range = 1.2f * (0.8f * (i + 1));

            // Calculate particle position using trigonometry (matches Old Guns exactly)
            float handX = -Mth.sin((rotationYaw + offset / 1.5f) / 180.0f * (float) Math.PI)
                    * Mth.cos(rotationPitch / 180.0f * (float) Math.PI) * range;
            float handY = -Mth.sin(rotationPitch / 180.0f * (float) Math.PI) * range - 0.1f;
            float handZ = Mth.cos((rotationYaw + offset / 1.5f) / 180.0f * (float) Math.PI)
                    * Mth.cos(rotationPitch / 180.0f * (float) Math.PI) * range;

            double particleX = posX + handX;
            double particleY = posY + handY;
            double particleZ = posZ + handZ;

            // Spawn flame particle only on first iteration
            if (i == 0) {
                level.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, 0, 0, 0);
            }

            // Spawn smoke particles with random offset
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    particleX + (0.5f - rand.nextFloat()),
                    particleY + (0.5f - rand.nextFloat()),
                    particleZ + (0.5f - rand.nextFloat()),
                    0, 0, 0);

            // Spawn poof particles with random offset
            level.addParticle(ParticleTypes.POOF,
                    particleX + (0.5f - rand.nextFloat()),
                    particleY + (0.5f - rand.nextFloat()),
                    particleZ + (0.5f - rand.nextFloat()),
                    0, 0, 0);
        }
    }
}
