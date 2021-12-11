package gg.scala.potato

import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.common.player.statistic.value.CgsGameStatistic

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class HotPotatoCgsStatistics
@JvmOverloads
constructor(
    override var gameKills: CgsGameStatistic = CgsGameStatistic(),
    override var kills: CgsGameStatistic = CgsGameStatistic(),
    override var deaths: CgsGameStatistic = CgsGameStatistic(),
    override var played: CgsGameStatistic = CgsGameStatistic(),
    override var wins: CgsGameStatistic = CgsGameStatistic(),
    override var losses: CgsGameStatistic = CgsGameStatistic()
) : GameSpecificStatistics()
