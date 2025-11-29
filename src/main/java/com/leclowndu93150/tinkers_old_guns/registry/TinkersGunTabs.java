package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.function.Consumer;

public class TinkersGunTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersOldGuns.MODID);

    public static final RegistryObject<CreativeModeTab> TAB_GUNS = CREATIVE_TABS.register(
        "guns", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tinkers_old_guns.guns"))
            .icon(() -> TinkersGunTools.MUSKET.get().getRenderTool())
            .displayItems((params, output) -> addGunTabItems(output))
            .withSearchBar()
            .build()
    );

    public static final RegistryObject<CreativeModeTab> TAB_GUN_PARTS = CREATIVE_TABS.register(
        "gun_parts", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tinkers_old_guns.gun_parts"))
            .icon(() -> new ItemStack(TinkersGunParts.FLINTLOCK_MECHANISM.get()))
            .displayItems((params, output) -> addGunPartTabItems(output))
            .build()
    );

    private static void addGunTabItems(CreativeModeTab.Output tab) {
        Consumer<ItemStack> output = tab::accept;

        acceptTool(output, TinkersGunTools.PISTOL);
        acceptTool(output, TinkersGunTools.CARBINE);
        acceptTool(output, TinkersGunTools.MUSKET);
    }

    private static void addGunPartTabItems(CreativeModeTab.Output tab) {
        Consumer<ItemStack> output = tab::accept;

        acceptPart(output, TinkersGunParts.FLINTLOCK_MECHANISM);
    }

    private static void acceptTool(Consumer<ItemStack> output, ItemObject<? extends IModifiable> tool) {
        ToolBuildHandler.addVariants(output, tool.get(), "");
    }

    private static void acceptPart(Consumer<ItemStack> output, ItemObject<? extends IMaterialItem> item) {
        item.get().addVariants(output, "");
    }
}
