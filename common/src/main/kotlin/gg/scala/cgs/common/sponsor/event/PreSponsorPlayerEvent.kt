package gg.scala.cgs.common.sponsor.event

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
class PreSponsorPlayerEvent(val sponsor: Player, val sponsoring: Player) : Event(), Cancellable
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    private var internalCancelled = false

    override fun getHandlers() = handlerList
    override fun isCancelled(): Boolean = internalCancelled

    override fun setCancelled(cancel: Boolean)
    {
        internalCancelled = cancel
    }

    fun call(): Boolean
    {
        Bukkit.getPluginManager().callEvent(this)
        return !internalCancelled
    }

}