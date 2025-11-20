package com.leclowndu93150.tinkers_old_guns.common.item.gun;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

public class ModifiableCarbineItem extends ModifiableGunItem {

    public ModifiableCarbineItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition, AmmoSize.MEDIUM);
    }

    @Override
    public int getBaseAmmoCapacity() {
        return 1;
    }

    @Override
    protected float getReloadSpeedMultiplier() {
        return 1.0f;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 20;
    }
}
