package de.herobrickhd.deathbuyback;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;

public class DeathSnapshot {
    private final ItemStack[] contents;
    private final int level;
    private final long timestamp;
    private final String deathMessage;

    public DeathSnapshot(ItemStack[] contents, int level, long timestamp, String deathMessage) {
        this.contents = contents; this.level = level;
        this.timestamp = timestamp; this.deathMessage = deathMessage;
    }

    public void toConfig(ConfigurationSection section) {
        section.set("level", level);
        section.set("timestamp", timestamp);
        section.set("deathMessage", deathMessage);
        section.set("contents", Arrays.asList(contents));
    }

    public static DeathSnapshot fromConfig(ConfigurationSection section) {
        int level = section.getInt("level");
        long time = section.getLong("timestamp");
        String msg = section.getString("deathMessage", "");
        var list = section.getList("contents");
        ItemStack[] cont = list.toArray(new ItemStack[0]);
        return new DeathSnapshot(cont, level, time, msg);
    }

    public ItemStack[] getContents() { return contents; }
    public int getLevel() { return level; }
    public long getTimestamp() { return timestamp; }
    public String getDeathMessage() { return deathMessage; }
}
