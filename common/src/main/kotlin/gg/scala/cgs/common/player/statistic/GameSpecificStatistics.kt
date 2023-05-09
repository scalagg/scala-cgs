package gg.scala.cgs.common.player.statistic

import gg.scala.cgs.common.player.statistic.value.CgsGameStatistic
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
abstract class GameSpecificStatistics(
    override val identifier: UUID
) : AbstractTypeSerializable, IDataStoreObject
{
    abstract fun save(): CompletableFuture<Void>

    var gameKills = CgsGameStatistic()

    var kills = CgsGameStatistic()
    var deaths = CgsGameStatistic()

    var wins = CgsGameStatistic()
    var losses = CgsGameStatistic()
}
