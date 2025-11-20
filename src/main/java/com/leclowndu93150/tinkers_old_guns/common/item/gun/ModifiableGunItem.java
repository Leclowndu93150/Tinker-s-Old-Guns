package com.leclowndu93150.tinkers_old_guns.common.item.gun;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import com.leclowndu93150.tinkers_old_guns.common.util.GunAmmoHelper;
import com.zach2039.oldguns.api.ammo.FirearmAmmo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;
import java.util.function.Predicate;

public abstract class ModifiableGunItem extends ModifiableLauncherItem {
    protected final AmmoSize ammoSize;

    public ModifiableGunItem(Properties properties, ToolDefinition toolDefinition, AmmoSize ammoSize) {
        super(properties, toolDefinition);
        this.ammoSize = ammoSize;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> stack.getItem() instanceof FirearmAmmo &&
                GunAmmoHelper.isValidAmmo(ItemStack.EMPTY, stack, ammoSize);
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
        
        if (GunAmmoHelper.getCurrentAmmoCount(gun) > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(gun);
        } else {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("tinkers_old_guns.gun.no_ammo"), true);
            }
            return InteractionResultHolder.fail(gun);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        ToolStack tool = ToolStack.from(stack);
        
        if (GunAmmoHelper.getCurrentAmmoCount(stack) == 0) {
            return;
        }

        if (!level.isClientSide) {
            GunAmmoHelper.fireProjectiles(tool, level, player);
            
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        player.getCooldowns().addCooldown(this, getReloadCooldown(tool));
    }

    protected int getReloadCooldown(ToolStack tool) {
        float drawSpeed = tool.getStats().get(ToolStats.DRAW_SPEED);
        return Math.max(1, (int) (20 / drawSpeed * getReloadSpeedMultiplier()));
    }

    public boolean reload(ItemStack gun, ItemStack ammo, Player player) {
        if (!GunAmmoHelper.isValidAmmo(gun, ammo, ammoSize)) {
            return false;
        }

        if (GunAmmoHelper.loadAmmo(gun, ammo)) {
            ammo.shrink(1);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        ToolStack tool = ToolStack.from(stack);
        if (!tool.isBroken()) {
            List<String> ammoTooltip = GunAmmoHelper.getAmmoTooltip(tool);
            for (String line : ammoTooltip) {
                tooltip.add(Component.literal(line));
            }
            
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tinkers_old_guns.gun.ammo_type." + ammoSize.name().toLowerCase())
                    .withStyle(style -> style.withColor(0x7F7F7F)));
        }
    }

    protected abstract int getBaseAmmoCapacity();
    protected abstract float getReloadSpeedMultiplier();

    public AmmoSize getAmmoSize() {
        return ammoSize;
    }
}
