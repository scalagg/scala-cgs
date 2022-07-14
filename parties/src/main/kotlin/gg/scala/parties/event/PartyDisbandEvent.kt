package gg.scala.parties.event

import gg.scala.parties.PartySpigotPlugin
import gg.scala.parties.model.Party
import org.bukkit.event.HandlerList
import org.bukkit.event.server.PluginEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author GrowlyX
 * @since 5/6/2022
 */
class PartyDisbandEvent(
    val party: Party
) : net.evilblock.cubed.event.PluginEvent()
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList
}
