package gg.scala.cgs.common.menu

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.sponsor.SponsorMenu
import gg.scala.cgs.common.sponsor.event.PreSponsorPlayerEvent
import gg.scala.cgs.common.sponsor.event.SponsorPlayerEvent
import gg.scala.cgs.common.teams.CgsGameTeamService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class CgsGameSpectateMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        var filter = { _: Player -> true }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { buttons ->
            CgsGameTeamService.teams.values.forEach { team ->
                team.alive
                    .mapNotNull {
                        Bukkit.getPlayer(it)
                    }
                    .filter(filter)
                    .forEach second@{
                        if (it.hasMetadata("spectator"))
                        {
                            return@second
                        }

                        buttons[buttons.size] = SpectateButton(it)
                    }
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player) = "Spectate"

    inner class SpectateButton(
        private val player: Player
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder(Material.SKULL_ITEM)
                .owner(this.player.name)
                .name("${CC.GREEN}${this.player.name}")
                .addToLore(
                    "${CC.GRAY}Click to teleport."
                )
                .apply {
                    if (CgsGameEngine.INSTANCE.getSponsorConfig() != null)
                    {
                        addToLore("${CC.GOLD}Shift-click to sponsor.")
                    }
                }
                .data(3)
                .build()
        }

        override fun clicked(
            player: Player, slot: Int,
            clickType: ClickType, view: InventoryView
        )
        {
            val sponsorConfig = CgsGameEngine.INSTANCE.getSponsorConfig()

            if (clickType.isShiftClick && sponsorConfig != null)
            {

                val event = PreSponsorPlayerEvent(player, this.player)
                event.callEvent()

                if (event.isCancelled)
                {
                    return
                }

                SponsorMenu(this.player) { prize ->
                    if (prize.cost > CgsGameEngine.INSTANCE.platform.getCoins(player))
                    {
                        player.sendMessage("${CC.RED}You do not have enough coins to sponsor this player!")
                        return@SponsorMenu
                    }

                    if (!prize.canApply(this.player))
                    {
                        player.sendMessage("${CC.RED}We cannot apply the prize to ${CC.YELLOW}${this.player.name}${CC.RED}, your coins have been refunded.")
                        return@SponsorMenu
                    }

                    ConfirmMenu(
                        title = "Sponsor ${player.name}",
                        confirm = true
                    ) {
                        if (it)
                        {
                            CgsGameEngine.INSTANCE.platform
                                .giveCoins(player, -prize.cost to "", false)

                            prize.apply(this.player)

                            Bukkit.getPluginManager().callEvent(SponsorPlayerEvent(player, this.player, prize))
                            player.sendMessage("${CC.GREEN}You sponsored ${this.player.displayName}${CC.GREEN}!")

                            Bukkit.broadcastMessage("${this.player.displayName}${CC.SEC} was sponsored by ${CC.GREEN}${player.name}${CC.SEC}!")
                        }
                    }.openMenu(player)
                }
                return
            }

            player.teleport(this.player)
            player.sendMessage("${CC.GREEN}You've been teleported to ${this.player.name}.")
        }
    }
}
