package com.leclowndu93150.tinkers_old_guns.common.modifier;

import com.leclowndu93150.tinkers_old_guns.common.TinkersGunStats;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class SubaquaticModifier extends Modifier implements ConditionalStatModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        super.registerHooks(builder);
        builder.addHook(this, ModifierHooks.CONDITIONAL_STAT);
    }
    
    @Override
    public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
        if (stat == TinkersGunStats.WATER_INERTIA && living != null && living.isInWater()) {
            return baseValue * 0.5f;
        }
        if (stat == ToolStats.VELOCITY && living != null && living.isInWater()) {
            return baseValue * 1.2f;
        }
        return baseValue;
    }

}
