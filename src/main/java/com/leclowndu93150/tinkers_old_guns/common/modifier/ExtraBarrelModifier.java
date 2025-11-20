package com.leclowndu93150.tinkers_old_guns.common.modifier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class ExtraBarrelModifier extends Modifier {

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        super.registerHooks(builder);
    }

    @Override
    public Component getDisplayName(int level) {
        return Component.translatable(getTranslationKey())
                .append(" ")
                .append(Component.translatable("enchantment.level." + level));
    }
}
