package com.leclowndu93150.tinkers_old_guns.datagen;

import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunParts;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTools;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.tinkering.AbstractStationSlotLayoutProvider;
import slimeknights.tconstruct.tools.TinkerToolParts;

public class TOGStationSlotLayoutProvider extends AbstractStationSlotLayoutProvider {
    public TOGStationSlotLayoutProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void addLayouts() {
        this.defineModifiable(TinkersGunTools.PISTOL)
                .sortIndex(20)
                .addInputItem(TinkerToolParts.bowLimb, 22, 32)
                .addInputItem(TinkerToolParts.toolHandle, 32, 60)
                .addInputItem(TinkersGunParts.FLINTLOCK_MECHANISM.get(), 43, 40)
                .build();
        this.defineModifiable(TinkersGunTools.CARBINE)
                .sortIndex(21)
                .addInputItem(TinkerToolParts.bowLimb, 21, 29)
                .addInputItem(TinkerToolParts.bowGrip, 51, 60)
                .addInputItem(TinkersGunParts.FLINTLOCK_MECHANISM.get(), 43, 40)
                .build();
        this.defineModifiable(TinkersGunTools.MUSKET)
                .sortIndex(22)
                .addInputItem(TinkerToolParts.bowLimb, 21, 29)
                .addInputItem(TinkerToolParts.toughHandle, 29, 60)
                .addInputItem(TinkerToolParts.bowGrip, 50, 60)
                .addInputItem(TinkersGunParts.FLINTLOCK_MECHANISM.get(), 43, 40)
                .build();
    }

    @Override
    public String getName() {
        return "Tinkers' Old Guns Tinker Station Slot Layouts";
    }
}
