package gg.scala.ktp

import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.visibility.CgsGameVisibilityAdapter
import net.evilblock.cubed.visibility.VisibilityAction

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerCgsVisibilityAdapter : CgsGameVisibilityAdapter
{
    override fun computeVisibility(viewer: CgsGamePlayer, target: CgsGamePlayer): VisibilityAction
    {
        return VisibilityAction.NEUTRAL
    }
}
