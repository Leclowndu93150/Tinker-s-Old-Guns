package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableCarbineItem;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableMusketItem;
import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiablePistolItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

public class TinkersGunTools {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TinkersOldGuns.MODID);

    private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().stacksTo(1);

    public static final RegistryObject<ModifiablePistolItem> PISTOL = ITEMS.register("pistol",
            () -> new ModifiablePistolItem(UNSTACKABLE_PROPS, TinkersGunDefinitions.PISTOL));

    public static final RegistryObject<ModifiableCarbineItem> CARBINE = ITEMS.register("carbine",
            () -> new ModifiableCarbineItem(UNSTACKABLE_PROPS, TinkersGunDefinitions.CARBINE));

    public static final RegistryObject<ModifiableMusketItem> MUSKET = ITEMS.register("musket",
            () -> new ModifiableMusketItem(UNSTACKABLE_PROPS, TinkersGunDefinitions.MUSKET));
}
