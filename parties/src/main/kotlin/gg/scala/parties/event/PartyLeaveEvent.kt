package gg.scala.parties.event

import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyMember
import net.evilblock.cubed.event.PluginEvent
import org.bukkit.event.HandlerList

/**
 * @author GrowlyX
 * @since 5/6/2022
 */
class PartyLeaveEvent(
    val party: Party,
    val member: PartyMember,
    val kicked: Boolean = false
) : PluginEvent()
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList
}
