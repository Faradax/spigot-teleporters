package de.faradax.minecraft.teleporters

import de.faradax.minecraft.teleporters.event.TeleporterListener
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by dockworker on 02.11.17.
 */
class Plugin extends JavaPlugin {

    @Override
    void onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(new TeleporterListener(this), this)

        def key = new NamespacedKey(this, "teleporter")
        ItemStack stack = getTeleporterItemStack()

        def recipe = new ShapedRecipe(key, stack)
                .shape("   ", " X ", " O ")
                .setIngredient('X' as char, Material.STONE_PLATE)
                .setIngredient('O' as char, Material.DIAMOND_BLOCK)
        server.addRecipe(recipe)
    }

    ItemStack getTeleporterItemStack() {
        def stack = new ItemStack(TeleporterListener.ACTIVATOR_MATERIAL)
        stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
        stack.amount = 2
        def meta = stack.itemMeta
        meta.displayName = "TeleporterBeacon"
        stack.setItemMeta(meta)
        stack
    }
}
