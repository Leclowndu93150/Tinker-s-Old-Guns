package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;

public class TinkersGunParts {
    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(TinkersOldGuns.MODID);

    private static final Item.Properties PART_PROPS = new Item.Properties();

    public static final ItemObject<ToolPartItem> FLINTLOCK_MECHANISM = ITEMS.register("flintlock_mechanism",
            () -> new ToolPartItem(PART_PROPS, HandleMaterialStats.ID));
}
