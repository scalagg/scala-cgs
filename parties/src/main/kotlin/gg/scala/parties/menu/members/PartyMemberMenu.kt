package gg.scala.parties.menu.members

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.parties.menu.PartyManageMenu
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyMember
import gg.scala.parties.model.PartyRole
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 2/27/2022
 */
class PartyMemberMenu(
    private val party: Party,
    private val role: PartyRole
) : PaginatedMenu()
{
    init
    {
        updateAfterClick = true
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().apply {
            for (member in party.members)
            {
                this[size] = MemberButton(member.value)
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player) =
        "Party ${Constants.DOUBLE_ARROW_RIGHT} Management ${Constants.DOUBLE_ARROW_RIGHT} Members"

    override fun onClose(
        player: Player, manualClose: Boolean
    )
    {
        if (manualClose)
        {
            val party = PartyService
                .loadedParties[party.uniqueId]
                ?: return

            Tasks.delayed(1L) {
                PartyManageMenu(party, role).openMenu(player)
            }
        }
    }

    inner class MemberButton(
        private val member: PartyMember
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder.of(XMaterial.SKELETON_SKULL)
                .data(3)
                .name("${CC.GREEN}${member.uniqueId.username()}")
                .owner(member.uniqueId.username())
                .addToLore(
                    "${CC.SEC}Role: ${CC.PRI}${member.role.formatted}",
                    "",
                    "${CC.GREEN}Click to kick!"
                )
                .build()
        }

        override fun clicked(
            player: Player, slot: Int,
            clickType: ClickType, view: InventoryView
        )
        {
            PartyService.handlePartyKick(player, member.uniqueId)
        }
    }
}
