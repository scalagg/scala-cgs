package gg.scala.cgs.game.listener

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.disqualify.CgsGameDisqualificationHandler
import gg.scala.cgs.common.handler.CgsDeathHandler
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.runnable.state.EndedStateRunnable
import gg.scala.cgs.common.runnable.state.StartedStateRunnable
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.spectator.CgsSpectatorHandler
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.lemon.disguise.update.event.PreDisguiseEvent
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants.HEART_SYMBOL
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import kotlin.math.ceil

/**
 * @author GrowlyX
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

            engine.sendMessage(
                "${coloredName(event.participant)}${CC.SEC} has joined. ${CC.GREEN}(${
                    "${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}"
                })"
            )

            event.participant.teleport(
                engine.gameArena.getPreLobbyLocation()
            )

            if (Bukkit.getOnlinePlayers().size >= engine.gameInfo.minimumPlayers)
            {
                engine.gameState = CgsGameState.STARTING
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
            Tasks.delayed(1L) {
                engine.sendMessage(
                    "${coloredName(event.participant)}${CC.SEC} has left. ${CC.GREEN}(${
                        "${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}"
                    })"
                )
            }
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

        if (killer != null)
        {
            val cgsGameKiller = CgsPlayerHandler.find(killer)!!

            val killerStatistics = engine.getStatistics(cgsGameKiller)
            killerStatistics.kills.increment()
            killerStatistics.gameKills.increment()
        }

        event.deathMessage = CgsDeathHandler
            .formDeathMessage(player, killer)

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
            val scoreboard = it.scoreboard ?: return@forEach

            val objective = scoreboard.registerNewObjective("tabHealth", "health")
            objective.displaySlot = DisplaySlot.PLAYER_LIST

            val healthObjective = scoreboard.registerNewObjective("nameHealth", "health")
            healthObjective.displaySlot = DisplaySlot.BELOW_NAME
            healthObjective.displayName = "${CC.D_RED}${HEART_SYMBOL}"

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
    fun onEntityDamageNormal(event: EntityDamageEvent)
    {
        if (event.entity is Player)
        {
            updateTabHealth(event.entity as Player)
        }
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

    fun updateTabHealth(player: Player)
    {
        if (!engine.gameInfo.showTabHearts)
            return

        for (onlinePlayer in Bukkit.getOnlinePlayers())
        {
            val board = onlinePlayer.scoreboard
            var tabHealthObjective = board.getObjective("tabHealth")

            if (tabHealthObjective == null)
            {
                tabHealthObjective = board.registerNewObjective("tabHealth", "health")
                tabHealthObjective.displaySlot = DisplaySlot.PLAYER_LIST
            }

            tabHealthObjective.getScore(player.name).score = getHealth(player)

            var nameHealthObjective = board.getObjective("nameHealth")

            if (nameHealthObjective == null)
            {
                nameHealthObjective = board.registerNewObjective("nameHealth", "health")
                nameHealthObjective.displaySlot = DisplaySlot.BELOW_NAME
                nameHealthObjective.displayName = CC.D_RED + HEART_SYMBOL
            }

            nameHealthObjective.getScore(player.name).score = getHealth(player)
        }
    }

    private fun getHealth(player: Player): Int
    {
        var health = player.health.toInt()

        val effect = player.activePotionEffects.firstOrNull {
            it.type == PotionEffectType.ABSORPTION
        }

        if (effect != null)
        {
            health += effect.amplifier * 2 + 2
        }

        return health
    }
}
