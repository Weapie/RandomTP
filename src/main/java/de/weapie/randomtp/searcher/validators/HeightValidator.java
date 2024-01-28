package de.weapie.randomtp.searcher.validators;

import de.weapie.randomtp.searcher.RandomLocationSearcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Collections;
import java.util.EnumSet;

public class HeightValidator extends LocationValidator {

    private final EnumSet<Material> unsafeBlocks;

    public HeightValidator(Material[] unsafeBlocks) {
        super("height");
        this.unsafeBlocks = EnumSet.noneOf(Material.class);
        Collections.addAll(this.unsafeBlocks, unsafeBlocks);
    }

    @Override
    public boolean validate(RandomLocationSearcher searcher, Location location) {
        Block block = location.getWorld().getHighestBlockAt(location);
        if (block.getY() > searcher.getMaxY()) {
            block = location.getWorld().getBlockAt(
                    block.getX(),
                    searcher.getMinY() + searcher.getRandom().nextInt(searcher.getMaxY() - searcher.getMinY()),
                    block.getZ()
            );
        }
        while (block.isEmpty()) {
            block = block.getRelative(BlockFace.DOWN);
            if (block == null || block.getY() < searcher.getMinY()) {
                return false;
            }
        }
        location.setY(block.getY());
        return isSafeBlockBelow(block) && isSafe(block.getRelative(BlockFace.UP)) && isSafe(block.getRelative(BlockFace.UP, 2));
    }

    private boolean isSafe(Block block) {
        return block.isPassable() && !block.isLiquid() && !unsafeBlocks.contains(block.getType());
    }

    private boolean isSafeBlockBelow(Block block) {
        return !block.isPassable() && !block.isLiquid() && !unsafeBlocks.contains(block.getType());
    }

}
