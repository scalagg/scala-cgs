package gg.scala.ktp

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.ktp.gamemode.KillThePlayerSoloGameMode
import me.lucko.helper.Events
import org.bukkit.Bukkit

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

        Events.subscribe(CgsGameEngine.CgsGameStartEvent::class.java).handler {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.inventory.armorContents = KillThePlayerInventory.ARMOR
                player.inventory.setItem(0, KillThePlayerInventory.SWORD)
            }
        }

        KillThePlayerCgsEngine.INSTANCE = engine
    }
}
