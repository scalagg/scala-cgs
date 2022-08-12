package gg.scala.cgs.common.sponsor

import gg.scala.cgs.common.CgsGameEngine
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
class SponsorMenu(var sponsoring: Player, var lambda: (SponsorPrize) -> Unit) : PaginatedMenu()
{
    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Sponsor - ${sponsoring.name}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { buttons ->
            val config = CgsGameEngine.INSTANCE.getSponsorConfig()

            for (prize in config!!.getPrizes())
            {
                buttons[buttons.size] = SponsorPrizeButton(prize)
            }
        }
    }

    private inner class SponsorPrizeButton(var sponsorPrize: SponsorPrize) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder.copyOf(sponsorPrize.toItemStack())
                .name("${CC.GREEN}${sponsorPrize.name}")
                .addToLore("", "${CC.YELLOW}Click to sponsor this to ${CC.GREEN}${player.name}${CC.YELLOW}.")
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            lambda.invoke(sponsorPrize)
        }
    }
}