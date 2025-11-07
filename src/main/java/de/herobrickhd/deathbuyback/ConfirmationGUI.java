package de.herobrickhd.deathbuyback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfirmationGUI {

    public static void open(Player player, int index, DeathSnapshot snapshot, int emeraldCost, boolean isFree) {
        Inventory inv = Bukkit.createInventory(
                null,
                27,
                Component.text("Bestätige Rückkauf").color(NamedTextColor.RED)
        );

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        int emeralds = countEmeralds(player);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        meta.displayName(Component.text("Tod vom " + sdf.format(new Date(snapshot.getTimestamp())))
                .color(NamedTextColor.GOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Level: " + snapshot.getLevel()).color(NamedTextColor.GRAY));
        if (isFree) {
            lore.add(Component.text("Kosten: GRATIS (1x)").color(NamedTextColor.GREEN));
        } else {
            lore.add(Component.text("Kosten: " + emeraldCost + " Smaragde").color(NamedTextColor.YELLOW));
        }
        lore.add(Component.text("Du hast: " + emeralds + " Smaragde").color(NamedTextColor.GRAY));
        meta.lore(lore);
        info.setItemMeta(meta);
        inv.setItem(13, info);

        // Confirm
        boolean canPay = isFree || emeralds >= emeraldCost;
        ItemStack confirm = new ItemStack(canPay ? Material.LIME_WOOL : Material.GRAY_WOOL);
        ItemMeta cm = confirm.getItemMeta();
        cm.displayName(
                (canPay ? Component.text("✅ Bestätigen").color(NamedTextColor.GREEN)
                        : Component.text("❌ Nicht genug Smaragde").color(NamedTextColor.DARK_GRAY))
        );
        confirm.setItemMeta(cm);
        inv.setItem(11, confirm);

        // Cancel
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta xm = cancel.getItemMeta();
        xm.displayName(Component.text("❌ Abbrechen").color(NamedTextColor.RED));
        cancel.setItemMeta(xm);
        inv.setItem(15, cancel);

        player.openInventory(inv);
        player.setMetadata("mmi_confirm_index",
                new FixedMetadataValue(DeathBuybackPlugin.getInstance(), index));
    }

    private static int countEmeralds(Player p) {
        int sum = 0;
        for (ItemStack it : p.getInventory().getContents()) {
            if (it != null && it.getType() == Material.EMERALD) {
                sum += it.getAmount();
            }
        }
        return sum;
    }
}
