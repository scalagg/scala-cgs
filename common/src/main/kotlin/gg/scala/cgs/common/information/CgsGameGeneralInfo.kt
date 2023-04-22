package gg.scala.cgs.common.information

import gg.scala.cgs.common.information.mode.CgsGameMode
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
open class CgsGameGeneralInfo(
    val fancyNameRender: String,
    val gameVersion: Float,
    val minimumPlayers: Int,
    val startingCountdownSec: Int,
    val awards: CgsGameAwardInfo,
    val preStartVoting: Boolean,
    val disqualifyOnLogout: Boolean,
    val spectateOnDeath: Boolean,
    val showTabHearts: Boolean,
    val showNameHearts: Boolean,
    val usesCustomArenaWorld: Boolean,
    val gameModes: List<CgsGameMode>
)
{
    var configureSpectatorChat: Boolean = true
    var requiresNoManualConfiguration = false

    var customDeathMessage: Boolean = false
    var customDeathMessageService = { player: Player -> "" }
}
