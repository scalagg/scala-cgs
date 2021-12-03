package gg.scala.cgs.common

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
enum class CgsGameState
{
    WAITING,
    STARTING,
    STARTED,
    ENDED;

    fun isPast(state: CgsGameState): Boolean
    {
        return ordinal >= state.ordinal
    }
}
