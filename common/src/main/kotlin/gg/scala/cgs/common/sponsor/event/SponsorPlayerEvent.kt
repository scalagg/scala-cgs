package gg.scala.cgs.common.sponsor.event

import gg.scala.cgs.common.sponsor.SponsorPrize
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
class SponsorPlayerEvent(val sponsor: Player, val sponsoring: Player, val prize: SponsorPrize) : Event()
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }
    override fun getHandlers() = handlerList
}