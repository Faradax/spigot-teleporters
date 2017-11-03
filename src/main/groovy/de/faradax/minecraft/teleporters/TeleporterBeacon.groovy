package de.faradax.minecraft.teleporters

import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack

/**
 * Represents a Diamond Block with a filled item frame attached.
 */
class TeleporterBeacon {

    ItemStack id
    TeleporterBeacon target = this
    Block block
    boolean isActive = true

    boolean teleportPlayerToCounterpart(Entity player) {
        if (isActive && !(target.is(this))) {
            int airBlocksBelowBeacon = target.getSpaceBelowBeacon()
            if (airBlocksBelowBeacon < 2) {
                player.sendMessage("The target teleporter is blocked.")
                return false
            } else {
                def targetLocation = target.block.location.add(0.5, -airBlocksBelowBeacon, 0.5)
                teleportEntityToLocation(player, targetLocation)
                return true
            }
        }
        return false
    }

    private int getSpaceBelowBeacon() {
        def blockBelowBeacon = block.getRelative(BlockFace.DOWN)
        def airBlocksBelowBeacon = 0
        def maxDistance = 5
        def currentBlock = blockBelowBeacon
        while ((currentBlock.empty || currentBlock.type == Plugin.ACTIVATOR_MATERIAL) && airBlocksBelowBeacon < maxDistance) {
            currentBlock = currentBlock.getRelative(BlockFace.DOWN)
            airBlocksBelowBeacon++
        }
        return airBlocksBelowBeacon
    }


    private boolean teleportEntityToLocation(Entity entity, Location location) {
        location.direction = entity.location.direction
        entity.world.playEffect(entity.location, Effect.PORTAL_TRAVEL, 0)
        entity.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
        entity.world.playEffect(entity.location, Effect.PORTAL_TRAVEL, 0)
    }
}
