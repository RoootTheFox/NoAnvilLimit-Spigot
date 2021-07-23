package net.ddns.foxsquad.noanvillimitspigot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.ddns.foxsquad.noanvillimitspigot.commands.RenameCommand;
import net.ddns.foxsquad.noanvillimitspigot.events.AnvilEventStuff;
import net.minecraft.SharedConstants;
import net.minecraft.world.inventory.ContainerAnvil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Objects;

public final class NoAnvilLimitSpigot extends JavaPlugin {
    public static final String[] color_perms = new String[]{"purpur.anvil.color"};

    FileConfiguration config = getConfig();

    public static boolean debug = false;
    @Override
    public void onEnable() {
        // Plugin startup logic

        config.addDefault("enableAnvilGUIHack", false);
        config.addDefault("enableRenameCommand", true);
        config.addDefault("debug", false);
        config.options().copyDefaults(true);

        saveConfig();

        if(config.getBoolean("debug")) {
            debug = true;
            getLogger().info("debug mode enabled");
            getServer().getPluginManager().registerEvents(new AnvilEventStuff(), this);
        }

        if(config.getBoolean("enableRenameCommand")) {
            if(debug) getLogger().info("REGISTER: commands");
            final Field bukkitCommandMap;
            try {
                bukkitCommandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getPluginManager());
                commandMap.register("rename", new RenameCommand("rename"));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(config.getBoolean("enableAnvilGUIHack")) {
            if(getBukkitVersion().equals("v1_17_R1")) {
                if(debug) getLogger().info("REGISTER: packets");
                ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
                protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.ITEM_NAME) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        event.setCancelled(true);
                        Player player = event.getPlayer();
                        String newName = event.getPacket().getStrings().getValues().get(0);
                        if(debug) System.out.println("RECEIVE PACKET");

                        if(debug) System.out.println("new name: "+newName);
                        Inventory inv = player.getOpenInventory().getTopInventory();

                        if(inv instanceof AnvilInventory) {
                            if(debug) System.out.println("anvil inventory confirmed");

                            Bukkit.getScheduler().runTask(this.plugin, () -> ((ContainerAnvil)((CraftPlayer)player).getHandle().bV).a(SharedConstants.a(newName))); // NMS sucks but its actually awesome
                        }
                    }
                });
            } else {
                getLogger().severe("Unsupported server version! Disabling custom packet handling. The length bypass in Anvil GUIs wont work and things might go south!");
                getLogger().severe("You are using version "+getBukkitVersion()+ " but this plugin was made for v_1_17_R1!");
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloading NoAnvilLimit");
        // Plugin shutdown logic
    }

    public static boolean hasColorPerms(Player p) {
        for (String color_perm : color_perms) {
            if(p.hasPermission(color_perm)) return true;
        }
        return false;
    }

    public static String getBukkitVersion() {
        String bukkitVersion = NoAnvilLimitSpigot.getProvidingPlugin(NoAnvilLimitSpigot.class).getServer().getClass().getPackage().getName();
        return bukkitVersion.substring(bukkitVersion.lastIndexOf(".")+1);
    }
}
