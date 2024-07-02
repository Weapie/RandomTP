package de.weapie.randomtp;

import de.weapie.devlok.files.config.DevlokConfig;
import de.weapie.randomtp.commands.RandomTPCommand;
import de.weapie.randomtp.config.PluginConfig;
import de.weapie.randomtp.config.module.MessageModule;
import de.weapie.randomtp.searcher.RandomLocationSearcher;
import de.weapie.randomtp.searcher.ValidatorRegistry;
import de.weapie.randomtp.searcher.exception.NotFoundException;
import de.weapie.randomtp.searcher.validators.HeightValidator;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

public final class RandomTP extends JavaPlugin {

    @Getter
    private static RandomTP instance;

    @Getter
    private DevlokConfig<PluginConfig> pluginConfig = DevlokConfig
            .builder(PluginConfig.class)
            .withPath("plugins/RandomTP/")
            .withDefault(() -> new PluginConfig(
                    true,
                    "§7[§c§lR§4§lT§4§lP§7] ",
                    5,
                    "randomtp.bypass",
                    "randomtp.admin",
                    new ArrayList<>() {{
                        add(new MessageModule("world_not_allowed", "§cYou can't perform this command in this world!"));
                        add(new MessageModule("cooldown_try_again", "§cCurrently you have a cooldown. Please try again later..."));
                        add(new MessageModule("title_searching_1", "§4§lSearching..."));
                        add(new MessageModule("title_searching_2", "§7This can take some time."));
                        add(new MessageModule("title_location_found_1", "§c§lYour new location:"));
                        add(new MessageModule("title_location_found_2", "§7X: §4%x§7, Y: §4%y§7, Z: §4%z"));
                        add(new MessageModule("title_no_location_found_1", "§4§lNo location found!"));
                        add(new MessageModule("title_no_location_found_2", "§7Please try again."));
                        add(new MessageModule("reload_config", "§7You reloaded the RandomTP-Config!"));
                        add(new MessageModule("help_admin_message", "§cPlease use the command /randomtp [reload]!"));
                        add(new MessageModule("help_message", "§cPlease use the command /randomtp!"));
                    }},
                    new String[]{ "world", "nether" },
                    new Material[]{ Material.LAVA, Material.FIRE, Material.CACTUS, Material.WATER, Material.MAGMA_BLOCK, Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES }
            ))
            .build();

    @Getter
    private String prefix;
    @Getter
    private Map<UUID, RandomLocationSearcher> runningSearches = new HashMap<>();
    @Getter
    private final Random random = new Random();
    @Getter
    private ValidatorRegistry locationValidators = new ValidatorRegistry();

    private Map<Player, Long> cooldowns = new HashMap();

    @Override
    public void onEnable() {
        instance = this;
        this.pluginConfig.write();
        this.pluginConfig.read();

        this.prefix = this.pluginConfig.value().getPrefix();

        this.initValidators();

        getCommand("randomtp").setExecutor(new RandomTPCommand());
        getCommand("rtp").setExecutor(new RandomTPCommand());
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void initValidators() {
        this.locationValidators.add(new HeightValidator(this.pluginConfig.value().getUnsafeBlocks()));
    }

    public RandomLocationSearcher run(Player player, Location startLocation, boolean forced) {
        RandomLocationSearcher searcher = new RandomLocationSearcher(this, startLocation, this.locationValidators.get("height"));

        if(!forced) {
            if (cooldowns.containsKey(player)) {
                if (cooldowns.get(player) + this.pluginConfig.value().getCooldown() * 1000 > System.currentTimeMillis()) {
                    player.sendMessage(this.prefix + this.message("cooldown_try_again"));
                    return null;
                } else {
                    cooldowns.remove(player);
                }
            }
        }

        Bukkit.getScheduler().runTask(this, () -> player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(100000, 10000)));
        player.showTitle(Title.title(Component.text(this.message("title_searching_1")), Component.text(this.message("title_searching_2"))));
        searcher.search().thenApply(targetLocation -> {
            Location belowLocation = targetLocation.clone().subtract(0, 1, 0);
            Block belowBlock = belowLocation.getBlock();
            player.sendBlockChange(belowLocation, belowBlock.getBlockData());

            targetLocation.setX(targetLocation.getBlockX() + 0.5);
            targetLocation.setY(targetLocation.getBlockY() + 1);
            targetLocation.setZ(targetLocation.getBlockZ() + 0.5);

            if(this.pluginConfig.value().isConsoleOutput()) {
                getLogger().info("[RTP] Search " + searcher.getUniqueId() + " triggered by " + player.getName() + " will try to teleport " + player.getType() + " " + player.getName() + "/" + player.getUniqueId() + " to " + targetLocation);
            }

            PaperLib.teleportAsync(player, targetLocation).whenComplete((success, ex) -> {
                if (success) {
                    if(!forced) {
                        cooldowns.put(player, System.currentTimeMillis());
                    }
                    player.showTitle(
                            Title.title(
                                    Component.text(
                                            this.message("title_location_found_1")
                                                    .replaceAll("%x", "" + targetLocation.getBlockX())
                                                    .replaceAll("%y", "" + targetLocation.getBlockY())
                                                    .replaceAll("%z", "" + targetLocation.getBlockZ())
                                    ),
                                    Component.text(
                                            this.message("title_location_found_2")
                                                    .replaceAll("%x", "" + targetLocation.getBlockX())
                                                    .replaceAll("%y", "" + targetLocation.getBlockY())
                                                    .replaceAll("%z", "" + targetLocation.getBlockZ())
                                    )
                            )
                    );
                } else {
                    player.showTitle(Title.title(Component.text(this.message("title_no_location_found_1")), Component.text(this.message("title_no_location_found_2"))));
                }
                Bukkit.getScheduler().runTaskLater(this, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), 40L);

                if (ex != null) {
                    if(this.pluginConfig.value().isConsoleOutput()) {
                        getLogger().log(Level.SEVERE, "[RTP] Error while trying to teleport to location!", ex);
                    }
                }
            });
            return true;
        }).exceptionally(ex -> {
            player.showTitle(Title.title(Component.text(this.message("title_no_location_found_1")), Component.text(this.message("title_no_location_found_2"))));

            if (!(ex.getCause() instanceof NotFoundException)) {
                if(this.pluginConfig.value().isConsoleOutput()) {
                    getLogger().log(Level.SEVERE, "[RTP] Error while trying to find a location!", ex);
                }
            }
            return true;
        });
        return searcher;
    }

    public String message(String name) {
        return this.pluginConfig.value().getMessages().stream().filter(messageModule -> messageModule.getName().equals(name)).findFirst().orElse(new MessageModule("no_message", "§cNo message found, please report this to a team member!")).getMessage();
    }

}
