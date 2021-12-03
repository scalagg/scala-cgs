package gg.scala.cgs.common.nametag

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.handler.CgsPlayerHandler
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsGameNametag : NametagProvider(
    "cgs", 10
)
{
    @JvmStatic
    val SPECTATOR = createNametag(CC.GRAY, "")

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo?
    {
        val viewer = CgsPlayerHandler.find(refreshFor)!!
        val target = CgsPlayerHandler.find(toRefresh)!!

        if (toRefresh.hasMetadata("spectator") && refreshFor.hasMetadata("spectator"))
        {
            return SPECTATOR
        }

        return CgsGameEngine.INSTANCE.getNametagAdapter()
            .computeNametag(viewer, target)
    }
}
