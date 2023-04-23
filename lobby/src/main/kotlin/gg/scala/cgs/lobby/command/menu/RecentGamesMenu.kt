package gg.scala.cgs.lobby.command.menu

import gg.scala.cgs.common.snapshot.wrapped.CgsWrappedGameSnapshot
import gg.scala.cgs.lobby.command.menu.inventory.PlayerInventoryMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 2/22/2022
 */
class RecentGamesMenu(
    private val recentGames: List<CgsWrappedGameSnapshot>
) : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
        )
    }

    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36
    override fun getAllPagesButtonSlots(): List<Int> = SLOTS

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            val sortedGames = recentGames
                .sortedByDescending { game ->
                    game.datePlayed.time
                }

            for (recentGame in sortedGames)
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
                    "${CC.GRAY}Duration: ${CC.GREEN}${
                        TimeUtil.formatIntoDetailedString((recentGame.elapsedTime / 1000).toInt())
                    }",
                    "",
                    "${CC.GRAY}Mode: ${CC.GREEN}${
                        recentGame.gameMode
                    }",
                )
                .also {
                    if (recentGame.mapName != "Random")
                    {
                        it.addToLore("${CC.GRAY}Map: ${CC.GREEN}${
                            recentGame.mapName
                        }")
                    }
                }
                .addToLore(
                    "",
                    "${CC.GRAY}Server: ${CC.GREEN}${
                        recentGame.server
                    }",
                    "${CC.GRAY}Players: ${CC.GREEN}${
                        recentGame.players.size
                    }"
                )
                .addToLore(
                    "",
                    *recentGame.extraInformation
                        .map {
                            it.removePrefix(" ")
                        }
                        .toTypedArray(),
                    "",
                    "${CC.GREEN}Click to view snapshots!"
                )
                .name("${CC.GREEN}${recentGame.gameName}")
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            PlayerInventoryMenu(
                this@RecentGamesMenu,
                recentGame
            ).openMenu(player)
        }
    }
}
