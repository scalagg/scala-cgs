package gg.scala.cgs.common.sponsor.impl

import gg.scala.cgs.common.sponsor.SponsorPrize
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
class SponsorPrizeItem(private val item: ItemStack, name: String, cost: Int) : SponsorPrize(name, cost)
{
    override fun canApply(player: Player): Boolean
    {
        return true
    }

    override fun apply(player: Player)
    {
        player.inventory.addItem(item)
    }

    override fun toItemStack(): ItemStack
    {
        return item
    }
}