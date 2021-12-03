package gg.scala.cgs.game.listener

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.disqualification.CgsGameDisqualificationHandler
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

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

        if (engine.gameState == CgsGameState.WAITING)
        {
            // TODO: 12/1/2021 check for party
            CgsGameTeamEngine.allocatePlayersToAvailableTeam(
                listOf(cgsGamePlayer)
            )

            engine.sendMessage("${coloredName(event.participant)}${CC.SEC} has joined. ${CC.GREEN}(${
                "${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}"
            })")

            event.participant.teleport(
                engine.gameArena.getSpawnCoordinates()
            )
        }
    }

    @EventHandler
    fun onCgsParticipantDisconnect(
        event: CgsGameEngine.CgsGameParticipantConnectEvent
    )
    {
        if (engine.gameState == CgsGameState.WAITING)
        {
            Tasks.delayed(1L) {
                engine.sendMessage("${coloredName(event.participant)}${CC.SEC} has left. ${CC.GREEN}(${
                    "${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}"
                })")
            }
        } else if (engine.gameState == CgsGameState.STARTED)
        {
            val cgsGamePlayer = CgsPlayerHandler
                .find(event.participant) ?: return

            // disqualification on death would also
            // mean disqualification on log-out.
            if (engine.gameInfo.disqualifyOnDeath)
            {
                CgsGameDisqualificationHandler.disqualifyPlayer(
                    player = event.participant,
                    broadcastNotification = true,
                    setSpectator = false
                )
            } else
            {
                // We're only adding reconnection data if the
                // player will not be disqualified on logout
                cgsGamePlayer.lastPlayedGameId = engine.uniqueId
                cgsGamePlayer.lastPlayedGameDisconnectionTimestamp = System.currentTimeMillis()
            }
        }
    }
}
