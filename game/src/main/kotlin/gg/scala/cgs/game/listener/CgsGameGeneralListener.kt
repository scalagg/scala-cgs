package gg.scala.cgs.game.listener

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.weather.WeatherChangeEvent


/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsGameGeneralListener : Listener
{
    private val engine = CgsGameEngine.INSTANCE

    @EventHandler
    fun onPickupItem(event: PlayerPickupItemEvent)
    {
        if (shouldCancel(event.player))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent)
    {
        if (event.target is Player)
        {
            if (shouldCancel(event.target as Player))
            {
                event.target = null
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent)
    {
        if (event.target is Player)
        {
            if (shouldCancel(event.target as Player))
            {
                event.target = null
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onHangingPlace(event: HangingPlaceEvent)
    {
        if (event.entity is ItemFrame)
        {
            if (shouldCancel(event.player))
            {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent)
    {
        if (shouldCancel(event.player))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (shouldCancel(event.player))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockPlaceEvent)
    {
        if (shouldCancel(event.player))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent)
    {
        if (event.entity is Player && shouldCancel(event.entity as Player))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent)
    {
        if (event.damager is Player && shouldCancel(event.damager as Player))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onVehicleEnter(event: VehicleEnterEvent)
    {
        if (event.entered is Player && shouldCancel((event.entered as Player)))
        {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onWeather(event: WeatherChangeEvent)
    {
        event.isCancelled = true
    }

    private fun shouldCancel(player: Player): Boolean
    {
        return player.hasMetadata("spectator") || engine.gameState.isBefore(CgsGameState.STARTING)
    }
}
