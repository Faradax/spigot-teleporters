package de.faradax.minecraft.teleporters.event

import de.faradax.minecraft.teleporters.Plugin
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class TeleporterListener implements Listener {

    private final Plugin plugin

    TeleporterListener(Plugin plugin) {
        this.plugin = plugin
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
                def canPlaceItem = wasEmpty && itemInMainHand
                if (canPlaceItem) {
                    if (plugin.isBlockTeleporterBeacon(block)) {
                        event.player.sendMessage "A strong energy prevents you from placing that item."
                        event.cancelled = true
                    } else {
                        plugin.addTeleporter(itemInMainHand, block)
                    }
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
                plugin.removeTeleporterByBlock(block)
            }
        }
    }

    @EventHandler
    void onTeleporterBeaconRemoval(BlockBreakEvent event) {
        def block = event.block
        def isDiamondBlock = block.type == Material.DIAMOND_BLOCK
        if (isDiamondBlock) {
            plugin.removeTeleporterByBlock(block)
        }
    }

    @EventHandler
    void onPlayerSteppedOnTeleporter(PlayerInteractEvent event) {
        if (event.action == Action.PHYSICAL) {
            def button = event.clickedBlock
            if (button.type == Plugin.ACTIVATOR_MATERIAL) {
                def teleporterBeaconBlock = button.getRelative(BlockFace.UP, 3)
                plugin.teleportPlayerFromBeaconBlock(event.player, teleporterBeaconBlock)
            }
        }
    }

    @EventHandler
    void onEntitySteppedOnTeleporter(EntityInteractEvent event) {
        def button = event.block
        if (button.type == Plugin.ACTIVATOR_MATERIAL) {
            def teleporterBeaconBlock = button.getRelative(BlockFace.UP, 3)
            plugin.teleportPlayerFromBeaconBlock(event.entity, teleporterBeaconBlock)
        }
    }
}
