package gg.scala.cgs.common.rewards.impl

import gg.scala.cgs.common.rewards.CoinRewardPlatform
import gg.scala.grape.GrapeSpigotPlugin
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2022
 */
object DefaultCoinRewardPlatform : CoinRewardPlatform
{
    override fun giveCoins(player: Player, amount: Pair<Int, String>, notify: Boolean)
    {
        TODO("Not yet implemented")
    }

    override fun getCoins(player: Player) = 0
}
