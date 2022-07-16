package gg.scala.cgs.common.states.machine

import gg.scala.cgs.common.runnable.state.StartedStateRunnable
import gg.scala.cgs.common.states.CgsGameStateService
import me.lucko.helper.Events
import me.lucko.helper.event.SingleSubscription
import me.lucko.helper.event.functional.single.SingleSubscriptionBuilder
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.event.Event

/**
 * @author GrowlyX
 * @since 12/19/2021
 */
abstract class CgsGameStateMachine : AutoCloseable
{
    var started = false

    var terminable = CompositeTerminable.create()
    var startTimestamp = -1L

    /**
     * State machines are sorted ascending.
     */
    abstract fun order(): Int
    abstract fun id(): String

    abstract fun onEnd()
    abstract fun onStart()

    abstract fun onUpdate()
    abstract fun getTimeout(): Long

    fun start()
    {
        started = true
        startTimestamp = System.currentTimeMillis()

        terminable.bind(this)

        kotlin
            .runCatching {
                onStart()
            }
            .onFailure {
                it.printStackTrace()
            }
    }

    /**
     * Ensure this method is used when forcefully
     * moving on to the next game state.
     */
    fun moveOn()
    {
        CgsGameStateService.stateMachines.poll()
            ?.terminable
            ?.closeAndReportException()
    }

    inline fun <reified T : Event> subscribe(
        crossinline lambda: (T) -> Unit
    ): SingleSubscription<T>
    {
        return Events.subscribe(T::class.java)
            .filter {
                CgsGameStateService.current().id() == id()
            }
            .handler {
                lambda(it)
            }
            .apply {
                bindWith(terminable)
            }
    }

    inline fun <reified T : Event> subscribe(): SingleSubscriptionBuilder<T>
    {
        return Events.subscribe(T::class.java)
            .filter {
                CgsGameStateService.current().id() == id()
            }
    }

    override fun close()
    {
        onEnd()
    }
}
