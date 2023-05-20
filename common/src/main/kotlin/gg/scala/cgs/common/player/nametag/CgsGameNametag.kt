package gg.scala.cgs.common.player.nametag

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.sorter.SortedRankCache
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsGameNametag : NametagProvider(
    "cgs", 1000
)
{
    @JvmStatic
    val SPECTATOR = createNametag(CC.GRAY, "")

    @JvmStatic
    val GREEN = createNametag(CC.GREEN, "")

    @JvmStatic
    val RED = createNametag(CC.RED, "")

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo
    {
        val viewer = CgsPlayerHandler.find(toRefresh)!!
        val target = CgsPlayerHandler.find(refreshFor)!!

        val lemonPlayer = PlayerHandler
            .find(toRefresh.uniqueId)
            ?: return SPECTATOR

        val rank = lemonPlayer.disguiseRank()
            ?: QuickAccess.realRank(toRefresh)

        if (
            toRefresh.hasMetadata("spectator") &&
            refreshFor.hasMetadata("spectator")
        )
        {
            return createNametag(CC.GRAY, "", "§0§9§9z")
        }

        val computed = CgsGameEngine.INSTANCE.getNametagAdapter()
            .computeNametag(viewer, target)

        val teamOfViewer = CgsGameTeamService.getTeamOf(toRefresh)
        val teamOfTarget = CgsGameTeamService.getTeamOf(refreshFor)

        return computed ?: createNametag(
            if (teamOfTarget == teamOfViewer) CC.GREEN else CC.RED,
            "",
            "§0§9§9" + (SortedRankCache.teamMappings[rank.uuid] ?: "z")
        )
    }
}
