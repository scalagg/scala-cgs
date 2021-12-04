package gg.scala.cgs.common.information

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
open class CgsGameAwardInfo(
    val awardCoins: Boolean,
    // select a random amount
    // within this IntRange
    val winningCoinRange: IntRange,
    val participationCoinRange: IntRange,
)
