package com.leclowndu93150.tinkers_old_guns.common;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TinkersGunStats {

    public static final FloatToolStat WATER_INERTIA = ToolStats.register(
            new FloatToolStat(
                    new ToolStatId(new ResourceLocation(TinkersOldGuns.MODID, "water_inertia")),
                    0xFF5A82F3,
                    0.6f,
                    0.01f,
                    0.99f
            )
    );

    public static void init() {
    }
}
