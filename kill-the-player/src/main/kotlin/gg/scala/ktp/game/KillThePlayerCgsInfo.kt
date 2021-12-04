package gg.scala.ktp.game

import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.ktp.game.gamemode.KillThePlayerSoloGameMode

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerCgsInfo : CgsGameGeneralInfo(
    "Kill the Player", 0.01F, 2,
    20, KillThePlayerCgsAwards, true,
    true, false, true, listOf(KillThePlayerSoloGameMode)
)
