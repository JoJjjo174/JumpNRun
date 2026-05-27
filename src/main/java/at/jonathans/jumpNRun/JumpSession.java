package at.jonathans.jumpNRun;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class JumpSession {

    private Player player;
    private Location returnLocation;
    private int score;

    private Location pos1;
    private Location pos2;

    private Block currentBlock;
    private Block nextBlock;

    public JumpSession(Player player) {
        this.player = player;
        this.returnLocation = player.getLocation();

        JumpNRun plugin = JumpNRun.getInstance();

        pos1 = plugin.getConfig().getLocation("pos1");
        pos2 = plugin.getConfig().getLocation("pos2");

        currentBlock = generateStartingLocation().getBlock();
        currentBlock.setType(Material.WHITE_WOOL);

        generateNextBlock();

        player.teleport(
                currentBlock.getLocation().add(0,1,0)
        );

        score = 0;

        plugin.getJumpSessions().put(player, this);
    }

    private Location generateStartingLocation() {
        int minX = (int) Math.min( pos1.x(), pos2.x() );
        int maxX = (int) Math.max( pos1.x(), pos2.x() );

        int minY = (int) Math.min( pos1.y(), pos2.y() );
        int maxY = (int) Math.max( pos1.y(), pos2.y() );

        int minZ = (int) Math.min( pos1.z(), pos2.z() );
        int maxZ = (int) Math.max( pos1.z(), pos2.z() );

        Random rng = new Random();
        return new Location(
                pos1.getWorld(),
                rng.nextInt(minX, maxX+1),
                rng.nextInt(minY, maxY+1),
                rng.nextInt(minZ, maxZ+1)
        );
    }

    private void generateNextBlock() {
        nextBlock = pos1.getWorld().getBlockAt(generateLocation(currentBlock.getLocation()));
        nextBlock.setType(Material.WHITE_WOOL);
    }

    private Location generateLocation(Location from) {
        Random rng = new Random();

        int[] xzOffset = {-3, -2, 2, 3};
        int[] yOffset = {-1, 0, 1};

        Location newLocation;
        do {
            newLocation = from.clone().add(
                    xzOffset[rng.nextInt(xzOffset.length)],
                    yOffset[rng.nextInt(yOffset.length)],
                    xzOffset[rng.nextInt(xzOffset.length)]
            );

        } while (!isInBounds(newLocation));

        return newLocation;
    }

    public void endSession() {
        currentBlock.setType(Material.AIR);
        nextBlock.setType(Material.AIR);
        JumpNRun.getInstance().getJumpSessions().remove(player);
        player.teleport(returnLocation);
        player.sendMessage(String.format("You fell! You reached a score of %d", score));
    }

    public void checkNextJump() {
        if (!isInBounds(player.getLocation(), 1)) {
            endSession();
            return;
        }

        Location location = player.getLocation();

        if (nextBlock.equals(location.add(0,-1,0).getBlock())) {
            currentBlock.setType(Material.AIR);
            currentBlock = nextBlock;
            generateNextBlock();
            score++;

            Sound expSound = Sound.sound(
                    Key.key("entity.experience_orb.pickup"),
                    Sound.Source.PLAYER,
                    1.0f,
                    1.0f
            );
            player.playSound(expSound);
        }
    }

    private boolean isInBounds(Location location, int margin) {
        int minX = (int) Math.min( pos1.x(), pos2.x() ) - margin;
        int maxX = (int) Math.max( pos1.x(), pos2.x() ) + margin;

        //int minY = (int) Math.min(currentBlock.getLocation().y(), nextBlock.getLocation().y())-1;
        int minY = (int) Math.min( pos1.y(), pos2.y() );
        int maxY = (int) Math.max( pos1.y(), pos2.y() ) + margin;

        int minZ = (int) Math.min( pos1.z(), pos2.z() ) - margin;
        int maxZ = (int) Math.max( pos1.z(), pos2.z() ) + margin;

        int playerX =  (int) location.x();
        int playerY =  (int) location.y();
        int playerZ = (int) location.z();

        return maxX >= playerX && minX <= playerX
                && maxY >= playerY && minY <= playerY
                && maxZ >= playerZ && minZ <= playerZ;
    }

    private boolean isInBounds(Location location) {
        return isInBounds(location, 0);
    }

}
