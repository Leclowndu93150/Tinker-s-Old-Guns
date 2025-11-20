package com.leclowndu93150.tinkers_old_guns.common;

import com.zach2039.oldguns.api.ammo.Ammo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.Optional;

public enum AmmoSize {
    SMALL("small", 0.36f),
    MEDIUM("medium", 0.48f),
    LARGE("large", Float.MAX_VALUE);

    private final String keyword;
    private final float maxProjectileSize;

    AmmoSize(String keyword, float maxProjectileSize) {
        this.keyword = keyword;
        this.maxProjectileSize = maxProjectileSize;
    }

    /**
     * Attempts to resolve the ammo size from the provided stack using the registry name or projectile stats.
     */
    public static Optional<AmmoSize> fromStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        Item item = stack.getItem();
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(item);
        if (name != null) {
            String path = name.getPath().toLowerCase(Locale.ROOT);
            for (AmmoSize size : values()) {
                if (size.matchesPath(path)) {
                    return Optional.of(size);
                }
            }
        }

        if (item instanceof Ammo ammo) {
            float projectileSize = ammo.getProjectileSize();
            for (AmmoSize size : values()) {
                if (projectileSize <= size.maxProjectileSize) {
                    return Optional.of(size);
                }
            }
        }

        return Optional.empty();
    }

    private boolean matchesPath(String path) {
        if (!path.contains(keyword)) {
            return false;
        }
        // ensure the keyword is a standalone segment (prefix/suffix) to avoid accidental matches
        String token = "_" + keyword + "_";
        return path.startsWith(keyword + "_") || path.endsWith("_" + keyword) || path.contains(token);
    }
}
