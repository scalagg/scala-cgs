package gg.scala.cgs.game.listener

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.player.handler.CgsGameDisqualificationHandler
import gg.scala.cgs.common.player.handler.CgsDeathHandler
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.refresh
import gg.scala.cgs.common.respawnPlayer
import gg.scala.cgs.common.runnable.state.EndedStateRunnable
import gg.scala.cgs.common.runnable.state.StartedStateRunnable
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.player.handler.CgsSpectatorHandler
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.lemon.disguise.update.event.PreDisguiseEvent
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants.HEART_SYMBOL
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.ceil

/**
 * @author GrowlyX, puugz
 * @since 12/1/2021
 */
object CgsGameEventListener : Listener
{
    private val engine = CgsGameEngine.INSTANCE

    @EventHandler
    fun onCgsParticipantConnect(
        event: CgsGameEngine.CgsGameParticipantConnectEvent
    )
    {
        val cgsGamePlayer = CgsPlayerHandler
            .find(event.participant)!!

        if (engine.gameState == CgsGameState.WAITING || engine.gameState == CgsGameState.STARTING)
        {
            // TODO: 12/1/2021 check for party leader
            //  connection & all online player's connections
            CgsGameTeamEngine.allocatePlayersToAvailableTeam(
                listOf(cgsGamePlayer)
            )

            // Bukkit.getOnlinePlayers().size
            // should be replaced with a thing that gets
            // all the players that will be playing the game
            // (excluding the spectators) since we will add
            // a spectator command (able to become a spectator before the game starts)

            val participantSize = Bukkit.getOnlinePlayers().size

            engine.broadcast(
                "${coloredName(event.participant)}${CC.SEC} has joined. ${CC.GREEN}(${
                    "${participantSize}/${Bukkit.getMaxPlayers()}"
                })"
            )

            event.participant refresh (false to GameMode.ADVENTURE)
            event.participant.teleport(
                engine.gameArena.getPreLobbyLocation()
            )

