package io.github.electroluxv2.BotanyGrow.runable;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.settings.Settings;
import io.github.electroluxv2.BotanyGrow.utils.BotanyTier;
import io.github.electroluxv2.BotanyGrow.utils.NeighbourInfo;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class FloraPopulate extends BukkitRunnable {

    private static int random(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void run() {
        if (MainPlugin.blocksToPopulate.size() == 0) return;
        int randomIndex = random(0, MainPlugin.blocksToPopulate.size() - 1);
        Location l = MainPlugin.blocksToPopulate.get(randomIndex);
        MainPlugin.blocksToPopulate.remove(randomIndex);

        World w = l.getWorld();
        if (w == null) return;

        Chunk c = w.getChunkAt(l);
        if (!c.isLoaded()) return;

        // Block that is currently spreading
        Block o = w.getBlockAt(l);

        // Block could be destroyed until now
        if (o.isEmpty()) return;

        // Block may be completely different until now
        if (!Settings.materialsToScan.contains(o.getType())) return;

        // Check for spots
        ArrayList<Block> acceptableSpots = new ArrayList<>();
        int offsetX = 2, offsetZ = 2, offsetY = 1;

        for (int x = (int) (l.getX() - offsetX); x <= l.getX() + offsetX; x++) {
            for (int z = (int) (l.getZ() - offsetZ); z <= l.getZ() + offsetZ; z++) {
                for (int y = (int) (l.getY() - offsetY); y < l.getY() + offsetY; y++) {
                    Block possibleSpot = w.getBlockAt(x, y, z);
                    if (!possibleSpot.getChunk().isLoaded()) continue;

                    // Can spread on only free space
                    if (!possibleSpot.isEmpty()) continue;

                    Block blockUnder = possibleSpot.getRelative(0, -1, 0);

                    // Has to be place able
                    if (Settings.placeAbleMaterials.get(o.getType()) == null) continue;
                    if (!Settings.placeAbleMaterials.get(o.getType()).contains(blockUnder.getType())) continue;

                    acceptableSpots.add(possibleSpot);
                }
            }
        }

        // Count neighbours
        NeighbourInfo neighbourInfo = new NeighbourInfo(o);

        // No spot
        if (acceptableSpots.size() == 0) {
            // Maybe just die instead of providing problems with spread?
            this.tierControl(o, neighbourInfo);
            return;
        }

        // Grow mechanics
        this.tierControl(o, neighbourInfo, acceptableSpots);
    }



    /**
     * Controls tier of flora at o Block
     * @param o Block to control (tier up or down)
     * @param neighbourInfo Contains all information about neighbors needed to check requirements
     * @param acceptableSpots Contains all free spots around Block o (including wrong spots for just spread)
     */
    private void tierControl(Block o, NeighbourInfo neighbourInfo, @Nullable ArrayList<Block> acceptableSpots) {

        // Match current tier for  Block o
        BotanyTier currentTier = Settings.tiers.get(o.getType());

        if (currentTier == null) {
            MainPlugin.logger.warning("Unsupported tier of grow with material: " + o.getType());
            return;
        }

        // Max tier
        if (!currentTier.spread && currentTier.next.size() == 0) {
            return;
        }

        // TODO: Shuffle array
        ArrayList<BotanyTier> nextTiers = currentTier.matchNext(o, neighbourInfo);

        // Spots to spread exists
        if (acceptableSpots != null) {

            // We have to remove free spots that doesn't meet requirements for spread
            ArrayList<Block> spotsToSpread = new ArrayList<>();
            for (Block spot : acceptableSpots) {
                if (currentTier.canSpreadOn(spot, new NeighbourInfo(spot))) {
                    spotsToSpread.add(spot);
                }
            }

            if (nextTiers.size() == 0) {
                // Then spread but not die
                if (currentTier.spread) spread(o, spotsToSpread);
                return;
            }

            // Not every grow can be done (eg. multi blocks) so try all of possible
            boolean grownSuccess = false;
            for (BotanyTier tier : nextTiers) {
                if (tierUp(o, tier)) {
                    grownSuccess = true;
                    break;
                }
            }

            if (!grownSuccess) {
                // Then spread but not die
                if (currentTier.spread) spread(o, spotsToSpread);
            }

            // All done with this case
            return;
        }

        // No spots
        // Not every grow can be done (eg. multi blocks) so try all of possible
        boolean grownSuccess = false;
        for (BotanyTier tier : nextTiers) {
            if (tierUp(o, tier)) {
                grownSuccess = true;
                break;
            }
        }

        if (grownSuccess) return;

        // No grow, no spread, maybe you should die?
        // Check if meets requirements for current tier

        boolean meets = true;

        // TODO

        if (!meets) tierDown(o);
    }

    private void tierControl(Block o, NeighbourInfo neighbourInfo) {
        tierControl(o, neighbourInfo, null);
    }

    private boolean spread(Block o, ArrayList<Block> acceptableSpots) {
        if (acceptableSpots.size() == 0) {
            return false;
        }

        // Rand new spot
        int indexForNewSpot = random(0, acceptableSpots.size() - 1);
        Block target = acceptableSpots.get(indexForNewSpot);

        target.setType(o.getType());
        return true;
    }

    /**
     * Tries to increase tier of Block o
     * @param o Block that is currently growing
     * @param tier Target tier
     * @return result of try, false if block remained unchanged
     */
    private boolean tierUp(Block o, BotanyTier tier) {

        // Multi blocks
        if (Settings.multiBlocks.contains(tier.material)) {
            // Need space above o
            Block upper = o.getRelative(0,1,0);


            if (!upper.isEmpty()) {
                //MainPlugin.logger.info("TierUp failed: " + o.getType() + " => " + tier.material);
                return false;
            }

            //MainPlugin.logger.info("TierUp success: " + o.getType() + " => " + tier.material);
            o.setType(tier.material, false);
            Bisected lowerData = (Bisected) o.getBlockData();
            lowerData.setHalf(Bisected.Half.BOTTOM);
            o.setBlockData(lowerData);

            upper.setType(tier.material, false);
            Bisected upperData = (Bisected) upper.getBlockData();
            upperData.setHalf(Bisected.Half.TOP);
            upper.setBlockData(upperData);
        } else {
            //MainPlugin.logger.info("TierUp success: " + o.getType() + " => " + tier.material);
            o.setType(tier.material, false);
        }

        return true;
    }

    /**
     * Decreases the tier of Block o
     * @param o Block that is currently dying
     */
    private void tierDown(Block o) {

    }
}
