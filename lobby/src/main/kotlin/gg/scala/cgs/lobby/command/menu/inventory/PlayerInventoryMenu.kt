package gg.scala.cgs.lobby.command.menu.inventory

import gg.scala.cgs.common.snapshot.CgsGameSnapshot
import gg.scala.cgs.common.snapshot.inventory.CgsInventorySnapshot
import gg.scala.cgs.common.snapshot.wrapped.CgsWrappedGameSnapshot
import gg.scala.cgs.lobby.command.menu.RecentGamesMenu
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
class PlayerInventoryMenu(
    private val recentGamesMenu: RecentGamesMenu,
    private val snapshot: CgsWrappedGameSnapshot
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
        async = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36
    override fun getAllPagesButtonSlots(): List<Int> = SLOTS

    val coloredUsernames = mutableMapOf<UUID, String>()

    override fun asyncLoadResources(
        player: Player, callback: (Boolean) -> Unit
    )
    {
        if (coloredUsernames.isNotEmpty())
        {
            // We don't want to preload usernames
            // every time this menu is opened.
            callback.invoke(true)
            return
        }

        CompletableFuture.runAsync {
            for (key in snapshot.snapshots.keys)
            {
                val colored = QuickAccess
                    .fetchColoredName(key)

                // we're going to default the username
                // to &aUsername as something may go wrong
                // in the process of fetching the username
                coloredUsernames[key] =
                    "${CC.GREEN}$colored"
            }
        }.whenComplete { _, throwable ->
            if (throwable != null)
            {
                throwable.printStackTrace()
                callback.invoke(false)
                return@whenComplete
            }

            callback.invoke(true)
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().apply {
            for (snapshot in snapshot.snapshots)
            {
                this[size] = SnapshotButton(snapshot.value)
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player) =
        "Recent Games ${Constants.DOUBLE_ARROW_RIGHT} Inventories"

    override fun onClose(
        player: Player, manualClose: Boolean
    )
    {
        if (manualClose)
        {
            Tasks.delayed(1L) {
                recentGamesMenu.openMenu(player)
            }
        }
    }

    inner class SnapshotButton(
        private val snapshot: CgsInventorySnapshot
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            val username = CubedCacheUtil
                .fetchName(snapshot.uniqueId)!!

            val description = mutableListOf<String>()
            description += "${CC.SEC}Click to view inventory!"

            return ItemBuilder.of(Material.SKULL)
                .owner(username)
                .name(coloredUsernames[snapshot.uniqueId])
                .setLore(description)
                .build()
        }

        override fun clicked(
            player: Player, slot: Int,
            clickType: ClickType, view: InventoryView
        )
        {
            val inventoryMenu = PlayerInventoryViewMenu(
                this@PlayerInventoryMenu, snapshot
            )

            inventoryMenu.openMenu(player)
        }
    }
}
