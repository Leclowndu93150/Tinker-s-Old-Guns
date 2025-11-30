package com.leclowndu93150.tinkers_old_guns.datagen;

import com.google.gson.JsonObject;
import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.registry.TinkersGunTools;
import net.minecraft.data.PackOutput;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.library.data.AbstractToolItemModelProvider;

import java.io.IOException;

public class TOGToolItemModelProvider extends AbstractToolItemModelProvider {
    public static final String PROPS = "{\n" +
            "  \"__comment\": \"this file is not actually used directly, its mostly for reference as it was simplier to copy transforms than the whole tool model\",\n" +
            "  \"parent\": \"forge:item/default\",\n" +
            "  \"gui_light\": \"front\",\n" +
            "  \"display\": {\n" +
            "    \"firstperson_righthand\": {\n" +
            "      \"rotation\": [ 0, 180, -5 ],\n" +
            "      \"translation\": [ -5, -2, -0.2 ],\n" +
            "      \"scale\": [ 1, 1, 1 ]\n" +
            "    },\n" +
            "    \"firstperson_lefthand\": {\n" +
            "      \"rotation\": [ 0, 0, 5 ],\n" +
            "      \"translation\": [ -5, -2, -0.2 ],\n" +
            "      \"scale\": [ 1, 1, 1 ]\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    public TOGToolItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, existingFileHelper, TinkersOldGuns.MODID);
    }

    @Override
    protected void addModels() throws IOException {
        JsonObject toolBlocking = GsonHelper.parse(PROPS);
        // Generate blocking and broken model variants for each gun
        tool(TinkersGunTools.PISTOL, toolBlocking, "limb_bottom", "handle", "flintlock_mechanism");
        tool(TinkersGunTools.CARBINE, toolBlocking, "limb_bottom", "handle", "flintlock_mechanism");
        tool(TinkersGunTools.MUSKET, toolBlocking, "limb_bottom", "handle", "flintlock_mechanism");
    }

    @Override
    public String getName() {
        return "Tinker's Old Guns Tool Item Models";
    }
}
