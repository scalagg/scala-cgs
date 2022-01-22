package gg.scala.cgs.common.statistics

import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics

/**
 * @author GrowlyX
 * @since 1/22/2022
 */
interface CgsStatisticProvider<S : GameSpecificStatistics>
{
    fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
}
