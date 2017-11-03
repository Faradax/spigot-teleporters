package de.faradax.minecraft.teleporters.event

import de.faradax.minecraft.teleporters.Plugin
import de.faradax.minecraft.teleporters.TeleporterBeacon
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack

class TeleporterListener implements Listener {

    public static final Material ACTIVATOR_MATERIAL = Material.STONE_PLATE

    Collection<TeleporterBeacon> teleporters = []

    private final Plugin plugin

    TeleporterListener(Plugin plugin) {
        this.plugin = plugin
    }

    void addTeleporter(ItemStack teleporterId, Block beaconBlock) {
        def teleporter = new TeleporterBeacon(id: teleporterId, block: beaconBlock)

        def pendingTeleporter = teleporters.find {
            it.id.isSimilar(teleporterId)
        }

        teleporters.add(teleporter)

        if (pendingTeleporter) {
            pendingTeleporter.counterpart = teleporter
            teleporter.counterpart = pendingTeleporter
        }
    }

    void removeTeleporterByBlock(Block beaconBlock) {
        def teleporter = teleporters.find { it.block == beaconBlock }
        if (teleporter) {
            teleporter.counterpart?.counterpart = null
            teleporters.remove(teleporter)
        }
    }

    @EventHandler
    void onTeleporterBeaconCompletion(PlayerInteractEntityEvent event) {
        def isItemFrameEvent = event.rightClicked instanceof ItemFrame
        if (isItemFrameEvent) {
            def itemFrame = event.rightClicked as ItemFrame
            def block = itemFrame.location.block.getRelative(itemFrame.attachedFace)
            def isHungOnDiamondBlock = block.type == Material.DIAMOND_BLOCK
            if (isHungOnDiamondBlock) {
                def wasEmpty = itemFrame.item.isSimilar(new ItemStack(Material.AIR))
                def itemInMainHand = event.player.inventory.itemInMainHand
                def hasItemPlaced = wasEmpty && itemInMainHand
                if (hasItemPlaced) {
                    addTeleporter(itemInMainHand, block)
                }
            }
        }

    }

    @EventHandler
    void onTeleporterBeaconRemoval(EntityDamageByEntityEvent event) {
        def isItemFrameEvent = event.entity instanceof ItemFrame
        if (isItemFrameEvent) {
            def itemFrame = event.entity as ItemFrame
            def block = itemFrame.location.block.getRelative(itemFrame.attachedFace)
            def isHungOnDiamondBlock = block.type == Material.DIAMOND_BLOCK
            if (isHungOnDiamondBlock) {
                removeTeleporterByBlock(block)
            }
        }
    }

    @EventHandler
    void onTeleporterBeaconRemoval(BlockBreakEvent event) {
        def block = event.block
        def isDiamondBlock = block.type == Material.DIAMOND_BLOCK
        if (isDiamondBlock) {
            removeTeleporterByBlock(block)
        }
    }

    @EventHandler
    void onPlayerSteppedOnTeleporter(PlayerInteractEvent event) {
        if (event.action == Action.PHYSICAL) {
            def button = event.clickedBlock
            if (button.type == ACTIVATOR_MATERIAL) {
                def teleporterBeacon = button.getRelative(BlockFace.UP, 3)
                def assignedTeleporter = teleporters.find { it.block == teleporterBeacon }
                if (assignedTeleporter && assignedTeleporter.isActive) {
                    def blockBelowBeacon = assignedTeleporter.counterpart.block.getRelative(BlockFace.DOWN)
                    def airBlocksBelowBeacon = 0
                    def maxDistance = 5
                    def currentBlock = blockBelowBeacon
                    while (!currentBlock.type.occluding && airBlocksBelowBeacon < maxDistance) {
                        currentBlock = currentBlock.getRelative(BlockFace.DOWN)
                        airBlocksBelowBeacon++
                    }
                    if (airBlocksBelowBeacon < 2) {
                        event.player.sendMessage("The target teleporter is blocked.")
                    } else {
                        def targetLocation = assignedTeleporter.counterpart.block.location.add(0.5, -airBlocksBelowBeacon, 0.5)
                        def entity = event.player

                        assignedTeleporter.counterpart.isActive = false
                        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
                            assignedTeleporter.counterpart.isActive = true
                        }, 20)

                        teleportEntityToLocation(entity, targetLocation)
                    }
                }
            }
        }
    }

    private boolean teleportEntityToLocation(Entity entity, Location location) {
        location.direction = entity.location.direction
        entity.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }

}
