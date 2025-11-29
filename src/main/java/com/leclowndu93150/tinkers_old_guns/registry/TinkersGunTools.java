package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableCarbineItem;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableMusketItem;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiablePistolItem;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;

public class TinkersGunTools {
    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(TinkersOldGuns.MODID);

    private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().stacksTo(1);

    public static final ItemObject<ModifiablePistolItem> PISTOL = ITEMS.register("pistol",
            () -> new ModifiablePistolItem(UNSTACKABLE_PROPS, TinkersGunDefinitions.PISTOL));

    public static final ItemObject<ModifiableCarbineItem> CARBINE = ITEMS.register("carbine",
            () -> new ModifiableCarbineItem(UNSTACKABLE_PROPS, TinkersGunDefinitions.CARBINE));

    public static final ItemObject<ModifiableMusketItem> MUSKET = ITEMS.register("musket",
            () -> new ModifiableMusketItem(UNSTACKABLE_PROPS, TinkersGunDefinitions.MUSKET));
}
