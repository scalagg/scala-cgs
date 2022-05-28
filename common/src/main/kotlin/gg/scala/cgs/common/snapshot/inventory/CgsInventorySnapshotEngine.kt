package gg.scala.cgs.common.snapshot.inventory

import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
object CgsInventorySnapshotEngine
{
    val snapshots =
        mutableMapOf<UUID, CgsInventorySnapshot>()

    fun takeSnapshot(
        player: Player,
        replace: Boolean = true
    ): CgsInventorySnapshot
    {
        if (!replace)
        {
            val existing = snapshots[player.uniqueId]

            if (existing != null)
            {
                return existing
            }
        }

        val snapshot =
            CgsInventorySnapshot(player.uniqueId)

        snapshot.compose(player)

        return snapshot.apply {
            snapshots[player.uniqueId] = this
        }
    }
}
