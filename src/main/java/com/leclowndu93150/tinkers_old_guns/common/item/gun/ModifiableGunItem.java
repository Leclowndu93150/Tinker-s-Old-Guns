package com.leclowndu93150.tinkers_old_guns.common.item.gun;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import com.leclowndu93150.tinkers_old_guns.common.util.GunAmmoHelper;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import com.zach2039.oldguns.api.ammo.FirearmAmmo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;
import java.util.function.Predicate;

public abstract class ModifiableGunItem extends ModifiableLauncherItem {
    protected final AmmoSize ammoSize;

    /** Base cooldown after reloading in ticks (2 seconds) */
    private static final int BASE_RELOAD_COOLDOWN_TICKS = 40;

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

        int currentAmmo = GunAmmoHelper.getCurrentAmmoCount(gun);

        // Only allow reload when gun is completely empty
        if (currentAmmo == 0) {
            ItemStack ammoStack = findAmmo(player, gun, tool);
            if (!ammoStack.isEmpty()) {
                int loaded = reloadAll(gun, ammoStack, player, tool);
                if (loaded > 0) {
                    // Apply post-reload cooldown
                    player.getCooldowns().addCooldown(this, getPostReloadCooldown(tool));
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
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        } else {
            fired = true;
        }

        if (fired) {
            player.getCooldowns().addCooldown(this, getReloadCooldown(tool));
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
     * @return number of ammo loaded
     */
    public int reloadAll(ItemStack gun, ItemStack ammo, Player player, ToolStack tool) {
        if (!GunAmmoHelper.isValidAmmo(gun, ammo, ammoSize, tool)) {
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
                if (!player.getAbilities().instabuild) {
                    ammo.shrink(1);
                }
            } else {
                break;
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

    public AmmoSize getAmmoSize() {
        return ammoSize;
    }

    private ItemStack findAmmo(Player player, ItemStack gun, ToolStack tool) {
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
}
