package gg.scala.potato

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.potato.player.HotPotatoNametagAdapter
import gg.scala.potato.player.HotPotatoScoreboard
import gg.scala.potato.player.HotPotatoVisibility
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class HotPotatoEngine(
    plugin: ExtendedScalaPlugin,
    gameInfo: CgsGameGeneralInfo,
    gameMode: CgsGameMode
) : CgsGameEngine<HotPotatoCgsStatistics>(
    plugin, gameInfo, gameMode
)
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<HotPotatoEngine>()
    }

    override fun onTick(state: CgsGameState, tickOfState: Int): Boolean
    {
        return true
    }

    override fun getScoreboardRenderer() = HotPotatoScoreboard
    override fun getVisibilityAdapter() = HotPotatoVisibility
    override fun getNametagAdapter() = HotPotatoNametagAdapter
    override fun getSnapshotCreator() = HotPotatoCgsSnapshot

    override fun getExtraWinInformation() = listOf(" Something something something")
}
