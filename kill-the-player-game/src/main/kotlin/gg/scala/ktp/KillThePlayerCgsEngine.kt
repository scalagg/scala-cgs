package gg.scala.ktp

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.ktp.game.KillThePlayerCgsSnapshot
import gg.scala.ktp.game.KillThePlayerCgsStatistics
import gg.scala.ktp.player.KillThePlayerNametagAdapter
import gg.scala.ktp.player.KillThePlayerScoreboard
import gg.scala.ktp.player.KillThePlayerVisibilityAdapter
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class KillThePlayerCgsEngine(
    plugin: ExtendedScalaPlugin,
    gameInfo: CgsGameGeneralInfo,
    gameMode: CgsGameMode
) : CgsGameEngine<KillThePlayerCgsStatistics>(
    plugin, gameInfo, gameMode
)
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<KillThePlayerCgsEngine>()
    }

    override fun onTick(state: CgsGameState, tickOfState: Int): Boolean
    {
        return true
    }

    override fun getScoreboardRenderer() = KillThePlayerScoreboard
    override fun getVisibilityAdapter() = KillThePlayerVisibilityAdapter
    override fun getNametagAdapter() = KillThePlayerNametagAdapter
    override fun getSnapshotCreator() = KillThePlayerCgsSnapshot

    override fun getExtraWinInformation() = listOf(" Something something something")
}
