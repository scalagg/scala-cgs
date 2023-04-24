package gg.scala.cgs.game.listener

import gg.scala.aware.message.AwareMessage
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsDeathHandler
import gg.scala.cgs.common.player.handler.CgsGameDisqualificationHandler
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.handler.CgsSpectatorHandler
import gg.scala.cgs.common.refresh
import gg.scala.cgs.common.runnable.StateRunnableService
import gg.scala.cgs.common.snapshot.inventory.CgsInventorySnapshotEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.commons.annotations.Listeners
import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.parties.receiver.PartyReceiverHandler
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants.HEART_SYMBOL
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.scoreboard.DisplaySlot
import kotlin.math.ceil

/**
 * @author GrowlyX, puugz
 * @since 12/1/2021
 */
@Listeners
object CgsGameEventListener : Listener
{
    private val engine by lazy {
        CgsGameEngine.INSTANCE
    }

    @EventHandler(
        priority = EventPriority.HIGH
    )
    fun onCgsParticipantConnect(
        event: CgsGameEngine.CgsGameParticipantConnectEvent
    )
    {
        val cgsGamePlayer = event.participantPlayer

        if (engine.gameState == CgsGameState.WAITING || engine.gameState == CgsGameState.STARTING)
        {
            val participantSize = Bukkit.getOnlinePlayers().size

            val party = PartyService
                .findPartyByUniqueId(event.participant.uniqueId)

            if (party != null)
            {
                AwareMessage.of(
                    "party-warp", PartyReceiverHandler.aware,
                    "uniqueId" to party.uniqueId.toString(),
                    "server" to Lemon.instance.settings.id
                ).publish(
                    channel = "party:backbone"
                )
            }

            if (!CgsGameTeamService.allocatePlayersToAvailableTeam(cgsGamePlayer))
            {
                if (
                    !CgsGameTeamService.allocatePlayersToAvailableTeam(
                        cgsGamePlayer, forceRandom = true
                    )
                )
                {
                    event.participant.kickPlayer("${CC.RED}Sorry, we were unable to allocate you to a team.")
                }
            }

            engine.sendMessage(
                "${coloredName(event.participant)}${CC.SEC} has joined ${CC.AQUA}(${
                    "${participantSize}/${Bukkit.getMaxPlayers()}"
                })${CC.YELLOW}!"
            )

            event.participant.removeMetadata("spectator", engine.plugin)

            VisibilityHandler.update(event.participant)

            event.participant refresh (false to GameMode.SURVIVAL)

            if (engine.gameState == CgsGameState.WAITING)
            {
                if (engine.getVotingConfig() != null)
                {
                    event.participant.teleport(
                        engine.getVotingConfig()!!.preStartLobby()
                    )
                } else
                {
                    // We're going to assume the pre-lobby
                    // location already exists now
                    event.participant.teleport(
                        engine.gameArena!!.getPreLobbyLocation()
                    )
                }
            }

