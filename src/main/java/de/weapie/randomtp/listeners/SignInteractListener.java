package de.weapie.randomtp.listeners;

import de.weapie.randomtp.runnable.RandomTPRunnable;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.awt.*;

public class SignInteractListener implements Listener {

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if(block == null || block.getType() != Material.OAK_WALL_SIGN) {
            return;
        }

        Sign sign = (Sign) block.getState();
        if(sign.getSide(Side.FRONT).line(1).contains(Component.text("§8§l[§4RandomTP§8§l]"))) {
            return;
        }
        event.setCancelled(true);

        RandomTPRunnable randomTPRunnable = new RandomTPRunnable(player, player.getLocation(), true);
        Thread thread = new Thread(randomTPRunnable);
        thread.start();
    }

}
