package gg.scala.potato.player

import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.nametag.CgsGameNametagAdapter
import net.evilblock.cubed.nametag.NametagInfo

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object HotPotatoNametagAdapter : CgsGameNametagAdapter
{
    override fun computeNametag(viewer: CgsGamePlayer, target: CgsGamePlayer): NametagInfo?
    {
        return null
    }
}
