package gg.scala.parties.event

import gg.scala.parties.PartySpigotPlugin
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyMember
import org.bukkit.event.HandlerList
import org.bukkit.event.server.PluginEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author GrowlyX
 * @since 5/6/2022
 */
class PartyLeaveEvent(
    val party: Party,
    val member: PartyMember,
    val kicked: Boolean = false
) : PluginEvent(
    JavaPlugin.getPlugin(
        PartySpigotPlugin::class.java
    )
)
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList
}
