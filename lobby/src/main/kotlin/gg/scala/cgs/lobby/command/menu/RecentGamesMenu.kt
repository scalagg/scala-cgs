package gg.scala.cgs.lobby.command.menu

import gg.scala.cgs.common.snapshot.wrapped.CgsWrappedGameSnapshot
import gg.scala.lemon.LemonConstants
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
        autoUpdate = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36
    override fun getAllPagesButtonSlots(): List<Int> = SLOTS

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            val sortedGames = recentGames.sortedByDescending {
                    game -> game.datePlayed.time
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
                .addToLore(
                    "",
                    *recentGame.extraInformation
                        .map {
                            it.removePrefix(" ")
                        }
                        .toTypedArray(),
                    "",
                    "${CC.GREEN}Click to view on web!"
                )
                .name("${CC.D_AQUA}${recentGame.gameName}")
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            player.sendMessage("${CC.SEC}View this game on our website: ${CC.GREEN}${
                "${LemonConstants.WEB_LINK}/game/${recentGame.identifier}"
            }")
        }
    }
}
