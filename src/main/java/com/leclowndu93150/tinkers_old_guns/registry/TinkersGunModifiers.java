package com.leclowndu93150.tinkers_old_guns.registry;

import com.leclowndu93150.tinkers_old_guns.TinkersOldGuns;
import com.leclowndu93150.tinkers_old_guns.common.modifier.ExtraBarrelModifier;
import com.leclowndu93150.tinkers_old_guns.common.modifier.ScattershotModifier;
import com.leclowndu93150.tinkers_old_guns.common.modifier.SimultaneousModifier;
import com.leclowndu93150.tinkers_old_guns.common.modifier.SubaquaticModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public class TinkersGunModifiers {
    public static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TinkersOldGuns.MODID);

    public static final StaticModifier<ExtraBarrelModifier> EXTRA_BARREL = MODIFIERS.register("extra_barrel", ExtraBarrelModifier::new);
    public static final StaticModifier<ScattershotModifier> SCATTERSHOT = MODIFIERS.register("scattershot", ScattershotModifier::new);
    public static final StaticModifier<SubaquaticModifier> SUBAQUATIC = MODIFIERS.register("subaquatic", SubaquaticModifier::new);
    public static final StaticModifier<SimultaneousModifier> SIMULTANEOUS = MODIFIERS.register("simultaneous", SimultaneousModifier::new);
}
