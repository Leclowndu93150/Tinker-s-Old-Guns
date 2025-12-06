package com.leclowndu93150.tinkers_old_guns.common.item.gun;

import com.leclowndu93150.tinkers_old_guns.common.AmmoSize;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

public class ModifiablePistolItem extends ModifiableGunItem {

    public ModifiablePistolItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition, AmmoSize.SMALL);
    }

    @Override
    public int getBaseAmmoCapacity() {
        return 1;
    }

    @Override
    protected float getReloadSpeedMultiplier() {
        return 0.8f;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    public float getBaseVelocity() {
        return 3.5f;
    }
}
