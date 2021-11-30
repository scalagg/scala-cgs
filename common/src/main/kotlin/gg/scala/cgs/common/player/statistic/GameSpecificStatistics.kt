package gg.scala.cgs.common.player.statistic

import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
abstract class GameSpecificStatistics : AbstractTypeSerializable
{
    abstract var kills: Int
    abstract var deaths: Int

    abstract var played: Int
    abstract var wins: Int
    abstract var losses: Int
}
