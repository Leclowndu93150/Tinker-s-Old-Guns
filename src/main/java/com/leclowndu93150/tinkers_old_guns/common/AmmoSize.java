package com.leclowndu93150.tinkers_old_guns.common;

import com.zach2039.oldguns.api.ammo.AmmoTypes;

import java.util.List;

public enum AmmoSize {
    SMALL(List.of(
        AmmoTypes.FirearmAmmo.SMALL_STONE_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.SMALL_IRON_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.SMALL_LEAD_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.SMALL_STONE_BIRDSHOT,
        AmmoTypes.FirearmAmmo.SMALL_IRON_BIRDSHOT,
        AmmoTypes.FirearmAmmo.SMALL_LEAD_BIRDSHOT,
        AmmoTypes.FirearmAmmo.SMALL_IRON_BUCKSHOT,
        AmmoTypes.FirearmAmmo.SMALL_LEAD_BUCKSHOT
    )),
    MEDIUM(List.of(
        AmmoTypes.FirearmAmmo.MEDIUM_STONE_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.MEDIUM_IRON_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.MEDIUM_LEAD_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.MEDIUM_STONE_BIRDSHOT,
        AmmoTypes.FirearmAmmo.MEDIUM_IRON_BIRDSHOT,
        AmmoTypes.FirearmAmmo.MEDIUM_LEAD_BIRDSHOT,
        AmmoTypes.FirearmAmmo.MEDIUM_IRON_BUCKSHOT,
        AmmoTypes.FirearmAmmo.MEDIUM_LEAD_BUCKSHOT
    )),
    LARGE(List.of(
        AmmoTypes.FirearmAmmo.LARGE_STONE_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.LARGE_IRON_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.LARGE_LEAD_MUSKET_BALL,
        AmmoTypes.FirearmAmmo.LARGE_STONE_BIRDSHOT,
        AmmoTypes.FirearmAmmo.LARGE_IRON_BIRDSHOT,
        AmmoTypes.FirearmAmmo.LARGE_LEAD_BIRDSHOT,
        AmmoTypes.FirearmAmmo.LARGE_IRON_BUCKSHOT,
        AmmoTypes.FirearmAmmo.LARGE_LEAD_BUCKSHOT
    ));

    private final List<AmmoTypes.FirearmAmmo> validAmmoTypes;

    AmmoSize(List<AmmoTypes.FirearmAmmo> validAmmoTypes) {
        this.validAmmoTypes = validAmmoTypes;
    }

    public List<AmmoTypes.FirearmAmmo> getValidAmmoTypes() {
        return validAmmoTypes;
    }

    public boolean isValidAmmo(AmmoTypes.FirearmAmmo ammoType) {
        return validAmmoTypes.contains(ammoType);
    }
}
