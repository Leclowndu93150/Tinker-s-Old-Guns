package com.leclowndu93150.tinkers_old_guns;

import com.leclowndu93150.tinkers_old_guns.common.TinkersGunStats;
import com.leclowndu93150.tinkers_old_guns.config.TOGConfig;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunModifiers;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunParts;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTabs;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTools;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

@Mod(TinkersOldGuns.MODID)
public class TinkersOldGuns {

    public static final String MODID = "tinkers_old_guns";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TinkersOldGuns() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTabContents);
        TinkersGunTools.ITEMS.register(modEventBus);
        TinkersGunParts.ITEMS.register(modEventBus);
        TinkersGunModifiers.MODIFIERS.register(modEventBus);
        TinkersGunTabs.CREATIVE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TOGConfig.COMMON_SPEC);

    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(TinkersGunStats::init);
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == TinkerSmeltery.tabSmeltery.get()) {
            event.accept(TinkersGunParts.FLINTLOCK_MECHANISM_CAST.get());
            event.accept(TinkersGunParts.FLINTLOCK_MECHANISM_CAST.getSand());
            event.accept(TinkersGunParts.FLINTLOCK_MECHANISM_CAST.getRedSand());
        }
    }

}
