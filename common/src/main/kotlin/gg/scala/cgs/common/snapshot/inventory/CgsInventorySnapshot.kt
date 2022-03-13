package gg.scala.cgs.common.snapshot.inventory

import net.evilblock.cubed.util.bukkit.player.PlayerSnapshot
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
class CgsInventorySnapshot(
    val uniqueId: UUID
)
{
    lateinit var internal: PlayerSnapshot

    fun compose(player: Player)
    {
        internal = PlayerSnapshot(player)
    }

    fun restore(player: Player)
    {
        internal.restore(player)
    }
}
