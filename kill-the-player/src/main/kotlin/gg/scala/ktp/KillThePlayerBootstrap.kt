package gg.scala.ktp

import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.ktp.gamemode.KillThePlayerSoloGameMode
import me.lucko.helper.Events
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class KillThePlayerBootstrap : ExtendedScalaPlugin()
{
    override fun enable()
    {
        CgsGameArenaHandler.initialLoad(
            KillThePlayerSoloGameMode
        )

        val engine = KillThePlayerCgsEngine(
            this, KillThePlayerCgsInfo,
            KillThePlayerSoloGameMode
        )
        engine.statisticType = KillThePlayerStatistics::class
        engine.initialLoad()

        KillThePlayerCgsEngine.INSTANCE = engine
    }
}