            if (engine.getVotingConfig() == null && engine.gameInfo.requiresNoManualConfiguration)
            {
                if (participantSize >= engine.gameInfo.minimumPlayers)
                {
                    engine.onAsyncPreStartResourceInitialization()
                        .thenAccept {
                            engine.gameState = CgsGameState.STARTING
                        }
                } else
                {
                    engine.sendMessage("${CC.SEC}The game requires ${CC.PRI + (engine.gameInfo.minimumPlayers - participantSize) + CC.SEC} more players to start.")
                }
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

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onCgsParticipantReconnect(
        event: CgsGameEngine.CgsGameParticipantReconnectEvent
    )
    {
        if (event.connectedWithinTimeframe)
        {
            val snapshot = CgsInventorySnapshotEngine
                .snapshots[event.participant.uniqueId]

            // The inventory snapshot of their previous connection should never be null.
            if (snapshot == null)
            {
                event.participant.kickPlayer("${CC.RED}[CGS] Something went terribly wrong while trying to reinstate you into the game.")
                return
            }

            val cgsParticipantReinstate = CgsGameEngine
                .CgsGameParticipantReinstateEvent(event.participant, snapshot, true)

            cgsParticipantReinstate.callNow()
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

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onCgsParticipantDisconnect(
        event: CgsGameEngine.CgsGameParticipantDisconnectEvent
    )
    {
        if (engine.gameState == CgsGameState.WAITING || engine.gameState == CgsGameState.STARTING)
        {
            CgsGameTeamService.removePlayerFromTeam(event.participant)

            engine.sendMessage(
                "${event.participant.name}${CC.SEC} has left ${CC.AQUA}(${
                    "${Bukkit.getOnlinePlayers().size - 1}/${Bukkit.getMaxPlayers()}"
                })${CC.YELLOW}!"
            )
        } else if (engine.gameState.isAfter(CgsGameState.STARTED) && !engine.gameState.equals(CgsGameState.ENDED))
        {
            val cgsGamePlayer = CgsPlayerHandler
                .find(event.participant) ?: return

            // disqualification on death would also
            // mean disqualification on log-out.
            if (engine.gameInfo.disqualifyOnLogout)
            {
                if (!event.participant.hasMetadata("spectator"))
                {
                    CgsGameDisqualificationHandler.disqualifyPlayer(
                        player = event.participant,
                        broadcastNotification = true,
                        setSpectator = false
                    )
                }
            } else
            {
                // We are not considering spectators as
                // active players in the game.
                if (event.participant.hasMetadata("spectator"))
                {
                    cgsGamePlayer.lastPlayedGameId = null
                    return
                }

                CgsInventorySnapshotEngine
                    .takeSnapshot(
                        event.participant, true
                    )

                // We're only adding reconnection data if the
                // player will not be disqualified on logout
                cgsGamePlayer.lastPlayedGameId = engine.uniqueId
                cgsGamePlayer.lastPlayedGameDisconnectionTimestamp = System.currentTimeMillis()
            }
        }
    }

    // TODO: re-implement
    /*@EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onPreDisguise(event: PreDisguiseEvent)
    {
        if (engine.gameState.isAfter(CgsGameState.STARTED))
        {
            event.isCancelled = true
            event.player.sendMessage("${CC.RED}You are not allowed to disguise at this time.")
        }
    }*/

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onPreDisguise(event: InventoryCreativeEvent)
    {
        if (event.whoClicked.hasMetadata("spectator"))
        {
            event.isCancelled = true
        }
    }

    @EventHandler(
        priority = EventPriority.HIGH
    )
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        val player = event.entity
        val location = event.entity.location
        val killer = event.entity.killer

        val cgsGamePlayer = CgsPlayerHandler.find(player)!!

        val statistics = engine.getStatistics(cgsGamePlayer)
        statistics.deaths++

        player.health = player.maxHealth
        player.foodLevel = 20
        player.saturation = 20.0f

        if (killer != null)
        {
            val cgsGameKiller = CgsPlayerHandler.find(killer)!!
            val killerStatistics = engine.getStatistics(cgsGameKiller)

            killerStatistics.kills++
            killerStatistics.gameKills++
        }

        event.deathMessage = if (engine.gameInfo.customDeathMessage)
        {
            engine.gameInfo.customDeathMessageService(player)
        } else
        {
            CgsDeathHandler.formDeathMessage(player, killer)
        }

        val cgsDeathEvent = CgsGameEngine
            .CgsGameParticipantDeathEvent(player, killer, location)

        if (engine.gameInfo.spectateOnDeath)
        {
            CgsGameDisqualificationHandler.disqualifyPlayer(
                player = player, broadcastNotification = false, setSpectator = true
            )
        }

        CgsInventorySnapshotEngine
            .takeSnapshot(player)

        cgsDeathEvent.callNow()
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onCgsGameStart(
        event: CgsGameEngine.CgsGameStartEvent
    )
    {
        val participants = Bukkit.getOnlinePlayers()
            .filter { !it.hasMetadata("spectator") }

        engine.gameStart = System.currentTimeMillis()
        engine.originalRemaining = participants
            .map { it.uniqueId }
            .toMutableList()

        participants.forEach {
            NametagHandler.reloadPlayer(it)

            engine
                .getStatistics(
                    CgsPlayerHandler.find(it)!!
                )
                .gameKills.reset()
        }

        if (this.engine.gameInfo.showNameHearts)
        {
            for (participant in participants)
            {
                val scoreboard = participant
                    .scoreboard
                    ?: return

                scoreboard
                    .registerNewObjective(
                        "showhealth", "health"
                    )
                    .also {
                        it.displaySlot = DisplaySlot.BELOW_NAME
                        it.displayName = "${CC.D_RED}\u2764"
                    }
            }
        }

        StateRunnableService
            .startRunningAsync(CgsGameState.STARTED)
    }

    @EventHandler
    fun onCgsGameParticipantReinstate(
        event: CgsGameEngine.CgsGameParticipantReinstateEvent
    )
    {
        if (!event.connected)
        {
            CgsSpectatorHandler.removeSpectator(event.participant)
        }

        val cgsGameTeam = CgsGameTeamService.getTeamOf(event.participant)!!
        cgsGameTeam.eliminated.remove(event.participant.uniqueId)

        event.snapshot.restore(event.participant)

        event.participant.sendMessage(
            "${CC.D_GREEN}✓ ${CC.GREEN}You've been added back into the game."
        )
    }

    @EventHandler
    fun onCgsGamePreStart(
        event: CgsGameEngine.CgsGamePreStartEvent
    )
    {
        StateRunnableService
            .startRunningAsync(CgsGameState.STARTING)
    }

    @EventHandler
    fun onCgsGameEnd(
        event: CgsGameEngine.CgsGameEndEvent
    )
    {
        engine.winningTeam.apply {
            this.participants.forEach {
                val bukkit = Bukkit.getPlayer(it)
                    ?: return@forEach

                CgsInventorySnapshotEngine
                    .takeSnapshot(bukkit, false)
            }
        }

        StateRunnableService
            .startRunningAsync(CgsGameState.ENDED)
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onEntityDamage(event: EntityDamageByEntityEvent)
    {
        val entity = event.entity
        val damagedBy = event.damager

        if (entity is Player && damagedBy is Player)
        {
            val cgsGameTeam = CgsGameTeamService
                .getTeamOf(damagedBy)
                ?: return

            if (cgsGameTeam.participants.contains(entity.uniqueId))
            {
                event.isCancelled = true
                event.damager.sendMessage(
                    "${CC.RED}You cannot hurt your teammate ${CC.B_RED}${entity.name}${CC.RED}!"
                )
            }
        }
    }

    @EventHandler
    fun onInventoryMove(event: InventoryMoveItemEvent)
    {
        if (
            engine.gameState != CgsGameState.STARTED ||
            event.initiator.viewers.firstOrNull()
                ?.hasMetadata("spectator") == true
        )
        {
            event.isCancelled = true
        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
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
                        shooter.sendMessage("${coloredName(player)}${CC.SEC} is now at ${CC.PRI}$health${HEART_SYMBOL}${CC.SEC}.")
                    }
                }
            }
        }
    }
}
