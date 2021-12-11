package gg.scala.potato.player

import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object HotPotatoItems
{
    @JvmStatic
    val POTATO = ItemBuilder(Material.BAKED_POTATO)
        .name("${CC.B_GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.B_GREEN}Hot Potato ${CC.B_GRAY}${Constants.DOUBLE_ARROW_LEFT}")
        .addToLore("${CC.GRAY}Punch a player to transfer this potato!")
        .build()
}
