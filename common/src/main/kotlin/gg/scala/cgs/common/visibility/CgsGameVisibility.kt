package gg.scala.cgs.common.visibility

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.handler.CgsPlayerHandler
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
        val viewer = CgsPlayerHandler.find(refreshFor)!!
        val target = CgsPlayerHandler.find(toRefresh)!!

        if (refreshFor.hasMetadata("spectator") && !toRefresh.hasMetadata("spectator"))
        {
            return VisibilityAction.HIDE
        } else if (refreshFor.hasMetadata("spectator") && toRefresh.hasMetadata("spectator"))
        {
            return VisibilityAction.NEUTRAL
        }

        return CgsGameEngine.INSTANCE.getVisibilityAdapter()
            .computeVisibility(viewer, target)
    }
}
