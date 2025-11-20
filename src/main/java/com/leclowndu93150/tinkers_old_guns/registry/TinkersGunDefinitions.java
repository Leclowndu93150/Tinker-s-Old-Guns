package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

public class TinkersGunDefinitions {
    public static final ToolDefinition PISTOL = ToolDefinition.create(new ResourceLocation(TinkersOldGuns.MODID, "pistol"));
    public static final ToolDefinition CARBINE = ToolDefinition.create(new ResourceLocation(TinkersOldGuns.MODID, "carbine"));
    public static final ToolDefinition MUSKET = ToolDefinition.create(new ResourceLocation(TinkersOldGuns.MODID, "musket"));

    private TinkersGunDefinitions() {
    }
}
