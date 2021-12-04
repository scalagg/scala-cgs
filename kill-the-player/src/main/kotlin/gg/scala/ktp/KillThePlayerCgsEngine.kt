package gg.scala.ktp

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.commons.ExtendedScalaPlugin

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class KillThePlayerCgsEngine(
    plugin: ExtendedScalaPlugin,
    gameInfo: CgsGameGeneralInfo,
    gameMode: CgsGameMode
) : CgsGameEngine<KillThePlayerStatistics>(
    plugin, gameInfo, gameMode
)
{
    companion object
    {
        @JvmStatic
        lateinit var INSTANCE: KillThePlayerCgsEngine
    }

    override fun onTick(state: CgsGameState, tickOfState: Int): Boolean
    {
        return true
    }

    override fun getScoreboardRenderer() = KillThePlayerCgsBoardRenderer
    override fun getVisibilityAdapter() = KillThePlayerCgsVisibilityAdapter
    override fun getNametagAdapter() = KillThePlayerCgsNametagAdapter
    override fun getSnapshotCreator() = KillThePlayerCgsSnapshot

    override fun getExtraWinInformation() = listOf("This is extra info")
}
