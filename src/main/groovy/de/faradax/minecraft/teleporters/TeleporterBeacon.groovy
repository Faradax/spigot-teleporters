package de.faradax.minecraft.teleporters

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * Represents a Diamond Block with a filled item frame attached.
 */
class TeleporterBeacon {

    ItemStack id
    TeleporterBeacon counterpart
    Block block
    boolean isActive = true
}
