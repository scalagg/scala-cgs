package gg.scala.cgs.lobby.gamemode

import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.game.CgsGameServerInfo
import gg.scala.cgs.common.instance.handler.CgsInstanceHandler
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEngine
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.locator.CgsInstanceLocator
import gg.scala.cgs.lobby.modular.CgsLobbyModule
import gg.scala.cgs.lobby.modular.CgsLobbyModuleItems
import gg.scala.cgs.lobby.updater.CgsGameInfoUpdater
import gg.scala.tangerine.TangerineSpigotPlugin
import me.lucko.helper.Events
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 12/4/2021
 */
abstract class CgsGameLobby<S : GameSpecificStatistics>
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsGameLobby<*>>()
    }

    abstract fun getScoreboardAdapter(): ScoreboardAdapter
    abstract fun getGameInfo(): CgsGameGeneralInfo

    abstract fun getRankingEntries(): Collection<CgsLobbyRankingEntry<*>>

    abstract fun getGameModeButtons(): Map<Int, Button>
    abstract fun getFormattedButton(info: CgsServerInstance): Button

    var statisticType by Delegates.notNull<KClass<S>>()

    fun initialResourceLoad()
    {
        CgsPlayerHandler.initialLoad()
        CgsInstanceHandler.initialLoad(CgsServerType.LOBBY)

        TangerineSpigotPlugin.instance.hubModule = CgsLobbyModule

        CgsLobbyModuleItems.initialLoad()
        CgsGameInfoUpdater.start()
        CgsLobbyRankingEngine.initialLoad()

        Events.subscribe(PlayerJoinEvent::class.java).handler {
            CgsPlayerHandler.find(it.player)?.let { player ->
                try
                {
                    getStatistics(player)
                } catch (ignored: Exception)
                {
                    player.gameSpecificStatistics[statisticType.java.simpleName] = statisticType.java.newInstance() as S
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
    {
        return cgsGamePlayer.gameSpecificStatistics[statisticType.java.simpleName]!! as S
    }
}
