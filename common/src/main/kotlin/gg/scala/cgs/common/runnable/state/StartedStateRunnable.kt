package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.runnable.StateRunnable
import gg.scala.cgs.common.states.CgsGameStateService
import gg.scala.cgs.common.teams.CgsGameTeamService
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
    private val machine = CgsGameStateService

    override fun onTick()
    {
        val teamsWithAlivePlayers = CgsGameTeamService.teams
            .values.filter { it.alive.isNotEmpty() }

        if (machine.stateMachines.isNotEmpty())
        {
            val current = machine.current()

            if (!current.started)
            {
                current.start()
            } else
            {
                if (System.currentTimeMillis() >= current.startTimestamp + current.getTimeout())
                {
                    machine.stateMachines.poll()
                        ?.terminable
                        ?.closeAndReportException()

                    CgsGameEngine.INSTANCE.plugin.logger.info(
                        "[CGS] Moving onto ${current.id()} due to timeout."
                    )
                } else
                {
                    current.onUpdate()
                }
            }
        }

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
