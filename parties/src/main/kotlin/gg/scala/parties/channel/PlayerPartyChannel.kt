package gg.scala.parties.channel

import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object PlayerPartyChannel : Channel
{
    override fun getId() = "parties"
    override fun isGlobal() = true

    override fun shouldCheckForPrefix() = true
    override fun getPrefix() = ">"

    override fun getFormatted(
        message: String, sender: String,
        rank: Rank, receiver: Player
    ): String
    {
        return "${CC.GREEN}[P] ${CC.GRAY}[$sender]: ${CC.WHITE}$message"
    }

    @Deprecated(
        message = "Please use PlayerPartyChannel#hasPermission",
        replaceWith = ReplaceWith(
            "hasPermission()"
        )
    )
    override fun getPermission() =
        throw RuntimeException("PlayerPartyChannel#getPermission is not used.")

}
