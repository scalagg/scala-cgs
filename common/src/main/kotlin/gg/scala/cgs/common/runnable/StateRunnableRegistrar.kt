package gg.scala.cgs.common.runnable

import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import me.lucko.helper.Schedulers

/**
 * @author GrowlyX
 * @since 12/22/2021
 */
object StateRunnableRegistrar
{
    private val registered = mutableMapOf<CgsGameState, StateRunnable>()

    fun registerOrOverride(
        state: CgsGameState, runnable: StateRunnable
    )
    {
        registered[state] = runnable
    }

    fun startRunningAsync(state: CgsGameState)
    {
        registered[state]?.let {
            Schedulers.async().runRepeating(
                it, 0L, 20L
            )
        }
    }
}
