package com.leclowndu93150.tinkers_old_guns;

import com.leclowndu93150.tinkers_old_guns.common.TinkersGunStats;
import com.leclowndu93150.tinkers_old_guns.config.TOGConfig;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunParts;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTabs;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTools;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(TinkersOldGuns.MODID)
public class TinkersOldGuns {

    public static final String MODID = "tinkers_old_guns";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TinkersOldGuns() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        TinkersGunTools.ITEMS.register(modEventBus);
        TinkersGunParts.ITEMS.register(modEventBus);
        TinkersGunModifiers.MODIFIERS.register(modEventBus);
        TinkersGunTabs.CREATIVE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TOGConfig.COMMON_SPEC);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(TinkersGunStats::init);
    }
}
