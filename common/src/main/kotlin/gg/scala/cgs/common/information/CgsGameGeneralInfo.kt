package gg.scala.cgs.common.information

import gg.scala.cgs.common.information.mode.CgsGameMode

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
data class CgsGameGeneralInfo(
    val fancyNameRender: String,
    val gameVersion: Float,
    val minimumPlayers: Int,
    val startingCountdownSec: Int,
    val persistence: CgsGamePersistenceInfo,
    val awards: CgsGameAwardInfo,
    val disqualifyOnLogout: Boolean,
    val spectateOnDeath: Boolean,
    val showTabHearts: Boolean,
    val gameModes: List<CgsGameMode>
)
