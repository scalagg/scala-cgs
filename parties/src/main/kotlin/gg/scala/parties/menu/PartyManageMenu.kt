package gg.scala.parties.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.cookie.settings.builder.MultiOptionPlayerSettingsBuilder
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.QuickAccess
import gg.scala.parties.command.PartyCommand
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyRole
import gg.scala.parties.model.PartyStatus
import gg.scala.parties.prefix
import gg.scala.parties.stream.PartyMessageStream
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/25/2022
 */
class PartyManageMenu(
    private val party: Party,
    private val role: PartyRole
) : Menu("Party ${Constants.DOUBLE_ARROW_RIGHT} Management")
{
    init
    {
        updateAfterClick = true
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().apply {
            this[size] = MultiOptionPlayerSettingsBuilder()
                .titleOf("${CC.GREEN}Visibility")
                .materialOf(XMaterial.ENDER_EYE)
                .descriptionOf(
                    "${CC.GRAY}What visibility setting",
                    "${CC.GRAY}would you like this party",
                    "${CC.GRAY}to use?"
                )
                .orderedValuesOf(
                    "Public",
                    "Protected",
                    "Private"
                )
                .fallbackOf("Public")
                .providerOverrideOf { _, _ ->
                    Metadata(party.status.capitalized)
                }
                .valueOverrideOf {
                    val status = PartyStatus
                        .valueOf(it.uppercase())

                    party.status = status
                    party.saveAndUpdateParty().thenRun {
                        PartyMessageStream.pushToStream(
                            party, FancyMessage()
                                .withMessage("$prefix${QuickAccess.coloredName(player)} ${CC.SEC}updated party visibility to ${party.status.formatted}${CC.SEC}!")
                        )
                    }
                }
                .asButton()

            if (role == PartyRole.LEADER)
            {
                val redDye = ColorUtil.toDyeData(ChatColor.RED)

                this[8] = ItemBuilder(Material.INK_SACK)
                    .data(redDye.toShort())
                    .name("${CC.GREEN}Disband Party")
                    .addToLore(
                        "${CC.GRAY}Disband your party and",
                        "${CC.GRAY}notify party members.",
                        "",
                        "${CC.YELLOW}Click to disband party."
                    )
                    .toButton { _, _ ->
                        try
                        {
                            PartyCommand.onDisband(player)
                        } catch (exception: ConditionFailedException)
                        {
                            player.sendMessage("${CC.RED}${exception.message}")
                        }

                        // as this is an updating menu
                        Tasks.delayed(2L) {
                            player.closeInventory()
                        }
                    }
            }
        }
    }
}
