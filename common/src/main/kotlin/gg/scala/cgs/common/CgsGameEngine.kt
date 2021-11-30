package gg.scala.cgs.common

import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.information.CgsGameInfo
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import org.bukkit.Bukkit
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 11/30/2021
 */

// PS - profile statistics
// S - state enum (specific)

// LP - local player - for anything
// related to this specific game
abstract class CgsGameEngine<S : GameSpecificStatistics>(
    val plugin: ExtendedJavaPlugin,
    val gameInfo: CgsGameInfo,
    val gameMode: CgsGameMode
)
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsGameEngine<*>>()
    }

    lateinit var statisticType: KClass<*>

    inline fun <reified T : GameSpecificStatistics> setStatisticType()
    {
        statisticType = T::class
    }

    fun initialResourceLoad()
    {
        INSTANCE = this
        CgsPlayerHandler.initialLoad()

        Serializers.useGsonBuilderThenRebuild {
            it.registerTypeAdapter(GameSpecificStatistics::class.java, AbstractTypeSerializer<GameSpecificStatistics>())
        }

        Bukkit.getServer().maxPlayers = gameMode.getMaxTeams() * gameMode.getTeamSize()
    }

    @Suppress("UNCHECKED_CAST")
    fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
    {
        return cgsGamePlayer.gameSpecificStatistics[statisticType.simpleName]!! as S
    }
}
