package de.herobrickhd.deathbuyback;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

public class ValueCalculator {

    private static FileConfiguration cfg() {
        return DeathBuybackPlugin.getInstance().getConfig();
    }

    public static double calculateInventoryValue(ItemStack[] contents) {
        double sum = 0;
        if (contents == null) return 0;
        for (ItemStack item : contents) sum += calculateItemValue(item);
        return sum;
    }

    public static double calculateItemValue(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        double base = getBaseValue(item.getType()) * item.getAmount();

        if (item.getItemMeta() != null && item.getItemMeta().hasEnchants())
            base *= 1.5 + (0.2 * item.getItemMeta().getEnchants().size());

        if (item.getItemMeta() instanceof BlockStateMeta bsm &&
                bsm.getBlockState() instanceof ShulkerBox shulker)
            base += calculateInventoryValue(shulker.getInventory().getContents());

        if (item.getItemMeta() instanceof BundleMeta bundle)
            for (ItemStack inner : bundle.getItems())
                base += calculateItemValue(inner);

        return base;
    }

    private static double getBaseValue(Material mat) {
        String key = mat.name();
        if (cfg().isSet("material_values." + key))
            return cfg().getDouble("material_values." + key);
        return cfg().getDouble("default_value", 10.0);
    }

    public static int toEmeraldCost(double value) {
        double div = cfg().getDouble("emerald_divisor", 100.0);
        return (int) Math.ceil(value / div);
    }
}
