package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.runnable.StateRunnable
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object StartedStateRunnable : StateRunnable(
    CgsGameState.STARTED
)
{
    private val engine = CgsGameEngine.INSTANCE

    override fun onTick()
    {
        val teamsWithAlivePlayers = CgsGameTeamEngine.teams
            .values.filter { it.alive.isNotEmpty() }

        if (teamsWithAlivePlayers.size == 1)
        {
            // This runnable is run asynchronously
            Tasks.sync {
                engine.winningTeam = teamsWithAlivePlayers[0]
                engine.gameState = CgsGameState.ENDED
            }
        } else if (Bukkit.getOnlinePlayers().isEmpty() || teamsWithAlivePlayers.isEmpty())
        {
            Bukkit.shutdown()
        }
    }
}
