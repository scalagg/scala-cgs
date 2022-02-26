package gg.scala.parties.model

import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
class PartyMember(
    val uniqueId: UUID,
    val role: PartyRole
)
{
    fun sendMessage(message: String)
    {
        QuickAccess
            .sendGlobalPlayerMessage(message, uniqueId)
    }

    fun sendMessage(message: FancyMessage)
    {
        QuickAccess
            .sendGlobalPlayerFancyMessage(message, uniqueId)
    }

    fun isOnline() = Bukkit.getPlayer(uniqueId) != null
}
