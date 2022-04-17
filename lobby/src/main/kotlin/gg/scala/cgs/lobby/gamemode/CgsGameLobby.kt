package gg.scala.cgs.lobby.gamemode

import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.handler.CgsInstanceService
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.common.statistics.CgsStatisticProvider
import gg.scala.cgs.common.statistics.CgsStatisticService
import gg.scala.cgs.lobby.CgsLobbyPlugin
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.modular.CgsLobbyModule
import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.tangerine.TangerineSpigotPlugin
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import org.bukkit.entity.Player
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 12/4/2021
 */
abstract class CgsGameLobby<S : GameSpecificStatistics>(
    override val statisticType: KClass<S>
) : CgsStatisticProvider<S>
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsGameLobby<*>>()
    }

    abstract fun getScoreboardAdapter(): ScoreboardAdapter
    abstract fun getGameInfo(): CgsGameGeneralInfo

    abstract fun getRankingEntries(): Collection<CgsLobbyRankingEntry>

    abstract fun getGameModeButtons(): Map<Int, Button>
    abstract fun getFormattedButton(info: CgsServerInstance, player: Player): Button

    fun configureResources(
        plugin: ExtendedScalaPlugin
    )
    {
        Serializers.create {
            registerTypeAdapter(
                GameSpecificStatistics::class.java,
                AbstractTypeSerializer<GameSpecificStatistics>()
            )
        }

        CgsInstanceService
            .configure(CgsServerType.LOBBY)

        TangerineSpigotPlugin.instance
            .hubModule = CgsLobbyModule

        plugin.flavor {
            bind<CgsGameLobby<S>>() to this
            bind<CgsLobbyPlugin>() to CgsLobbyPlugin.INSTANCE
            bind<CgsStatisticProvider<S>>() to this

            injected<CgsStatisticService<S>>().configure()
            startup()
        }

        CloudSyncDiscoveryService
            .discoverable.assets
            .apply {
                add("gg.scala.cgs:lobby:cgs-lobby")
                add("gg.scala.cgs:parties:cgs-parties")
            }
    }

    override fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
    {
        return cgsGamePlayer.gameSpecificStatistics[statisticType.java.simpleName]!! as S
    }
}
