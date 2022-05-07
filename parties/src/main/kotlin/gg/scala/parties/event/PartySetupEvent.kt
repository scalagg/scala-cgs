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
class PartySetupEvent(
    val party: Party
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
