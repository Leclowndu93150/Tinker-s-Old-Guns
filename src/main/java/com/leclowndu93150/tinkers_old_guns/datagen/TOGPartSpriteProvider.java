package com.leclowndu93150.tinkers_old_guns.datagen;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.common.TinkersGunStats;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunParts;
import slimeknights.tconstruct.library.client.data.material.AbstractPartSpriteProvider;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;

public class TOGPartSpriteProvider extends AbstractPartSpriteProvider {
    public TOGPartSpriteProvider() {
        super(TinkersOldGuns.MODID);
    }

    @Override
    public String getName() {
        return "Tinker's Old Guns Part Sprite Provider";
    }

    @Override
    protected void addAllSpites() {
        this.buildTool("pistol")
                .addLimb("limb_bottom")
                .addHandle("handle")
                .addPart("flintlock_mechanism", HandleMaterialStats.ID);
        this.addHandle("flintlock_mechanism");
    }
}
