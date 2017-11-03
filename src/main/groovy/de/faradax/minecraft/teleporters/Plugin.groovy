package de.faradax.minecraft.teleporters

import de.faradax.minecraft.teleporters.event.TeleporterListener
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class Plugin extends JavaPlugin {

    public static final Material ACTIVATOR_MATERIAL = Material.STONE_PLATE

    List<TeleporterBeacon> teleporters = []

    @Override
    void onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(new TeleporterListener(this), this)
    }

    void addTeleporter(ItemStack teleporterId, Block beaconBlock) {
        def teleporter = new TeleporterBeacon(id: teleporterId, block: beaconBlock)

        def teleporterGroup = teleporters.findAll {
            it.id.isSimilar(teleporterId)
        }

        if (!teleporterGroup.empty) {

            def lastTeleporter = teleporterGroup.last()
            def firstTeleporter = lastTeleporter.target

            lastTeleporter.target = teleporter
            teleporter.target = firstTeleporter
        }

        teleporters.add(teleporter)
    }

    boolean isBlockTeleporterBeacon(Block block) {
        teleporters.any { it.block == block }
    }

    void removeTeleporterByBlock(Block beaconBlock) {
        def teleporter = teleporters.find { it.block == beaconBlock }
        if (teleporter) {

            def source = teleporters.find { it.target == teleporter }
            source.target = teleporter.target
            teleporters.remove(teleporter)
        }
    }

    def teleportPlayerFromBeaconBlock(Entity player, Block beaconBlock) {
        def teleporterBeacon = teleporters.find { it.block == beaconBlock }
        if (teleporterBeacon) {
            def teleportedSuccessfully = teleporterBeacon.teleportPlayerToCounterpart(player)
            if (teleportedSuccessfully) {
                teleporterBeacon.target.isActive = false
                server.scheduler.scheduleSyncDelayedTask(this, {
                    teleporterBeacon.target.isActive = true
                }, 20)
            }
        }
    }
}