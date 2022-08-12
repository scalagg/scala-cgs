package gg.scala.cgs.common.sponsor

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
abstract class SponsorPrize(val name: String, val cost: Int) {

    abstract fun canApply(player: Player): Boolean

    abstract fun apply(player: Player)

    abstract fun toItemStack(): ItemStack

}