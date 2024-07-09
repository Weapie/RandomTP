package de.weapie.randomtp.searcher;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.weapie.randomtp.RandomTP;
import de.weapie.randomtp.searcher.exception.NotFoundException;
import de.weapie.randomtp.searcher.validators.LocationValidator;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ToString
public class RandomLocationSearcher {

    private final RandomTP plugin;
    @Getter
    private final UUID uniqueId = UUID.randomUUID();

    @Getter
    @Setter
    private Location startLocation;
    private int checkDelay = 1;
    private int currentTries = 0;
    private int maxTries = 10;
    @Getter
    private int minY = 0;
    @Getter
    private int maxY = 126;

    @Setter
    @Getter
    private Random random;

    private long lastCheck;
    private final CompletableFuture<Location> futureLocation = new CompletableFuture<>();
    private final Multimap<Integer, Integer> checked = MultimapBuilder.hashKeys().hashSetValues().build();
    private final ValidatorRegistry validators = new ValidatorRegistry();

    public RandomLocationSearcher(RandomTP plugin, Location startLocation, int maxTries, LocationValidator... validators) {
        Validate.notNull(startLocation, "StartLocation cannot be null");
        Validate.notNull(startLocation.getWorld(), "StartLocation world cannot be null");
        this.plugin = plugin;
        this.startLocation = startLocation;
        this.maxTries = maxTries;
        this.random = plugin.getRandom();

        switch (this.startLocation.getWorld().getEnvironment()) {
            case NORMAL -> this.maxY = this.startLocation.getWorld().getMaxHeight();
            case NETHER -> this.maxY = 126;
        }

        Arrays.asList(validators).forEach(this.validators::add);
    }

    public RandomLocationSearcher(RandomTP plugin, Location startLocation, LocationValidator... validators) {
        Validate.notNull(startLocation, "StartLocation cannot be null");
        Validate.notNull(startLocation.getWorld(), "StartLocation world cannot be null");
        this.plugin = plugin;
        this.startLocation = startLocation;
        this.random = plugin.getRandom();

        switch (this.startLocation.getWorld().getEnvironment()) {
            case NORMAL -> this.maxY = this.startLocation.getWorld().getMaxHeight();
            case NETHER -> this.maxY = 126;
        }

        Arrays.asList(validators).forEach(this.validators::add);
    }

    public CompletableFuture<Location> search() {
        if(this.plugin.getRunningSearches().containsKey(this.uniqueId)) {
            throw new IllegalStateException("Searcher " + this.uniqueId + " already running!");
        }

        this.plugin.getRunningSearches().put(this.uniqueId, this);

        this.checked.clear();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.checkRandomLocation(this.futureLocation));
        this.futureLocation.whenComplete((location, throwable) -> this.plugin.getRunningSearches().remove(this.uniqueId));
        return futureLocation;
    }

    private void checkRandomLocation(CompletableFuture<Location> future) {
        if(this.currentTries >= this.maxTries) {
            future.completeExceptionally(new NotFoundException("location"));
            return;
        }

        if(future.isCancelled() || future.isDone() || future.isCompletedExceptionally()) {
            return;
        }

        this.lastCheck = this.startLocation.getWorld().getTime();
        double x = this.randomMinMax(RandomTP.getInstance().getPluginConfig().value().getMinRadius(), RandomTP.getInstance().getPluginConfig().value().getMaxRadius());
        double z = this.randomMinMax(RandomTP.getInstance().getPluginConfig().value().getMinRadius(), RandomTP.getInstance().getPluginConfig().value().getMaxRadius());

        Location randomLocation = new Location(this.startLocation.getWorld(), x, this.minY, z);
        PaperLib.getChunkAtAsync(randomLocation).thenApply(chunk -> {
            this.currentTries++;

            if(chunk == null) {
                this.checkRandomLocation(future);
                return false;
            }

            chunk.addPluginChunkTicket(this.plugin);

            try {
                Location foundLocation = null;
                boolean validated = true;

                for (LocationValidator validator : this.validators.getAll()) {
                    if (!validator.validate(this, randomLocation)) {
                        validated = false;
                        break;
                    }
                }

                if (validated) {
                    foundLocation = randomLocation;
                }

                if(foundLocation != null) {
                    chunk.load();
                    future.complete(foundLocation);
                    return true;
                }

                long diff = this.startLocation.getWorld().getTime() - this.lastCheck;
                if (diff < this.checkDelay) {
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.checkRandomLocation(future), checkDelay - diff);
                } else {
                    this.checkRandomLocation(future);
                }

                return false;
            } finally {
                chunk.removePluginChunkTicket(this.plugin);
            }
        }).exceptionally(future::completeExceptionally);
    }

    private int randomMinMax(int minValue, int maxValue) {
        return this.random.nextInt((maxValue - minValue) + 1) + minValue;
    }

}
