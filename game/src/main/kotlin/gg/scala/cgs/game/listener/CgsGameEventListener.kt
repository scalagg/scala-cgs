package gg.scala.cgs.game.listener

import gg.scala.aware.message.AwareMessage
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.alive
import gg.scala.cgs.common.combat.CombatLogService
import gg.scala.cgs.common.giveCoins
import gg.scala.cgs.common.player.GameSave
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
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers
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
            val participantSize = alive.size

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

            if (!event.participant.hasMetadata("vanished"))
            {
                engine.sendMessage(
                    "${CC.GREEN}${event.participant.name}${CC.SEC} joined. ${CC.GRAY}(${
                        "${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}"
                    })"
                )
            }

            event.participant.removeMetadata("spectator", engine.plugin)

            VisibilityHandler.update(event.participant)

            event.participant refresh (false to GameMode.SURVIVAL)

            if (engine.gameState == CgsGameState.WAITING)
            {
                event.participant.teleport(
                    engine.getVotingConfig()?.preStartLobby() ?:
                        engine.gameArena!!.getPreLobbyLocation()
                )
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
            println("STOP IT")

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

            if (!event.participantInVanish)
            {
                engine.sendMessage(
                    "${CC.GREEN}${event.participant.name}${CC.SEC} left. ${CC.GRAY}(${
                        "${Bukkit.getOnlinePlayers().size - 1}/${Bukkit.getMaxPlayers()}"
                    })"
                )
            }
        } else if (engine.gameState.isAfter(CgsGameState.STARTED) && engine.gameState != CgsGameState.ENDED)
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
                        broadcastNotification = !event.participantInVanish,
                        setSpectator = false
                    )
                }
            } else
            {
                // We are not considering spectators as
                // active players in the game.
                if (event.participant.hasMetadata("spectator"))
                {
                    ScalaCommonsSpigot.instance.kvConnection
                        .sync().del(
                            "game-saves:${event.participant.uniqueId}"
                        )
                    return
                }

                CgsInventorySnapshotEngine
                    .takeSnapshot(
                        event.participant, true
                    )

                // temp, we'll get rid of them once they rejoin
                CgsGameTeamService.getTeamOf(event.participant)
                    ?.apply {
                        this.eliminated.add(event.participant.uniqueId)
                    }

                val relogTime = CgsPlayerHandler.dynamicRelogTime.invoke(event.participant)

                if (relogTime > 0L)
                {
                    if (engine.gameInfo.configureCombatLog)
                    {
                        CombatLogService.create(event.participant, relogTime / 1000)
                        Bukkit.broadcastMessage(
                            "${CC.GRAY}(Combat Log) ${CC.GREEN}${event.participant.name}${CC.SEC} disconnected."
                        )
                    }

                    ScalaCommonsSpigot.instance.kvConnection
                        .sync()
                        .apply {
                            psetex(
                                "game-saves:${event.participant.uniqueId}", relogTime,
                                Serializers.gson.toJson(GameSave(
                                    expirationTimestamp = System.currentTimeMillis() + relogTime,
                                    serverId = Lemon.instance.settings.id
                                ))
                            )
                        }
                } else
                {
                    CgsGameDisqualificationHandler.disqualifyPlayer(
                        player = event.participant,
                        broadcastNotification = !event.participantInVanish,
                        setSpectator = false
                    )
                }
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
            killerStatistics.save()

            killerStatistics.gameKills++

            CgsGameTeamService.getTeamOf(killer)
                ?.apply {
                    this.totalKills += 1
                }

            killer.giveCoins(100 to "Killing a player")
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
        val participants = alive

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
        } else
        {
            CgsGameTeamService.getTeamOf(event.participant)
                ?.apply {
                    this.eliminated.remove(event.participant.uniqueId)
                }
        }

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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onCgsGameForceStart(
        event: CgsGameEngine.CgsGameForceStartEvent
    )
    {
        val teamsWithAlivePlayers = CgsGameTeamService.teams
            .values.filter {
                it.alive.isNotEmpty()
            }

        if (teamsWithAlivePlayers.size <= 1)
        {
            event.isCancelled = true
            event.starter.sendMessage(
                "${CC.RED}You must have at least two teams with participants to start the game!"
            )
        }
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
