package gg.scala.cgs.common.information

import gg.scala.cgs.common.information.mode.CgsGameMode

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
data class CgsGameInfo(
    val fancyNameRender: String,
    val minimumPlayers: Int,
    val disqualifyOnDeath: Boolean,
    val persistence: CgsGamePersistenceInfo,
    val spectatable: Boolean,
    val gameModes: List<CgsGameMode>
)
