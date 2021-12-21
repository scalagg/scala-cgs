package gg.scala.cgs.common.runnable

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
abstract class StateRunnable(
    private val gameState: CgsGameState
) : BukkitRunnable()
{
    var currentTick = -1

    override fun run()
    {
        if (CgsGameEngine.INSTANCE.gameState != gameState)
        {
            cancel()
            return
        }

        try
        {
            onTick()

            CgsGameEngine.INSTANCE.onTick(
                gameState, currentTick
            )
        } catch (exception: Exception)
        {
            CgsGameEngine.INSTANCE.plugin.logger.info(
                "An exception was thrown! Here's the exception message: ${exception.message}"
            )
        }

        currentTick++
    }

    abstract fun onTick()
}
