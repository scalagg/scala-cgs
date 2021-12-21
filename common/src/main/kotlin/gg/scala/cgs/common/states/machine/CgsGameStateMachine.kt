package gg.scala.cgs.common.states.machine

import kotlin.time.Duration

/**
 * @author GrowlyX
 * @since 12/19/2021
 */
interface CgsGameStateMachine
{
    fun isAvailable()

    fun onEnd()
    fun onStart()

    fun onUpdate()
    fun getDuration(): Duration
}
