package net.ddns.foxsquad.noanvillimitspigot.commands;

import com.google.common.collect.Lists;
import joptsimple.internal.Strings;
import net.ddns.foxsquad.noanvillimitspigot.ColorThing;
import net.ddns.foxsquad.noanvillimitspigot.NoAnvilLimitSpigot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RenameCommand extends Command implements CommandExecutor, TabCompleter {

    public RenameCommand(String name) {
        super(name);
        this.description = "Rename an item without the limits of an anvil.";
        this.usageMessage = "/<command> [text]";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, Command command, @NotNull String s, String[] strings) {
        if(command.getName().equalsIgnoreCase("rename")) {
            if(commandSender instanceof Player) {
                Player p = (Player) commandSender;
                ItemMeta meta = p.getInventory().getItemInMainHand().getItemMeta();
                if(meta != null) {
                    return Collections.singletonList(ColorThing.untranslateHexCodes(meta.getDisplayName().replace("§", "&")));
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            ItemMeta meta = p.getInventory().getItemInMainHand().getItemMeta();
            if(meta != null) {
                String arg = Strings.join(args, " ");
                String completion = ColorThing.untranslateHexCodes(meta.getDisplayName().replace("§", "&"));

                List<String> yes = Collections.singletonList(completion);

                return yes.stream()
                        .filter(name -> name.regionMatches(true, 0, arg, 0, arg.length()))
                        .collect(Collectors.toList());
            }
        }
        return super.tabComplete(sender, alias, args);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        return run(commandSender, s, strings);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return run(commandSender, s, strings);
    }

    private boolean run(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("§cThis command can only be used by Players!");
        } else {
            Player p = (Player) commandSender;
            Block targetedBlock = p.getTargetBlock(null, 5);
            if(NoAnvilLimitSpigot.debug) System.out.println("block: "+targetedBlock.getType());

            if(targetedBlock.getType() == Material.ANVIL || targetedBlock.getType() == Material.CHIPPED_ANVIL || targetedBlock.getType() == Material.DAMAGED_ANVIL) {
                ItemStack item = p.getInventory().getItemInMainHand();
                if(item.getType() != Material.AIR) {
                    String bukkitVersion = NoAnvilLimitSpigot.getBukkitVersion();
                    String bukkitPackage = "org.bukkit.craftbukkit."+bukkitVersion;

                    Class<?> nms_CraftItemStack;
                    Method nms_CraftItemStack_asNMSCopy;
                    Class<?> nms_ItemStack;
                    Method nms_ItemStack_getRepairCost;
                    try {
                        nms_CraftItemStack = Class.forName(bukkitPackage+".inventory.CraftItemStack");
                        nms_CraftItemStack_asNMSCopy = nms_CraftItemStack.getMethod("asNMSCopy", ItemStack.class);

                        try {
                            nms_ItemStack = Class.forName("net.minecraft.world.item.ItemStack");
                        } catch (ClassNotFoundException ignored) {
                            nms_ItemStack = Class.forName("net.minecraft.server."+bukkitVersion+".ItemStack");
                        }
                        nms_ItemStack_getRepairCost = nms_ItemStack.getMethod("getRepairCost");
                    } catch (NoSuchMethodException | ClassNotFoundException e) {
                        System.out.println("Could not load NMS!");
                        e.printStackTrace();
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lAn internal server error occured. Please report this to an admin."));
                        return true;
                    }

                    // NMS REFLECTION MAGIC!
                    int repairCost = 0;
                    try {
                        Object magic_itemstack = nms_CraftItemStack_asNMSCopy.invoke(null, item);
                        repairCost = (int)nms_ItemStack_getRepairCost.invoke(magic_itemstack);

                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.out.println("NMS exception!");
                        e.printStackTrace();
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lAn internal server error occured. Please report this to an admin."));
                    }

                    // original NMS code for reference
                    //int repairCost = CraftItemStack.asNMSCopy(p.getInventory().getItemInMainHand()).getRepairCost();

                    if(NoAnvilLimitSpigot.debug) System.out.println("repairCost: "+repairCost);
                    int xp_left = p.getLevel() - (repairCost+1);
                    if(xp_left >= 0) {
                        p.setLevel(xp_left);
                        ItemMeta meta = item.getItemMeta();
                        String text = ColorThing.translate(Strings.join(args, " "));
                        if(NoAnvilLimitSpigot.hasColorPerms(p)) {
                            if(text.startsWith("&r")) {
                                text = ColorThing.translate(text);
                            } else {
                                text = ColorThing.translate("&o"+text);
                            }
                        }
                        assert meta != null;
                        meta.setDisplayName(text);
                        item.setItemMeta(meta);
                        p.playSound(targetedBlock.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, 1f);

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe item has been renamed."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have enough levels to rename that item! You still need &a"+(xp_left*-1)+"&c levels"));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have to hold an item that you want to rename!"));
                }
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have to look at an anvil to use that!"));
            }
        }
        return true;
    }
}
