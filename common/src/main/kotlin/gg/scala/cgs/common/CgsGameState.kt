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

    fun isPast(one: CgsGameState, two: CgsGameState): Boolean
    {
        return one.ordinal > two.ordinal
    }
}
