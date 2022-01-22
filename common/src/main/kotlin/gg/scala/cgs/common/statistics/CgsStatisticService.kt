package gg.scala.cgs.common.statistics

import gg.scala.cgs.common.ClassReifiedParameterUtil.getType
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import me.lucko.helper.Events
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author GrowlyX
 * @since 1/22/2022
 */
class CgsStatisticService<S : GameSpecificStatistics>
{
    @Inject
    lateinit var engine: CgsStatisticProvider<S>

    @Configure
    fun configure()
    {
        Events.subscribe(PlayerJoinEvent::class.java).handler {
            CgsPlayerHandler.find(it.player)?.let { player ->
                try
                {
                    engine.getStatistics(player)
                } catch (ignored: Exception)
                {
                    val type = this::class.getType()

                    player.gameSpecificStatistics[type.java.simpleName] = type.java.newInstance() as S
                }
            }
        }
    }
}
