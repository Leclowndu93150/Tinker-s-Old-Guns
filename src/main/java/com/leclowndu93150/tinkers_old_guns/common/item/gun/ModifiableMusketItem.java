package com.leclowndu93150.tinkers_old_guns.common.item.gun;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

public class ModifiableMusketItem extends ModifiableGunItem {

    public ModifiableMusketItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition, AmmoSize.LARGE);
    }

    @Override
    protected int getBaseAmmoCapacity() {
        return 1;
    }

    @Override
    protected float getReloadSpeedMultiplier() {
        return 1.3f;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 25;
    }
}
