package gg.scala.ktp

import gg.scala.cgs.common.nametag.CgsGameNametagAdapter
import gg.scala.cgs.common.player.CgsGamePlayer
import net.evilblock.cubed.nametag.NametagInfo

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerCgsNametagAdapter : CgsGameNametagAdapter
{
    override fun computeNametag(viewer: CgsGamePlayer, target: CgsGamePlayer): NametagInfo?
    {
        return null
    }
}
