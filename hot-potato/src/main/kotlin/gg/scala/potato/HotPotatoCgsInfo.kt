package gg.scala.potato

import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.potato.gamemode.HotPotatoSolo

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object HotPotatoCgsInfo : CgsGameGeneralInfo(
    "Hot Potato", 0.01F, 2,
    12, HotPotatoCgsAwards, true,
    true, false, false,
    listOf(HotPotatoSolo)
)
