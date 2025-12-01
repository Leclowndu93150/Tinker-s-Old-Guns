package com.leclowndu93150.tinkers_old_guns.datagen;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTools;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.IToolRecipeHelper;

import java.util.function.Consumer;

public class TOGToolsRecipeProvider extends RecipeProvider implements IMaterialRecipeHelper, IToolRecipeHelper, IConditionBuilder, IRecipeHelper {
    public TOGToolsRecipeProvider(PackOutput generator) {
        super(generator);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        String folder = "tools/building/";

        this.toolBuilding(consumer, TinkersGunTools.PISTOL, folder);
        this.toolBuilding(consumer, TinkersGunTools.CARBINE, folder);
        this.toolBuilding(consumer, TinkersGunTools.MUSKET, folder);
    }

    @Override
    public String getModId() {
        return TinkersOldGuns.MODID;
    }
}
