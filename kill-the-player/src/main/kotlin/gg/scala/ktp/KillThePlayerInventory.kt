package gg.scala.ktp

import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerInventory
{
    @JvmStatic
    val ARMOR = arrayOf(
        ItemBuilder(Material.LEATHER_BOOTS)
            .color(Color.RED).build(),
        ItemBuilder(Material.LEATHER_LEGGINGS)
            .color(Color.RED).build(),
        ItemBuilder(Material.LEATHER_CHESTPLATE)
            .color(Color.RED).build(),
        ItemBuilder(Material.LEATHER_HELMET)
            .color(Color.RED).build(),
    )

    @JvmStatic
    val SWORD = ItemStack(Material.STONE_SWORD)
}