            if (participantSize >= engine.gameInfo.minimumPlayers)
            {
                engine.gameState = CgsGameState.STARTING
            } else {
                engine.broadcast("${CC.SEC}The game requires ${CC.PRI + (engine.gameInfo.minimumPlayers - participantSize) + CC.SEC} more players to start.")
            }
        } else if (!event.reconnectCalled)
        {
            CgsSpectatorHandler.setSpectator(event.participant)

            // An extension to the spectator message which is
            // sent within the setSpectator method being called above this.
            Tasks.delayed(1L)
            {
                event.participant.sendMessage("${CC.D_RED}✘ ${CC.RED}This is due to your late entrance into the game.")
            }
        }
    }

    @EventHandler
    fun onCgsParticipantReconnect(
        event: CgsGameEngine.CgsGameParticipantReconnectEvent
    )
    {
        if (event.connectedWithinTimeframe)
        {
            // The CGS game team should never be null.
            val cgsGameTeam = CgsGameTeamEngine.getTeamOf(event.participant)!!
            cgsGameTeam.eliminated.remove(event.participant.uniqueId)

            val cgsParticipantReinstate = CgsGameEngine
                .CgsGameParticipantReinstateEvent(event.participant, cgsGameTeam)

            cgsParticipantReinstate.callNow()

            event.participant.sendMessage("${CC.D_GREEN}✓ ${CC.GREEN}You've been added back into the game.")
        } else
        {
            CgsSpectatorHandler.setSpectator(
                event.participant, false
            )

            // We are delaying this message by one tick to send
            // the player the correct order of messages.

            // The original spectator notification is sent
            // during the delayed task.
            Tasks.delayed(1L)
            {
                event.participant.sendMessage("${CC.D_RED}✘ ${CC.RED}This is due to your late reconnection to the server.")
            }
        }
    }

    @EventHandler
    fun onCgsParticipantDisconnect(
        event: CgsGameEngine.CgsGameParticipantDisconnectEvent
    )
    {
        if (engine.gameState == CgsGameState.WAITING || engine.gameState == CgsGameState.STARTING)
        {
            CgsGameTeamEngine.removePlayerFromTeam(event.participant)

            engine.broadcast(
                "${coloredName(event.participant)}${CC.SEC} has left. ${CC.GREEN}(${
                    "${Bukkit.getOnlinePlayers().size - 1}/${Bukkit.getMaxPlayers()}"
                })"
            )
        } else if (engine.gameState.isAfter(CgsGameState.STARTED))
        {
            val cgsGamePlayer = CgsPlayerHandler
                .find(event.participant) ?: return

            // disqualification on death would also
            // mean disqualification on log-out.
            if (engine.gameInfo.disqualifyOnLogout)
            {
                CgsGameDisqualificationHandler.disqualifyPlayer(
                    player = event.participant,
                    broadcastNotification = true,
                    setSpectator = false
                )
            } else
            {
                // We are not considering spectators as
                // active players in the game.
                if (event.participant.hasMetadata("spectator"))
                {
                    cgsGamePlayer.lastPlayedGameId = null
                    return
                }

                // We're only adding reconnection data if the
                // player will not be disqualified on logout
                cgsGamePlayer.lastPlayedGameId = engine.uniqueId
                cgsGamePlayer.lastPlayedGameDisconnectionTimestamp = System.currentTimeMillis()
            }
        }
    }

    @EventHandler
    fun onPreDisguise(event: PreDisguiseEvent)
    {
        if (engine.gameState.isAfter(CgsGameState.STARTED))
        {
            event.isCancelled = true
            event.player.sendMessage("${CC.RED}You are not allowed to disguise at this time.")
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        val player = event.entity
        val killer = event.entity.killer

        val cgsGamePlayer = CgsPlayerHandler.find(player)!!

        val statistics = engine.getStatistics(cgsGamePlayer)
        statistics.deaths.increment()

        respawnPlayer(event)

        if (killer != null)
        {
            val cgsGameKiller = CgsPlayerHandler.find(killer)!!

            val killerStatistics = engine.getStatistics(cgsGameKiller)
            killerStatistics.kills.increment()
            killerStatistics.gameKills.increment()
        }

        event.deathMessage = CgsDeathHandler
            .formDeathMessage(player, killer)

        // TODO: 04/12/2021 make a thing for player respawning
        // like for example they get 5 respawns and after they use them all
        // they disqualified

        if (engine.gameInfo.spectateOnDeath)
        {
            CgsGameDisqualificationHandler.disqualifyPlayer(
                player = player, broadcastNotification = false, setSpectator = true
            )
        }
    }

    @EventHandler
    fun onCgsGameStart(
        event: CgsGameEngine.CgsGameStartEvent
    )
    {
        val participants = Bukkit.getOnlinePlayers()
            .filter { !it.hasMetadata("spectator") }

        engine.gameStart = System.currentTimeMillis()
        engine.originalRemaining = participants.size

        participants.forEach {
            NametagHandler.reloadPlayer(it)
        }

        Tasks.asyncTimer(
            StartedStateRunnable,
            0L, 20L
        )
    }

    @EventHandler
    fun onCgsGamePreStart(
        event: CgsGameEngine.CgsGamePreStartEvent
    )
    {
        Tasks.timer(
            0L, 20L,
            StartingStateRunnable
        )
    }

    @EventHandler
    fun onCgsGameEnd(
        event: CgsGameEngine.CgsGameEndEvent
    )
    {
        Tasks.timer(
            0L, 20L,
            EndedStateRunnable
        )
    }

    @EventHandler
    fun onCgsForceStart(
        event: CgsGameEngine.CgsGameForceStartEvent
    )
    {
        engine.broadcast("${CC.GREEN}The game has been force-started. ${CC.GRAY}(by ${
            if (event.starter is Player) event.starter.name else "Console"
        })")
    }

    @EventHandler(
        priority = EventPriority.LOW
    )
    fun onEntityDamage(event: EntityDamageByEntityEvent)
    {
        val entity = event.entity
        val damagedBy = event.damager

        if (entity is Player && damagedBy is Player)
        {
            val cgsGameTeam = CgsGameTeamEngine
                .getTeamOf(damagedBy)!!

            if (cgsGameTeam.participants.contains(entity.uniqueId))
            {
                event.isCancelled = true
                event.damager.sendMessage(
                    "${CC.RED}You're unable to hurt ${CC.ITALIC}${entity.name}${CC.RED}."
                )
            }
        }
    }

    @EventHandler
    fun onArrowShoot(event: EntityDamageByEntityEvent)
    {
        if (engine.gameState != CgsGameState.STARTED)
            return

        val player = event.entity
        val arrow = event.damager

        if (arrow is Arrow && player is Player)
        {
            val shooter = arrow.shooter

            if (shooter is Player)
            {
                if (arrow.getName() != shooter.name)
                {
                    val health = ceil(player.health - event.finalDamage) / 2.0

                    if (health > 0.0)
                    {
                        shooter.sendMessage("${coloredName(player)}${CC.SEC} is now at ${CC.RED}$health${HEART_SYMBOL}${CC.SEC}.")
                    }
                }
            }
        }
    }
}
