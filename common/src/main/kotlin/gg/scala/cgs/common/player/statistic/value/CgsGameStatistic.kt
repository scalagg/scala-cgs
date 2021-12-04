package gg.scala.cgs.common.player.statistic.value

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsGameStatistic
{
    var value = 0

    fun increment()
    {
        value += 1
    }

    fun decrement()
    {
        value += 1
    }

    fun update(new: Int)
    {
        value = new
    }

    fun reset()
    {
        value += 1
    }
}
