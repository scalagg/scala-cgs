package gg.scala.cgs.common.voting.menu

import gg.scala.cgs.common.voting.CgsVotingMapService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
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
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 27

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        for (entry in CgsVotingMapService.configuration.entries())
        {
            buttons[10 + buttons.size] = ItemBuilder
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
                .name("${CC.B_YELLOW}${entry.displayName}")
                .toButton { _, _ ->
                    CgsVotingMapService.invalidatePlayerVote(player)
                    CgsVotingMapService.registerVote(player, entry.id)
                }
        }

        return buttons
    }

    override fun getTitle(player: Player) = "Vote for a map..."
}
