package gg.scala.cgs.game.listener

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.util.CC
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

            engine.sendMessage("${coloredName(event.participant)}${CC.SEC} joined! ${CC.GREEN}(${
                "${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}"
            })")

            event.participant.teleport(
                engine.gameArena.getSpawnCoordinates()
            )
        }
    }
}
