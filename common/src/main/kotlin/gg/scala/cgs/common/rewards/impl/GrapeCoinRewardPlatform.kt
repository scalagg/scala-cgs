package gg.scala.cgs.common.rewards.impl

import gg.scala.cgs.common.rewards.CoinRewardPlatform
import gg.scala.grape.GrapeSpigotPlugin
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2022
 */
object GrapeCoinRewardPlatform : CoinRewardPlatform
{
    override fun giveCoins(player: Player, amount: Int)
    {
        GrapeSpigotPlugin.getInstance()
            .playerHandler.getByPlayer(player)
            ?.apply {
                this.coins += amount
                this.save()
            }
    }

    override fun getCoins(player: Player) = GrapeSpigotPlugin.getInstance()
        .playerHandler.getByPlayer(player)?.coins ?: 0
}
