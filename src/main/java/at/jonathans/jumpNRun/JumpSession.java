package at.jonathans.jumpNRun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class JumpSession {

    private Player player;
    private Location returnLocation;

    private Location pos1;
    private Location pos2;

    private Block currentBlock;
    private Block nextBlock;

    public JumpSession(Player player) {
        this.player = player;
        this.returnLocation = player.getLocation();

        Plugin plugin = JumpNRun.getInstance();

        pos1 = plugin.getConfig().getLocation("pos1");
        pos2 = plugin.getConfig().getLocation("pos2");

        currentBlock = generateStartingLocation().getBlock();
        currentBlock.setType(Material.WHITE_WOOL);

        generateNextBlock();

        player.teleport(
                currentBlock.getLocation().add(0,1,0)
        );
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

        Location newLocation = from.clone();
        while (from.equals(newLocation)) {
            newLocation = from.add(
                    rng.nextInt(-3, 4),
                    rng.nextInt(-1, 2),
                    rng.nextInt(-3, 4)
            );
        }

        return newLocation;
    }

    public void endSession() {
        currentBlock.setType(Material.AIR);
        nextBlock.setType(Material.AIR);
        player.teleport(returnLocation);
    }

}
