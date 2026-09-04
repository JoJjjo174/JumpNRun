package at.jonathans.jumpNRun;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HologramBlock {

    private DyeColor colour;
    private Player player;

    private int entityId;
    private UUID fakeUuid;

    private Location location;


    public HologramBlock(Player player, DyeColor colour) {
        this.player = player;
        this.colour = colour;

        this.entityId = SpigotReflectionUtil.generateEntityId();
        this.fakeUuid = UUID.randomUUID();
    }

    public void spawn(Location location) {
        if (this.location != null) {
            return;
        }
        this.location = location;

        WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                fakeUuid,
                EntityTypes.BLOCK_DISPLAY,
                SpigotConversionUtil.fromBukkitLocation(location),
                0,
                0,
                new Vector3d(0, 0, 0)
        );

        List<EntityData<?>> displayMeta = new ArrayList<>();

        // Enable Glow
        displayMeta.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x40));

        // Set Glow Colour
        int colourRgb = colour.getColor().asRGB();
        displayMeta.add(new EntityData<>(22, EntityDataTypes.INT, colourRgb));

        // Set Glass Block
        int blockId = StateTypes.GLASS.createBlockState().getGlobalId();
        displayMeta.add(new EntityData<>(23, EntityDataTypes.BLOCK_STATE, blockId));

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, displayMeta);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntityPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
    }

    public void teleport(Location location) {
        if (this.location == null) {
            return;
        }
        this.location = location;

        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                entityId,
                SpigotConversionUtil.fromBukkitLocation(location),
                true
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, teleportPacket);
    }

    public void despawn() {
        if (location == null) {
            return;
        }
        this.location = null;

        WrapperPlayServerDestroyEntities despawnPacket = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, despawnPacket);
    }

    public Location getLocation() {
        return location;
    }

}
