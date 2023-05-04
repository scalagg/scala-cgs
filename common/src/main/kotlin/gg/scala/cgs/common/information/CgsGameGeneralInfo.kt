package gg.scala.cgs.common.information

import gg.scala.cgs.common.information.mode.CgsGameMode
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
open class CgsGameGeneralInfo(
    var fancyNameRender: String,
    var gameVersion: Float,
    var minimumPlayers: Int,
    var startingCountdownSec: Int,
    var awards: CgsGameAwardInfo,
    var preStartVoting: Boolean,
    var disqualifyOnLogout: Boolean,
    var spectateOnDeath: Boolean,
    var showTabHearts: Boolean,
    var showNameHearts: Boolean,
    var usesCustomArenaWorld: Boolean,
    var gameModes: List<CgsGameMode>
)
{
    var configureSpectatorChat: Boolean = true
    var requiresNoManualConfiguration = true
    var timeUntilShutdown = 10

    var customDeathMessage: Boolean = false
    var customDeathMessageService = { player: Player -> "" }
}
