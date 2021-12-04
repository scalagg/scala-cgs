package gg.scala.cgs.common.player.visibility

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import net.evilblock.cubed.visibility.VisibilityAction
import net.evilblock.cubed.visibility.VisibilityAdapter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsGameVisibility : VisibilityAdapter
{
    override fun getAction(toRefresh: Player, refreshFor: Player): VisibilityAction
    {
        if (refreshFor.hasMetadata("spectator") && !toRefresh.hasMetadata("spectator"))
        {
            return VisibilityAction.HIDE
        } else if (refreshFor.hasMetadata("spectator") && toRefresh.hasMetadata("spectator"))
        {
            return VisibilityAction.NEUTRAL
        }

        val viewer = CgsPlayerHandler.find(refreshFor)!!
        val target = CgsPlayerHandler.find(toRefresh)!!

        return CgsGameEngine.INSTANCE.getVisibilityAdapter()
            .computeVisibility(viewer, target)
    }
}
