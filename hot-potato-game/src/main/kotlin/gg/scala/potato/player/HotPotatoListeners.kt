package gg.scala.potato.player

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
object HotPotatoListeners : Listener
{
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent)
    {
        if (event.cause == Cause.ENTITY_ATTACK)
        {
            event.damage = 0.0
        } else
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent)
    {
        event.isCancelled = true
    }
}

typealias Cause = EntityDamageEvent.DamageCause
