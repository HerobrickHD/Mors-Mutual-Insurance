package de.herobrickhd.deathbuyback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MmiCommand implements CommandExecutor {

    private final DeathBuybackPlugin plugin;
    private final DeathSnapshotManager manager;

    public MmiCommand(DeathBuybackPlugin plugin, DeathSnapshotManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // nur Admins
        if (!sender.hasPermission("mmi.admin")) {
            sender.sendMessage(Component.text("Dafür hast du keine Rechte.")
                    .color(NamedTextColor.RED));
            return true;
        }

        // /mmi oder /mmi help
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        // /mmi reload
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(Component.text("Mors Mutual Insurance Config neu geladen.")
                    .color(NamedTextColor.GREEN));
            return true;
        }

        // /mmi list <spieler>
        if (args[0].equalsIgnoreCase("list")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Benutzung: /mmi list <spieler>")
                        .color(NamedTextColor.YELLOW));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID uuid = target.getUniqueId();

            List<DeathSnapshot> snaps = manager.getSnapshots(uuid);
            boolean usedFree = manager.hasUsedFree(uuid);

            sender.sendMessage(Component.text("===== MMI-Verlauf für " + target.getName() + " =====")
                    .color(NamedTextColor.AQUA));
            sender.sendMessage(Component.text("Gratis bereits genutzt: " + usedFree)
                    .color(usedFree ? NamedTextColor.RED : NamedTextColor.GREEN));

            if (snaps.isEmpty()) {
                sender.sendMessage(Component.text("Keine gespeicherten Tode.").color(NamedTextColor.GRAY));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                for (int i = 0; i < snaps.size(); i++) {
                    DeathSnapshot snap = snaps.get(i);
                    String time = sdf.format(new Date(snap.getTimestamp()));
                    sender.sendMessage(
                            Component.text("#" + (i + 1) + " ").color(NamedTextColor.GOLD)
                                    .append(Component.text(time).color(NamedTextColor.GRAY))
                                    .append(Component.text(" | Level: " + snap.getLevel()).color(NamedTextColor.GREEN))
                    );
                    if (snap.getDeathMessage() != null && !snap.getDeathMessage().isEmpty()) {
                        String dm = snap.getDeathMessage();
                        if (dm.length() > 80) dm = dm.substring(0, 77) + "...";
                        sender.sendMessage(Component.text("   Grund: " + dm).color(NamedTextColor.DARK_GRAY));
                    }
                }
            }
            sender.sendMessage(Component.text("================================").color(NamedTextColor.AQUA));
            return true;
        }

        // /mmi clear <spieler>
        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Benutzung: /mmi clear <spieler>")
                        .color(NamedTextColor.YELLOW));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID uuid = target.getUniqueId();

            manager.clearSnapshots(uuid);
            sender.sendMessage(Component.text("Versicherungsverlauf von " + target.getName() + " gelöscht.")
                    .color(NamedTextColor.GREEN));
            return true;
        }

        // /mmi resetfree <spieler>
        if (args[0].equalsIgnoreCase("resetfree")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Benutzung: /mmi resetfree <spieler>")
                        .color(NamedTextColor.YELLOW));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID uuid = target.getUniqueId();

            manager.resetUsedFree(uuid);
            sender.sendMessage(Component.text("Gratis-Versicherung für " + target.getName() + " zurückgesetzt.")
                    .color(NamedTextColor.GREEN));
            return true;
        }

        // alles andere
        sender.sendMessage(Component.text("Unbekannter Befehl. Nutze /mmi help.")
                .color(NamedTextColor.YELLOW));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("====== Mors Mutual Insurance ======").color(NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/mmi help").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Zeigt diese Hilfe an.").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/mmi reload").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Lädt die Config neu.").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/mmi list <spieler>").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Zeigt gespeicherte Tode des Spielers.").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/mmi clear <spieler>").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Löscht NUR den Verlauf des Spielers.").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/mmi resetfree <spieler>").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Setzt NUR das Gratis-Recht zurück.").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("===================================").color(NamedTextColor.AQUA));
    }
}
