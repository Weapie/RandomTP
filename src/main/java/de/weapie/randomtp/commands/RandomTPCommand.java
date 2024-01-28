package de.weapie.randomtp.commands;

import de.weapie.randomtp.RandomTP;
import de.weapie.randomtp.runnable.RandomTPRunnable;
import it.unimi.dsi.fastutil.ints.IntSpliterators;
import org.bukkit.World;
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
                RandomTPRunnable randomTPRunnable = new RandomTPRunnable(player, player.getLocation(), true);
                Thread thread = new Thread(randomTPRunnable);
                thread.start();

                return true;
            }
        }

        player.sendMessage(RandomTP.getInstance().getPrefix() + "§cPlease use the command /randomtp!");
        return false;
    }

}
