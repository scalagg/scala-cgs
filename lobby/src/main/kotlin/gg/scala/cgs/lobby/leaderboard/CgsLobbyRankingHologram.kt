package gg.scala.cgs.lobby.leaderboard

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.updating.FormatUpdatingHologramEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
class CgsLobbyRankingHologram(
    location: Location,
    private val entryId: String
) : FormatUpdatingHologramEntity(
    entryId, location
)
{
    fun configure()
    {
        initializeData()
        EntityHandler.trackEntity(this)
    }

    override fun getNewLines() = mutableListOf<String>().also {
        it.add("${CC.B_PRI}${CgsGameLobby.INSTANCE.getGameInfo().fancyNameRender}")
        it.add("${CC.GRAY}Top 10 ${entryId.capitalize()}")
        it.add("")
        it.addAll(CgsLobbyRankingEngine.ID_TO_FORMAT[entryId] ?: listOf())
    }

    override fun getTickInterval() = 15 * 20L
}
