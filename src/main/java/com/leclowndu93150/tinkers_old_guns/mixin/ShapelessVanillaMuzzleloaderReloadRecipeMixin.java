package com.leclowndu93150.tinkers_old_guns.mixin;

import com.leclowndu93150.tinkers_old_guns.common.item.gun.ModifiableGunItem;
import com.leclowndu93150.tinkers_old_guns.common.util.GunAmmoHelper;
import com.zach2039.oldguns.api.ammo.FirearmAmmo;
import com.zach2039.oldguns.api.firearm.util.FirearmNBTHelper;
import com.zach2039.oldguns.world.item.crafting.recipe.ShapelessVanillaMuzzleloaderReloadRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mixin(value = ShapelessVanillaMuzzleloaderReloadRecipe.class, remap = false)
public class ShapelessVanillaMuzzleloaderReloadRecipeMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void onMatches(CraftingContainer craftingContainer, Level level, CallbackInfoReturnable<Boolean> cir) {
        ItemStack gunStack = ItemStack.EMPTY;
        ItemStack ammoStack = ItemStack.EMPTY;
        int itemCount = 0;

        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack stack = craftingContainer.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() instanceof ModifiableGunItem) {
                    gunStack = stack;
                } else if (stack.getItem() instanceof FirearmAmmo) {
                    ammoStack = stack;
                }
            }
        }

        if (!gunStack.isEmpty() && !ammoStack.isEmpty() && itemCount == 2) {
            ToolStack tool = ToolStack.from(gunStack);
            ModifiableGunItem gunItem = (ModifiableGunItem) gunStack.getItem();
            
            if (!tool.isBroken() && 
                GunAmmoHelper.getCurrentAmmoCount(gunStack) < GunAmmoHelper.getAmmoCapacity(tool) &&
                GunAmmoHelper.isValidAmmo(gunStack, ammoStack, gunItem.getAmmoSize())) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void onAssemble(CraftingContainer inv, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack gunStack = ItemStack.EMPTY;
        ItemStack ammoStack = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ModifiableGunItem) {
                gunStack = stack.copy();
            } else if (stack.getItem() instanceof FirearmAmmo) {
                ammoStack = stack.copy();
            }
        }

        if (!gunStack.isEmpty() && !ammoStack.isEmpty()) {
            FirearmNBTHelper.pushNBTTagAmmo(gunStack, ammoStack);
            cir.setReturnValue(gunStack);
        }
    }
}
