package gg.scala.cgs.common.snapshot.wrapped

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.snapshot.CgsGameSnapshot
import gg.scala.cgs.common.snapshot.inventory.CgsInventorySnapshotEngine
import gg.scala.lemon.Lemon
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 2/22/2022
 */
class CgsWrappedGameSnapshot(
    // we don't want this in the
    // final serialized form
    snapshot: CgsGameSnapshot,
    override val identifier: UUID =
        CgsGameEngine.INSTANCE.uniqueId
) : IDataStoreObject
{
    val extraInformation =
        snapshot.getExtraInformation()

    val elapsedTime =
        System.currentTimeMillis() - CgsGameEngine.INSTANCE.gameStart

    val mapName = CgsGameEngine
        .INSTANCE.gameArena.getName()

    val gameName = CgsGameEngine.INSTANCE
        .gameInfo.fancyNameRender

    val gameMode = CgsGameEngine.INSTANCE
        .gameMode.getName()

    val server = Lemon.instance
        .settings.id

    val players = CgsGameEngine.INSTANCE
        .originalRemaining

    val icon = CgsGameEngine.INSTANCE
        .gameMode.getMaterial().first.name

    val snapshots =
        CgsInventorySnapshotEngine.snapshots

    val datePlayed = Date()
}
