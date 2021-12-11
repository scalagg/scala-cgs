package gg.scala.ktp.player

import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.visibility.CgsGameVisibilityAdapter
import net.evilblock.cubed.visibility.VisibilityAction

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerVisibilityAdapter : CgsGameVisibilityAdapter
{
    override fun computeVisibility(
        viewer: CgsGamePlayer, target: CgsGamePlayer
    ): VisibilityAction
    {
        return VisibilityAction.NEUTRAL
    }
}
