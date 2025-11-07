package de.herobrickhd.deathbuyback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class DeathBuybackGuiListener implements Listener {

    private final DeathSnapshotManager manager;

    public DeathBuybackGuiListener(DeathSnapshotManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView() == null) return;

        Component titleComponent = event.getView().title();
        String title = PlainTextComponentSerializer.plainText().serialize(titleComponent);

        ItemStack clicked = event.getCurrentItem();

        // 1) Haupt-Menü
        if ("Mors Mutual Insurance".equalsIgnoreCase(title)) {
            event.setCancelled(true); // nix rausnehmen

            if (clicked == null) return;

            Material type = clicked.getType();

            // Menü schließen
            if (type == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            // Normales Händler-UI öffnen
            if (type == Material.EMERALD) {
                openVanillaTrader(player);
                return;
            }

            // Tod auswählen
            if (type == Material.PAPER) {
                int index = event.getSlot() - 10; // unsere Tode liegen auf 10–16
                if (index < 0) return;

                List<DeathSnapshot> snaps = manager.getSnapshots(player.getUniqueId());
                if (index >= snaps.size()) return;

                DeathSnapshot snap = snaps.get(index);

                double value = ValueCalculator.calculateInventoryValue(snap.getContents());
                int emeraldCost = ValueCalculator.toEmeraldCost(value);

                boolean firstFreeEnabled = DeathBuybackPlugin.getInstance().getConfig()
                        .getBoolean("first_free_enabled", true);
                boolean alreadyUsedFree = manager.hasUsedFree(player.getUniqueId());

                // WICHTIG: hier NICHT nach index == 0 gehen
                boolean isFree = firstFreeEnabled && !alreadyUsedFree;

                ConfirmationGUI.open(player, index, snap, emeraldCost, isFree);
            }

            return;
        }

        // 2) Bestätigungs-GUI
        if ("Bestätige Rückkauf".equalsIgnoreCase(title)) {
            event.setCancelled(true);

            if (clicked == null) return;

            Material type = clicked.getType();

            // Metadaten holen: welchen Tod will er wirklich?
            if (!player.hasMetadata("mmi_confirm_index")) {
                player.sendMessage(Component.text("Keine Auswahl gefunden.").color(NamedTextColor.RED));
                player.closeInventory();
                return;
            }
            int index = player.getMetadata("mmi_confirm_index").get(0).asInt();

            List<DeathSnapshot> snaps = manager.getSnapshots(player.getUniqueId());
            if (index < 0 || index >= snaps.size()) {
                player.sendMessage(Component.text("Dieser Tod existiert nicht mehr.").color(NamedTextColor.RED));
                player.closeInventory();
                return;
            }
            DeathSnapshot snap = snaps.get(index);

            // Kosten erneut berechnen
            double value = ValueCalculator.calculateInventoryValue(snap.getContents());
            int emeraldCost = ValueCalculator.toEmeraldCost(value);

            boolean firstFreeEnabled = DeathBuybackPlugin.getInstance().getConfig()
                    .getBoolean("first_free_enabled", true);
            boolean alreadyUsedFree = manager.hasUsedFree(player.getUniqueId());
            boolean isFree = firstFreeEnabled && !alreadyUsedFree;

            // Abbrechen
            if (type == Material.RED_WOOL) {
                player.closeInventory();
                return;
            }

            // Bestätigen
            if (type == Material.LIME_WOOL || type == Material.GRAY_WOOL) {

                // prüfen ob er zahlen kann (wenn nicht free)
                if (!isFree && countEmeralds(player) < emeraldCost) {
                    player.sendMessage(Component.text("Du hast nicht genug Smaragde.")
                            .color(NamedTextColor.RED));
                    return;
                }

                // bezahlen
                if (!isFree) {
                    removeEmeralds(player, emeraldCost);
                } else {
                    // jetzt erst: free verbraucht!
                    manager.setUsedFree(player.getUniqueId(), true);
                }

                // Inventar + Level wiederherstellen
                restorePlayer(player, snap);

                // diesen Tod aus der Liste löschen
                manager.removeSnapshot(player.getUniqueId(), index);

                player.removeMetadata("mmi_confirm_index", DeathBuybackPlugin.getInstance());
                player.sendMessage(Component.text("Inventar wiederhergestellt.").color(NamedTextColor.GREEN));
                player.closeInventory();
            }
        }
    }

    // Drag ebenfalls blocken, damit man nix rausziehen kann
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if ("Mors Mutual Insurance".equalsIgnoreCase(title)
                || "Bestätige Rückkauf".equalsIgnoreCase(title)) {
            event.setCancelled(true);
        }
    }

    private void openVanillaTrader(Player player) {
        if (!player.hasMetadata("mmi_last_trader")) {
            player.sendMessage(Component.text("Kein Händler verfügbar.").color(NamedTextColor.RED));
            return;
        }

        String traderId = player.getMetadata("mmi_last_trader").get(0).asString();
        UUID traderUuid = UUID.fromString(traderId);
        Entity trader = player.getWorld().getEntity(traderUuid);

        if (trader == null || trader.getType() != EntityType.WANDERING_TRADER) {
            player.sendMessage(Component.text("Der Händler ist nicht mehr da.").color(NamedTextColor.RED));
            return;
        }

        player.openMerchant((org.bukkit.entity.WanderingTrader) trader, true);
        player.removeMetadata("mmi_last_trader", DeathBuybackPlugin.getInstance());
    }

    private int countEmeralds(Player p) {
        int sum = 0;
        for (ItemStack it : p.getInventory().getContents()) {
            if (it != null && it.getType() == Material.EMERALD) {
                sum += it.getAmount();
            }
        }
        return sum;
    }

    private void removeEmeralds(Player p, int amount) {
        int toRemove = amount;
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it == null) continue;
            if (it.getType() != Material.EMERALD) continue;

            int stack = it.getAmount();
            if (stack > toRemove) {
                it.setAmount(stack - toRemove);
                break;
            } else {
                contents[i] = null;
                toRemove -= stack;
                if (toRemove <= 0) break;
            }
        }
        p.getInventory().setContents(contents);
    }

    private void restorePlayer(Player player, DeathSnapshot snapshot) {
        // Inventar wiederherstellen
        player.getInventory().setContents(snapshot.getContents());
        // Level wiederherstellen
        player.setLevel(snapshot.getLevel());
    }
}
