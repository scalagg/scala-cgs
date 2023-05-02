package gg.scala.cgs.common.rewards.impl

import gg.scala.cgs.common.rewards.CoinRewardPlatform
import gg.scala.grape.GrapeSpigotPlugin
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2022
 */
object GrapeCoinRewardPlatform : CoinRewardPlatform
{
    override fun giveCoins(player: Player, amount: Pair<Int, String>, notify: Boolean)
    {
        GrapeSpigotPlugin.getInstance()
            .playerHandler.getByPlayer(player)
            ?.apply {
                this.coins += amount.first
                this.save()
            }


        if (notify)
        {
            player.sendMessage(
                "${CC.GOLD}+${amount.first} coins (${amount.second})!"
            )
        }
    }

    override fun getCoins(player: Player) = GrapeSpigotPlugin.getInstance()
        .playerHandler.getByPlayer(player)?.coins ?: 0
}
