package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;

public class TinkersGunParts {
    public static final ItemDeferredRegisterExtension ITEMS = new ItemDeferredRegisterExtension(TinkersOldGuns.MODID);

    private static final Item.Properties PART_PROPS = new Item.Properties();

    public static final ItemObject<ToolPartItem> FLINTLOCK_MECHANISM = ITEMS.register("flintlock_mechanism",
            () -> new ToolPartItem(PART_PROPS, HandleMaterialStats.ID));

    // Creates: flintlock_mechanism_cast, flintlock_mechanism_sand_cast, flintlock_mechanism_red_sand_cast
    public static final CastItemObject FLINTLOCK_MECHANISM_CAST = ITEMS.registerCast(FLINTLOCK_MECHANISM, PART_PROPS);
}
