package gg.scala.cgs.common.information

import gg.scala.cgs.common.information.mode.CgsGameMode

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
data class CgsGameGeneralInfo(
    val fancyNameRender: String,
    val minimumPlayers: Int,
    val startingCountdownSec: Int,
    val disqualifyOnDeath: Boolean,
    val persistence: CgsGamePersistenceInfo,
    val awards: CgsGameAwardInfo,
    val spectatable: Boolean,
    val gameModes: List<CgsGameMode>
)
