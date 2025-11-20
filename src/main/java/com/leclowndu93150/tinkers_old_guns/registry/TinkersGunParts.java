package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;

public class TinkersGunParts {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TinkersOldGuns.MODID);

    private static final Item.Properties PART_PROPS = new Item.Properties();

    public static final RegistryObject<ToolPartItem> FLINTLOCK_MECHANISM = ITEMS.register("flintlock_mechanism",
            () -> new ToolPartItem(PART_PROPS, HandleMaterialStats.ID));
}
