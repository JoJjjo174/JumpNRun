package at.jonathans.jumpNRun;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

// !!! Currently Spawning hologram whether enabled or not in config

public class JumpSession {

    private Player player;
    private Location returnLocation;
    private int originalFoodLevel;
    private Collection<PotionEffect> originalEffects;
    private int score;
    private BossBar scoreBar;

    private DyeColor colour;
    private Material colourMaterial;

    private Location pos1;
    private Location pos2;

    private Block currentBlock;
    private Block nextBlock;
    private HologramBlock hologramBlock;

    public JumpSession(Player player) {
        this.player = player;
        this.returnLocation = player.getLocation();

        JumpNRun plugin = JumpNRun.getInstance();
        colour = plugin.getRandomColour();
        colourMaterial = getMaterialFromColour(colour);

        pos1 = plugin.getConfig().getLocation("pos1");
        pos2 = plugin.getConfig().getLocation("pos2");

        Location startingLocation = generateStartingLocation();
        Location nextLocation = generateLocation(startingLocation);
        Location hologramLocation = generateLocation(nextLocation);

        if (startingLocation == null || nextLocation == null || hologramLocation == null) {
            player.sendMessage(Message.noSpace());
            return;
        }

        currentBlock = startingLocation.getBlock();
        currentBlock.setType(colourMaterial);

        nextBlock = nextLocation.getBlock();
        nextBlock.setType(colourMaterial);

        hologramBlock = new HologramBlock(player, colour);
        hologramBlock.spawn(hologramLocation);


        if (plugin.getConfig().getBoolean("enable-bossbar")) {
            scoreBar = BossBar.bossBar(Message.bossbarText(score), 1f, getBossBarColor(colour), BossBar.Overlay.PROGRESS);
            player.showBossBar(scoreBar);
        }

        player.teleport(
                currentBlock.getLocation().add(0,1,0)
        );

        if (plugin.getConfig().getBoolean("disable-hunger")) {
            this.originalFoodLevel = player.getFoodLevel();
            player.setFoodLevel(20);
        }
        if (plugin.getConfig().getBoolean("disable-potions")) {
            this.originalEffects = player.getActivePotionEffects();
            player.clearActivePotionEffects();
        }

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
        Location location;
        for (int i = 0; i < 100; i++) {
            location = new Location(
                    pos1.getWorld(),
                    rng.nextInt(minX, maxX+1),
                    rng.nextInt(minY, maxY+1),
                    rng.nextInt(minZ, maxZ+1)
            );
            if (location.getBlock().getType().equals(Material.AIR)) {
                return location;
            }
        }
        return null;
    }

    private boolean generateNextBlock() {
        nextBlock = pos1.getWorld().getBlockAt(hologramBlock.getLocation());
        nextBlock.setType(colourMaterial);

        Location nextLocation = generateLocation(nextBlock.getLocation());
        if (nextLocation != null) {
            hologramBlock.teleport(nextLocation);
            return true;
        }

        return false;
    }

    private Vector generateEasyJump() {
        int[] xzOffset = {-3, -2, 2, 3};
        int[] yOffset = {-1, 0, 1};

        Random rng = new Random();

        return new Vector(
                xzOffset[rng.nextInt(xzOffset.length)],
                yOffset[rng.nextInt(yOffset.length)],
                xzOffset[rng.nextInt(xzOffset.length)]
        );
    }

    private Vector generateHardJump() {
        int[][] jumps = {
                {5,0,0}, {0,0,5},
                {5,0,1}, {1,0,5},
                {5,0,2}, {2,0,5}
        };
        int[] signs = {-1, 1};

        Random rng = new Random();

        int[] jump = jumps[rng.nextInt(jumps.length)];

        return new Vector(
                jump[0] * signs[rng.nextInt(signs.length)],
                jump[1] * signs[rng.nextInt(signs.length)],
                jump[2] * signs[rng.nextInt(signs.length)]
        );
    }

    private Location generateLocation(Location from) {
        if (from == null) {
            return null;
        }

        Random rng = new Random();

        double hardChance = getHardJumpChance();
        boolean hardJump = hardChance >= rng.nextDouble();

        Location newLocation;
        for (int i = 0; i < 100; i++) {
            newLocation = from.clone().add(
                    hardJump ? generateHardJump() : generateEasyJump()
            );

            if ( pos1.getWorld().getBlockAt(newLocation).getType().equals(Material.AIR) && isInBounds(pos1, pos2, newLocation)) {
                return newLocation;
            }
        }

        return null;
    }

