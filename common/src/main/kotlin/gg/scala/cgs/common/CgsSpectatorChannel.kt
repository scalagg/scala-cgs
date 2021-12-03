package gg.scala.cgs.common

import gg.scala.lemon.player.channel.ChannelOverride
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsSpectatorChannel : ChannelOverride
{
    override fun getId() = "parties"
    override fun isGlobal() = true

    override fun shouldCheckForPrefix() = true
    override fun shouldOverride(player: Player) = player.hasMetadata("spectator")

    override fun getPrefix() = ">"
    override fun getWeight() = 10

    override fun getFormatted(
        message: String, sender: String,
        rank: Rank, receiver: Player
    ): String
    {
        return "${CC.GRAY}[Spectator] $sender $message"
    }

    @Deprecated(
        message = "Please use CgsSpectatorChannel#hasPermission",
        replaceWith = ReplaceWith(
            "hasPermission(player)"
        )
    )
    override fun getPermission() =
        throw RuntimeException("PlayerPartyChannel#getPermission is not used.")

}
