package gg.scala.cgs.common.rewards

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2022
 */
interface CoinRewardPlatform
{
    fun giveCoins(player: Player, amount: Int)
    fun getCoins(player: Player): Int
}
