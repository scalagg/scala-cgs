package gg.scala.cgs.common.player.channel

import gg.scala.lemon.channel.ChatChannelComposite
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.util.CC
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsSpectatorChannelComposite : ChatChannelComposite
{
    override fun format(
        sender: UUID, receiver: Player?,
        message: String, server: String, rank: Rank
    ): TextComponent
    {
        return LegacyComponentSerializer.legacySection()
            .deserialize("${CC.GRAY}[Spectator] ${
                sender.username()
            }${CC.GRAY}: $message")
    }

    override fun identifier() = "spectator"
}
