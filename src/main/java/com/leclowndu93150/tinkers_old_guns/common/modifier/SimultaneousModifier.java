package com.leclowndu93150.tinkers_old_guns.common.modifier;

import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

public class SimultaneousModifier extends Modifier implements ValidateModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        super.registerHooks(builder);
        builder.addHook(this, ModifierHooks.VALIDATE);
    }

    @Nullable
    @Override
    public Component validate(IToolStackView tool, ModifierEntry modifier) {
        if (tool.getModifierLevel(TinkersGunModifiers.EXTRA_BARREL.getId()) == 0) {
            return Component.translatable("modifier.tinkers_old_guns.simultaneous.requirement");
        }
        return null;
    }
}
