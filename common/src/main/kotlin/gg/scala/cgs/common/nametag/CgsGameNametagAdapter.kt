package gg.scala.cgs.common.nametag

import gg.scala.cgs.common.player.CgsGamePlayer
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.visibility.VisibilityAction

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
interface CgsGameNametagAdapter
{
    fun computeNametag(viewer: CgsGamePlayer, target: CgsGamePlayer): NametagInfo?
}
