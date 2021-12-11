package gg.scala.potato

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.ktp.game.KillThePlayerCgsInfo
import gg.scala.ktp.game.gamemode.KillThePlayerSoloGameMode
import gg.scala.potato.player.HotPotatoItems
import gg.scala.ktp.game.KillThePlayerCgsStatistics
import me.lucko.helper.Events
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class HotPotatoPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        CgsGameArenaHandler.initialLoad(
            KillThePlayerSoloGameMode
        )

        val engine = HotPotatoEngine(
            this, KillThePlayerCgsInfo,
            KillThePlayerSoloGameMode
        )
        engine.statisticType = KillThePlayerCgsStatistics::class
        engine.initialLoad()

        Events.subscribe(CgsGameEngine.CgsGameStartEvent::class.java).handler {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.inventory.armorContents = HotPotatoItems.ARMOR
                player.inventory.contents = HotPotatoItems.CONTENTS

                player.updateInventory()
            }
        }

        HotPotatoEngine.INSTANCE = engine
    }
}
