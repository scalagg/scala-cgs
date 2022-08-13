package gg.scala.cgs.common.sponsor.event

import gg.scala.commons.event.StatefulEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
class PreSponsorPlayerEvent(val sponsor: Player, val sponsoring: Player) : StatefulEvent()
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }
    override fun getHandlers() = handlerList
}