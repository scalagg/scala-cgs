package gg.scala.cgs.common.rewards.impl

import gg.scala.cgs.common.rewards.CoinRewardPlatform
import gg.scala.game.extensions.profile.CorePlayerProfileService
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2022
 */
object CGECoinRewardPlatform : CoinRewardPlatform
{
    override fun giveCoins(player: Player, amount: Pair<Int, String>, notify: Boolean)
    {
        val profile = CorePlayerProfileService.find(player)
            ?: return

        profile.addCoins(
            amount.first,
            ""
        ) {
            if (notify)
            {
                player.sendMessage(
                    "$it${
                        if (amount.second.isNotEmpty()) " (${amount.second})" else "" 
                    }"
                )
            }
        }
    }

    override fun getCoins(player: Player) = CorePlayerProfileService.find(player)?.coins ?: 0
}
