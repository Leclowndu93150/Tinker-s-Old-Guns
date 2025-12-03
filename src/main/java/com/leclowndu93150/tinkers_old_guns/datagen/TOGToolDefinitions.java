package com.leclowndu93150.tinkers_old_guns.datagen;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunParts;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTools;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.tinkering.AbstractToolDefinitionDataProvider;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.tools.definition.module.build.MultiplyStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.SetStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.DefaultMaterialsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.PartStatsModule;
import slimeknights.tconstruct.library.tools.nbt.MultiplierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.ToolDefinitions;

public class TOGToolDefinitions extends AbstractToolDefinitionDataProvider {

    public TOGToolDefinitions(PackOutput packOutput) {
        super(packOutput, TinkersOldGuns.MODID);
    }

    @Override
    protected void addToolDefinitions() {
        RandomMaterial tier1Material = RandomMaterial.random().tier(1).build();
        DefaultMaterialsModule defaultFourParts = DefaultMaterialsModule.builder().material(new RandomMaterial[]{tier1Material, tier1Material, tier1Material, tier1Material}).build();
        this.define(TinkersGunTools.PISTOL.get())
                .module(PartStatsModule.parts()
                        .part(TinkerToolParts.bowLimb)
                        .part(TinkerToolParts.toolHandle)
                        .part(TinkersGunParts.FLINTLOCK_MECHANISM)
                        .build())
                .module(defaultFourParts)
                .module(new SetStatsModule(StatsNBT.builder()
                        .set(ToolStats.DURABILITY, 120.0F)
                        .set(ToolStats.ATTACK_DAMAGE, 0.0F)
                        .set(ToolStats.ATTACK_SPEED, 1.0F)
                        .build()))
                .module(new MultiplyStatsModule(MultiplierNBT.builder()
                        .set(ToolStats.DURABILITY, 1.5F)
                        .build()))
                .largeToolStartingSlots();
        this.define(TinkersGunTools.CARBINE.get())
                .module(PartStatsModule.parts()
                        .part(TinkerToolParts.bowLimb)
                        .part(TinkerToolParts.toughHandle)
                        .part(TinkersGunParts.FLINTLOCK_MECHANISM)
                        .build())
                .module(defaultFourParts)
                .module(new SetStatsModule(StatsNBT.builder()
                        .set(ToolStats.DURABILITY, 120.0F)
                        .set(ToolStats.ATTACK_DAMAGE, 0.0F)
                        .set(ToolStats.ATTACK_SPEED, 1.0F)
                        .build()))
                .module(new MultiplyStatsModule(MultiplierNBT.builder()
                        .set(ToolStats.DURABILITY, 1.5F)
                        .build()))
                .largeToolStartingSlots();
        this.define(TinkersGunTools.MUSKET.get())
                .module(PartStatsModule.parts()
                        .part(TinkerToolParts.bowLimb)
                        .part(TinkerToolParts.toughHandle)
                        .part(TinkerToolParts.largePlate)
                        .part(TinkersGunParts.FLINTLOCK_MECHANISM)
                        .build())
                .module(defaultFourParts)
                .module(new SetStatsModule(StatsNBT.builder()
                        .set(ToolStats.DURABILITY, 120.0F)
                        .set(ToolStats.ATTACK_DAMAGE, 0.0F)
                        .set(ToolStats.ATTACK_SPEED, 1.0F)
                        .build()))
                .module(new MultiplyStatsModule(MultiplierNBT.builder()
                        .set(ToolStats.DURABILITY, 1.5F)
                        .build()))
                .largeToolStartingSlots();
    }

    @Override
    public String getName() {
        return "Tinkers Old Guns Tool Definitions";
    }
}
