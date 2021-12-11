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
        if (refreshFor.hasMetadata("spectator"))
        {
            return if (toRefresh.hasMetadata("spectator"))
            {
                VisibilityAction.NEUTRAL
            } else
            {
                VisibilityAction.HIDE
            }
        }

        val viewer = CgsPlayerHandler.find(toRefresh)!!
        val target = CgsPlayerHandler.find(refreshFor)!!

        return CgsGameEngine.INSTANCE.getVisibilityAdapter()
            .computeVisibility(viewer, target)
    }
}
