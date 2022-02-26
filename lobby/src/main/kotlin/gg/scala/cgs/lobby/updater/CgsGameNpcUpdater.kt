package gg.scala.cgs.lobby.updater

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Schedulers
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 2/26/2022
 */
@Service
object CgsGameNpcUpdater : Runnable
{
    @Configure
    fun configure()
    {
        Schedulers.async().runRepeating(
            this, 0L, 10L
        )
    }

    override fun run()
    {
        EntityHandler.getEntitiesByType(NpcEntity::class.java).forEach {
            val firstLine = it.hologram
                .getLines()[0]

            val gameModeType = CgsGameLobby.INSTANCE
                .getGameInfo().gameModes
                .firstOrNull { game ->
                    game.getId().equals(firstLine, true)
                } ?: return@forEach

            val lines = mutableListOf<String>()
            lines.add("${CC.BD_AQUA}${gameModeType.getName()}")
            lines.add("${CC.SEC}Playing: ${CC.PRI}${
                CgsGameInfoUpdater.gameModeCounts[gameModeType.getId()] ?: "N/A"
            }")
            lines.add("${CC.GREEN}Click to play!")

            it.hologram.updateLines(lines)
        }
    }
}

