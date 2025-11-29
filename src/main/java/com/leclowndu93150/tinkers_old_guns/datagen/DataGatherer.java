package com.leclowndu93150.tinkers_old_guns.datagen;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.client.data.material.GeneratorPartTextureJsonGenerator;

@Mod.EventBusSubscriber(modid = TinkersOldGuns.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGatherer {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        generator.addProvider(event.includeServer(), new TOGToolsRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new TOGStationSlotLayoutProvider(packOutput));
        generator.addProvider(event.includeServer(), new TOGToolDefinitions(packOutput));
        TOGPartSpriteProvider spriteProvider = new TOGPartSpriteProvider();
        generator.addProvider(event.includeClient(), new GeneratorPartTextureJsonGenerator(packOutput, TinkersOldGuns.MODID, spriteProvider));
        generator.addProvider(event.includeClient(), new TOGToolItemModelProvider(packOutput, event.getExistingFileHelper()));
    }
}
