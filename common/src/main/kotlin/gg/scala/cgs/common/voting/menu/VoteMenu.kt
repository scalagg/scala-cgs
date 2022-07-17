package gg.scala.cgs.common.voting.menu

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.voting.CgsVotingMapService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.distribution.MenuRowDistribution
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/16/2022
 */
class VoteMenu : Menu()
{
    init
    {
        autoUpdate = true
    }

    override fun size(buttons: Map<Int, Button>) = 27

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>()
            .apply {
                for (entry in CgsVotingMapService.configuration.entries())
                {
                    this[10 + buttons.size] = ItemBuilder
                        .of(entry.item)
                        .setLore(
                            TextSplitter.split(
                                entry.description,
                                linePrefix = CC.GRAY,
                                wordSuffix = ""
                            )
                        )
                        .addToLore(
                            "",
                            "${CC.GRAY}Votes: ${CC.WHITE}${
                                CgsVotingMapService.selections[entry.id]!!.size
                            }",
                            "",
                            "${CC.GREEN}Click to vote..."
                        )
                        .name("${CC.YELLOW}${entry.displayName}")
                        .toButton { _, _ ->
                            CgsVotingMapService.invalidatePlayerVote(player)
                            CgsVotingMapService.registerVote(player, entry.id)
                        }
                }
            }
    }

    override fun getTitle(player: Player) = "Vote for a map..."
}
