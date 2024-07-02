package de.weapie.randomtp.commands;

import com.google.gson.JsonSyntaxException;
import de.weapie.randomtp.RandomTP;
import de.weapie.randomtp.runnable.RandomTPRunnable;
import it.unimi.dsi.fastutil.ints.IntSpliterators;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;

public class RandomTPCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof ConsoleCommandSender) return false;

        Player player = (Player) sender;

        if(args.length == 0) {
            if(!Arrays.stream(RandomTP.getInstance().getPluginConfig().value().getAllowedWorlds()).anyMatch(s -> s.equals(player.getLocation().getWorld().getName()))) {
                player.sendMessage(RandomTP.getInstance().getPrefix() + RandomTP.getInstance().message("world_not_allowed"));
                return false;
            }

            RandomTPRunnable randomTPRunnable = new RandomTPRunnable(player, player.getLocation(), player.hasPermission(RandomTP.getInstance().getPluginConfig().value().getBypassPermission()));
            Thread thread = new Thread(randomTPRunnable);
            thread.start();

            return true;
        } else if(args.length == 1) {
            if(player.hasPermission(RandomTP.getInstance().getPluginConfig().value().getAdminPermission())) {
                if (args[0].equals("reload")) {
                    try {
                        RandomTP.getInstance().getPluginConfig().read();
                        player.sendMessage(RandomTP.getInstance().getPrefix() + RandomTP.getInstance().message("reload_config"));
                        return true;
                    } catch (JsonSyntaxException ex) {
                        player.sendMessage(RandomTP.getInstance().getPrefix() + "§cPlease check the configuration, something went wrong!");
                        return false;
                    }
                }

                player.sendMessage(RandomTP.getInstance().getPrefix() + RandomTP.getInstance().message("help_admin_message"));
                return false;
            }
        }

        player.sendMessage(RandomTP.getInstance().getPrefix() + RandomTP.getInstance().message("help_message"));
        return false;
    }

}
