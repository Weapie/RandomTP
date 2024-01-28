package de.weapie.randomtp.runnable;

import de.weapie.randomtp.RandomTP;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RandomTPRunnable implements Runnable {

    private Player player;
    private Location startLocation;
    private boolean forced;

    public RandomTPRunnable(Player player, Location startLocation, boolean forced) {
        this.player = player;
        this.startLocation = startLocation;
        this.forced = forced;
    }

    public void run() {
        RandomTP.getInstance().run(this.player, this.startLocation, this.forced);
    }
}
