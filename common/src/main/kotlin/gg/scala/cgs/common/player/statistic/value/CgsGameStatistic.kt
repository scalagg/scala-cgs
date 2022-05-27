package gg.scala.cgs.common.player.statistic.value

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsGameStatistic
{
    var value = 0

    operator fun inc() : CgsGameStatistic
    {
        value++
        return this
    }

    operator fun dec() : CgsGameStatistic
    {
        value--
        return this
    }

    fun update(new: Int)
    {
        value = new
    }

    fun reset()
    {
        value = 0
    }
}