    public void endSession(boolean runSync) {
        currentBlock.setType(Material.AIR);
        nextBlock.setType(Material.AIR);
        hologramBlock.despawn();

        JumpNRun plugin = JumpNRun.getInstance();

        plugin.getJumpSessions().remove(player);
        player.teleport(returnLocation);

        if (scoreBar != null) {
            player.hideBossBar(scoreBar);
        }
        if (originalFoodLevel != 0) {
            player.setFoodLevel(originalFoodLevel);
        }
        if (originalEffects != null) {
            player.addPotionEffects(originalEffects);
        }

        CompletableFuture<Boolean> brokeHighscoreFuture = plugin.getDatabase().brokeHighscore(player, score);

        if (runSync) {
            brokeHighscoreFuture.join();
            return;
        }

        brokeHighscoreFuture.thenAccept(result -> {
            if (result) {
                player.sendMessage(Message.brokeHighscoreMessage(score));
            } else {
                int highscore = plugin.getDatabase().getHighscore(player).join();
                player.sendMessage(Message.scoreMessage(score, highscore));
            }
        });

    }

    public void checkNextJump() {
        if (!isInPlayerBounds(player.getLocation())) {
            endSession(false);
            return;
        }

        if (!onNextBlock()) {
            return;
        }

        currentBlock.setType(Material.AIR);
        currentBlock = nextBlock;
        score++;
        if (scoreBar != null) {
            scoreBar.name(Message.bossbarText(score));
        }

        Sound expSound = Sound.sound(
                Key.key("entity.experience_orb.pickup"),
                Sound.Source.PLAYER,
                1.0f,
                1.0f
        );
        player.playSound(expSound);

        if (!generateNextBlock()) {
            player.sendMessage(Message.noSpace());
            endSession(false);
        }
    }

    private boolean onNextBlock() {
        BoundingBox playerBoundingBox = player.getBoundingBox();
        BoundingBox nextBlockBoundingBox = nextBlock.getBoundingBox();

        return playerBoundingBox.shift(0, -0.1, 0).overlaps(nextBlockBoundingBox);
    }

    public static boolean isInBounds(Location pos1, Location pos2, Location location) {
        int minX = (int) Math.min( pos1.x(), pos2.x() );
        int maxX = (int) Math.max( pos1.x(), pos2.x() );

        int minY = (int) Math.min( pos1.y(), pos2.y() );
        int maxY = (int) Math.max( pos1.y(), pos2.y() );

        int minZ = (int) Math.min( pos1.z(), pos2.z() );
        int maxZ = (int) Math.max( pos1.z(), pos2.z() );

        int locX =  (int) location.x();
        int locY =  (int) location.y();
        int locZ = (int) location.z();

        return maxX >= locX && minX <= locX
                && maxY >= locY && minY <= locY
                && maxZ >= locZ && minZ <= locZ;
    }

    private boolean isInPlayerBounds(Location location) {
        int minX = (int) Math.min( currentBlock.getLocation().x(), nextBlock.getLocation().x() ) - 3;
        int maxX = (int) Math.max( currentBlock.getLocation().x(), nextBlock.getLocation().x() ) + 3;

        int minY = (int) Math.min( currentBlock.getLocation().y(), nextBlock.getLocation().y() ) - 1;
        int maxY = (int) Math.max( currentBlock.getLocation().y(), nextBlock.getLocation().y() ) + 3;

        int minZ = (int) Math.min( currentBlock.getLocation().z(), nextBlock.getLocation().z() ) - 3;
        int maxZ = (int) Math.max( currentBlock.getLocation().z(), nextBlock.getLocation().z() ) + 3;

        int locX =  (int) location.x();
        int locY =  (int) location.y();
        int locZ = (int) location.z();

        return maxX >= locX && minX <= locX
                && maxY >= locY && minY <= locY
                && maxZ >= locZ && minZ <= locZ;
    }

    private static Material getMaterialFromColour(DyeColor colour) {
        return Material.valueOf(colour.name() + "_" + JumpNRun.getInstance().getConfig().getString("block-material").toUpperCase());
    }

    private static BossBar.Color getBossBarColor(DyeColor dyeColor) {
        return switch (dyeColor) {
            case PINK -> BossBar.Color.PINK;
            case BLUE, CYAN, LIGHT_BLUE -> BossBar.Color.BLUE;
            case RED -> BossBar.Color.RED;
            case GREEN, LIME -> BossBar.Color.GREEN;
            case YELLOW, ORANGE, BROWN -> BossBar.Color.YELLOW;
            case PURPLE, MAGENTA -> BossBar.Color.PURPLE;
            case WHITE, BLACK, GRAY, LIGHT_GRAY -> BossBar.Color.WHITE;

            default -> BossBar.Color.WHITE;
        };
    }

    public int getScore() {
        return score;
    }

    public double getHardJumpChance() {
        return Math.max(0.333 - Math.exp(-0.05*score), 0);
    }

}
