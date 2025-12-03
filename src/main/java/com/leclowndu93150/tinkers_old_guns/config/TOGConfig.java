package com.leclowndu93150.tinkers_old_guns.config;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

public class TOGConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = pair.getRight();
        COMMON = pair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.EnumValue<Level.ExplosionInteraction> explosiveOrdnanceBlockInteraction;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("modifiers");
            explosiveOrdnanceBlockInteraction = builder
                .comment("Explosive Ordnance block destruction mode:",
                         "NONE - No block destruction",
                         "BLOCK - Breaks blocks in radius",
                         "TNT - Full TNT-style destruction")
                .defineEnum("explosiveOrdnanceBlockInteraction", Level.ExplosionInteraction.BLOCK);
            builder.pop();
        }
    }
}
