package de.weapie.randomtp.commands;

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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RandomTPCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if(player.getLocation().getWorld().getEnvironment() == World.Environment.THE_END) {
            player.sendMessage(RandomTP.getInstance().getPrefix() + "§cThis command is not allow in the end!");
            return false;
        }

        if(args.length == 0) {
            RandomTPRunnable randomTPRunnable = new RandomTPRunnable(player, player.getLocation(), false);
            Thread thread = new Thread(randomTPRunnable);
            thread.start();

            return true;
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("force")) {
                if(!player.hasPermission("de.weapie.randomtp")) {
                    player.sendMessage(RandomTP.getInstance().getPrefix() + "§cYou don't have the permission to perform this command!");
                    return false;
                }

                RandomTPRunnable randomTPRunnable = new RandomTPRunnable(player, player.getLocation(), true);
                Thread thread = new Thread(randomTPRunnable);
                thread.start();

                return true;
            } else if(args[0].equalsIgnoreCase("sign")) {
                if(!player.hasPermission("de.weapie.randomtp.sign")) {
                    player.sendMessage(RandomTP.getInstance().getPrefix() + "§cYou don't have the permission to perform this command!");
                    return false;
                }

                Block lookAtBlock = player.getTargetBlockExact(5);

                if(lookAtBlock == null || lookAtBlock.getType() != Material.OAK_WALL_SIGN) {
                    player.sendMessage(RandomTP.getInstance().getPrefix() + "§cYou don't select a sign!");
                    return false;
                }

                Sign sign = (Sign) lookAtBlock.getState();
                sign.setLine(0, "§8§m------------");
                sign.setLine(1, "§8§l[§4RandomTP§8§l]");
                sign.setLine(2, "§7Teleporting...");
                sign.setLine(3, "§8§m------------");
                sign.update();

                player.sendMessage(RandomTP.getInstance().getPrefix() + "§aYou create a teleport sign!");
                return true;
            }
        }

        player.sendMessage(RandomTP.getInstance().getPrefix() + "§cPlease use the command /randomtp!");
        return false;
    }

}
