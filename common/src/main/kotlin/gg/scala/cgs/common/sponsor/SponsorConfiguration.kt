package gg.scala.cgs.common.sponsor

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
interface SponsorConfiguration
{
    fun getSponsorAmount(): Int
    fun handleSponsorPrize(player: Player, target: Player)
}
