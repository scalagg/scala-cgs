package gg.scala.cgs.common.disqualification

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.spectator.CgsSpectatorHandler
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsGameDisqualificationHandler
{
    fun disqualifyPlayer(
        player: Player,
        broadcastNotification: Boolean = false,
        setSpectator: Boolean = true,
        reason: String = ""
    )
    {
        val cgsGameTeam = CgsGameTeamEngine.getTeamOf(player) ?: return
        cgsGameTeam.eliminated.add(player.uniqueId)

        if (CgsGameEngine.INSTANCE.gameInfo.spectatable && setSpectator)
        {
            CgsSpectatorHandler
        }
    }
}
