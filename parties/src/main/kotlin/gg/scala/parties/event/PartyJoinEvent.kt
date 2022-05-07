package gg.scala.parties.event

import gg.scala.parties.PartySpigotPlugin
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyMember
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.server.PluginEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author GrowlyX
 * @since 5/6/2022
 */
class PartyJoinEvent(
    val party: Party,
    val member: PartyMember
) : PluginEvent(
    JavaPlugin.getPlugin(
        PartySpigotPlugin::class.java
    )
), Cancellable
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList

    private var cancelledLol = false

    override fun isCancelled() = cancelledLol
    override fun setCancelled(cancel: Boolean)
    {
        cancelledLol = cancel
    }
}
