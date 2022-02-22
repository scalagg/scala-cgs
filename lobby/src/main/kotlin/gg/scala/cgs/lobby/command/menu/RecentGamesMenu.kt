package gg.scala.cgs.lobby.command.menu

import gg.scala.cgs.common.snapshot.wrapped.CgsWrappedGameSnapshot
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 2/22/2022
 */
class RecentGamesMenu(
    private val recentGames: List<CgsWrappedGameSnapshot>
) : PaginatedMenu()
{
    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            for (recentGame in recentGames)
                it[it.size] = RecentGameButton(recentGame)
        }
    }

    override fun getPrePaginatedTitle(player: Player) = "Recent Games"

    inner class RecentGameButton(
        private val recentGame: CgsWrappedGameSnapshot
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder.of(Material.valueOf(recentGame.icon))
                .addToLore(
                    "${CC.D_GRAY}${
                        TimeUtil.formatIntoCalendarString(recentGame.datePlayed)
                    }",
                    "${CC.SEC}Duration: ${CC.PRI}${
                        TimeUtil.formatIntoDetailedString((recentGame.elapsedTime / 1000).toInt())
                    }",
                    "",
                    "${CC.SEC}Mode: ${CC.PRI}${
                        recentGame.gameMode
                    }",
                )
                .also {
                    if (recentGame.mapName != "Random")
                    {
                        it.addToLore("${CC.SEC}Map: ${CC.PRI}${
                            recentGame.mapName
                        }")
                    }
                }
                .addToLore(
                    "",
                    "${CC.SEC}Server: ${CC.PRI}${
                        recentGame.server
                    }",
                    "${CC.SEC}Players: ${CC.PRI}${
                        recentGame.players.size
                    }"
                )
                .name("${CC.GREEN}${recentGame.gameName}")
                .build()
        }
    }
}
