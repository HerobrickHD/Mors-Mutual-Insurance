package de.herobrickhd.deathbuyback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TraderListener implements Listener {

    private final DeathSnapshotManager manager;

    public TraderListener(DeathSnapshotManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (clicked.getType() != EntityType.WANDERING_TRADER) {
            return;
        }

        Player player = event.getPlayer();

        // Sonderfall: Spieler sneakt + Nametag in einer Hand -> NICHT unser UI öffnen
        boolean sneaking = player.isSneaking();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        boolean holdingNameTag =
                (main != null && main.getType() == Material.NAME_TAG) ||
                (off != null && off.getType() == Material.NAME_TAG);

        if (sneaking && holdingNameTag) {
            // vanilla-Verhalten erlauben
            return;
        }

        // ab hier: unser Plugin übernimmt
        event.setCancelled(true);

        // Händler merken, damit wir später das vanilla-GUI öffnen können
        player.setMetadata("mmi_last_trader",
                new FixedMetadataValue(DeathBuybackPlugin.getInstance(), clicked.getUniqueId().toString()));

        openMainGui(player);
    }

    public void openMainGui(Player player) {
        boolean firstFreeEnabled = DeathBuybackPlugin.getInstance().getConfig().getBoolean("first_free_enabled", true);
        boolean alreadyUsedFree = manager.hasUsedFree(player.getUniqueId());

        Inventory gui = Bukkit.createInventory(
                null,
                27,
                Component.text("Mors Mutual Insurance").color(NamedTextColor.DARK_AQUA)
        );

        // Rahmen füllen
        ItemStack frame = createGlass(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, frame);
        }

        // feste Buttons
        gui.setItem(20, createOpenVanillaTraderItem());
        gui.setItem(26, createCloseItem());

        List<DeathSnapshot> snapshots = manager.getSnapshots(player.getUniqueId());

        // FALL: keine gespeicherten Tode
        if (snapshots.isEmpty()) {
            ItemStack info = new ItemStack(Material.BOOK);
            ItemMeta meta = info.getItemMeta();
            meta.displayName(Component.text("Keine gespeicherten Tode")
                    .color(NamedTextColor.GRAY));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Du hast aktuell nichts zurückzukaufen.")
                    .color(NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Wenn du stirbst, merkt der Händler sich das.")
                    .color(NamedTextColor.GRAY));
            lore.add(Component.text("Komm nach deinem nächsten Tod wieder.")
                    .color(NamedTextColor.GRAY));
            meta.lore(lore);
            info.setItemMeta(meta);

            gui.setItem(13, info);
            player.openInventory(gui);
            return;
        }

        // FALL: es gibt Tode -> anzeigen
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        int slot = 10;

        for (int i = 0; i < snapshots.size() && slot <= 16; i++) {
            DeathSnapshot snap = snapshots.get(i);

            double value = ValueCalculator.calculateInventoryValue(snap.getContents());
            int emeraldCost = ValueCalculator.toEmeraldCost(value);

            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();

            meta.displayName(Component.text("Tod #" + (i + 1)).color(NamedTextColor.GOLD));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Todeszeitpunkt: " + sdf.format(new Date(snap.getTimestamp())))
                    .color(NamedTextColor.GRAY));
            if (snap.getDeathMessage() != null && !snap.getDeathMessage().isEmpty()) {
                lore.add(Component.text(snap.getDeathMessage()).color(NamedTextColor.DARK_GRAY));
            }
            lore.add(Component.text("Level: " + snap.getLevel()).color(NamedTextColor.GRAY));

            // Anzeige: nur wenn Spieler sein Gratis-Recht noch nicht genutzt hat
            // (die echte Logik macht der GUI-Listener beim Kauf)
            if (firstFreeEnabled && !alreadyUsedFree && i == 0) {
                lore.add(Component.text("Kosten: GRATIS (1x)").color(NamedTextColor.GREEN));
            } else {
                lore.add(Component.text("Kosten: " + emeraldCost + " Smaragde").color(NamedTextColor.YELLOW));
            }
            lore.add(Component.text("Klicke zum Wiederherstellen").color(NamedTextColor.DARK_GRAY));

            meta.lore(lore);
            paper.setItemMeta(meta);

            gui.setItem(slot, paper);
            slot++;
        }

        player.openInventory(gui);
    }

    private ItemStack createGlass(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Menü schließen").color(NamedTextColor.RED));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createOpenVanillaTraderItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Normales Händler-Menü").color(NamedTextColor.GREEN));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Öffnet das originale").color(NamedTextColor.GRAY));
        lore.add(Component.text("Wanderhändler-GUI.").color(NamedTextColor.GRAY));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
