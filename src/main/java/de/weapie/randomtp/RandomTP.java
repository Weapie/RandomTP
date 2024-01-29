package de.weapie.randomtp;

import de.weapie.randomtp.commands.RandomTPCommand;
import de.weapie.randomtp.listeners.SignInteractListener;
import de.weapie.randomtp.searcher.RandomLocationSearcher;
import de.weapie.randomtp.searcher.ValidatorRegistry;
import de.weapie.randomtp.searcher.exception.NotFoundException;
import de.weapie.randomtp.searcher.validators.HeightValidator;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

public final class RandomTP extends JavaPlugin {

    @Getter
    private static RandomTP instance;

    @Getter
    private String prefix = "§a§lRandomTP §8➜ §7";
    @Getter
    private Map<UUID, RandomLocationSearcher> runningSearches = new HashMap<>();
    @Getter
    private final Random random = new Random();
    @Getter
    private ValidatorRegistry locationValidators = new ValidatorRegistry();


    private Location spawn = new Location(Bukkit.getWorld("world"), 0, 0, 0);
    private int cooldownTime = 15 * 1000; // 15 seconds
    private Map<Player, Long> cooldowns = new HashMap();

    private Material[] unsafeBlocks = new Material[]{ Material.LAVA, Material.FIRE, Material.CACTUS, Material.WATER, Material.MAGMA_BLOCK };

    @Override
    public void onEnable() {
        instance = this;

        this.initValidators();

        getCommand("randomtp").setExecutor(new RandomTPCommand());

        Bukkit.getPluginManager().registerEvents(new SignInteractListener(), this);
    }

    @Override
    public void onDisable() {

    }

    private void initValidators() {
        this.locationValidators.add(new HeightValidator(unsafeBlocks));
    }

    public RandomLocationSearcher run(Player player, Location startLocation, boolean forced) {
        RandomLocationSearcher searcher = new RandomLocationSearcher(this, startLocation, this.locationValidators.get("height"));

        if(!forced) {
            Location spawnPlayerLocation = this.spawn.clone();
            spawnPlayerLocation.setY(player.getLocation().getY());
            if (player.getLocation().distance(spawnPlayerLocation) > 30) {
                player.sendMessage(this.prefix + "§cYou are to far away from spawn!");
                return null;
            }

            if (cooldowns.containsKey(player)) {
                if (cooldowns.get(player) + cooldownTime > System.currentTimeMillis()) {
                    player.sendMessage(this.prefix + "§cCurrently you have a cooldown. Please try again later...");
                    return null;
                } else {
                    cooldowns.remove(player);
                }
            }
        }

        Bukkit.getScheduler().runTask(this, () -> player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(100000, 10000)));
        player.showTitle(Title.title(Component.text("§9§lSearching location..."), Component.text("§7This can take some time.")));
        searcher.search().thenApply(targetLocation -> {
            Location belowLocation = targetLocation.clone().subtract(0, 1, 0);
            Block belowBlock = belowLocation.getBlock();
            player.sendBlockChange(belowLocation, belowBlock.getBlockData());

            targetLocation.setX(targetLocation.getBlockX() + 0.5);
            targetLocation.setY(targetLocation.getBlockY() + 1);
            targetLocation.setZ(targetLocation.getBlockZ() + 0.5);

            getLogger().info("[DEBUG] Search " + searcher.getUniqueId() + " triggered by " + player.getName() + " will try to teleport " + player.getType() + " " + player.getName() + "/" + player.getUniqueId() + " to " + targetLocation);

            PaperLib.teleportAsync(player, targetLocation).whenComplete((success, ex) -> {
                if (success) {
                    if(!forced) {
                        cooldowns.put(player, System.currentTimeMillis());
                    }
                    player.showTitle(Title.title(Component.text("§2§lLocation found!"), Component.text("§7Teleport now.")));
                } else {
                    player.showTitle(Title.title(Component.text("§c§lNo location found!"), Component.text("§7Please try again.")));
                }
                Bukkit.getScheduler().runTaskLater(this, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), 40L);

                if (ex != null) {
                    getLogger().log(Level.SEVERE, "Error while trying to teleport to location!", ex);
                }
            });
            return true;
        }).exceptionally(ex -> {
            player.showTitle(Title.title(Component.text("§c§lNo location found!"), Component.text("§7Please try again.")));

            if (!(ex.getCause() instanceof NotFoundException)) {
                getLogger().log(Level.SEVERE, "Error while trying to find a location!", ex);
            }
            return true;
        });
        return searcher;
    }

}
