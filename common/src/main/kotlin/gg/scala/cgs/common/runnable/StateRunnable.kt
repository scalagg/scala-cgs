package gg.scala.cgs.common.runnable

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.printStackTraceV2
import gg.scala.cgs.common.states.CgsGameState
import me.lucko.helper.scheduler.Task
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
abstract class StateRunnable(
    private val gameState: CgsGameState
) : Runnable
{
    var currentTick = -1
    var task: Task? = null

    override fun run()
    {
        if (
            CgsGameEngine.INSTANCE
                .gameState != gameState
        )
        {
            this.task?.closeAndReportException()
            return
        }

        try
        {
            onTick()
        } catch (exception: Exception)
        {
            exception.printStackTraceV2(
                "StateRunnable for ${
                    CgsGameEngine.INSTANCE.gameState.name
                }"
            )
        }

        currentTick++
    }

    abstract fun onTick()
}
