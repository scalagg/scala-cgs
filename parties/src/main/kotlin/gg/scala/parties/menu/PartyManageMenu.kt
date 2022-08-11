package gg.scala.parties.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.aware.message.AwareMessage
import gg.scala.cookie.settings.builder.MultiOptionPlayerSettingsBuilder
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.QuickAccess
import gg.scala.parties.command.PartyCommand
import gg.scala.parties.menu.members.PartyMemberMenu
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyRole
import gg.scala.parties.model.PartySetting
import gg.scala.parties.model.PartyStatus
import gg.scala.parties.prefix
import gg.scala.parties.receiver.PartyReceiverHandler
import gg.scala.parties.stream.PartyMessageStream
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.parties.service.PartyService
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.*
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

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

    override fun size(buttons: Map<Int, Button>) = 27
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().apply {
            this[10] = MultiOptionPlayerSettingsBuilder()
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
                .fallbackOf("Private")
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

            this[11] = MultiOptionPlayerSettingsBuilder()
                .titleOf("${CC.GREEN}All Invite")
                .materialOf(XMaterial.FIRE_CHARGE)
                .descriptionOf(
                    "${CC.GRAY}Do you want all party",
                    "${CC.GRAY}members to be able to",
                    "${CC.GRAY}invite players?"
                )
                .orderedValuesOf(
                    "Enabled",
                    "Disabled"
                )
                .fallbackOf("Disabled")
                .providerOverrideOf { _, _ ->
                    if (party.isEnabled(PartySetting.ALL_INVITE))
                        Metadata("Enabled")
                    else
                        Metadata("Disabled")
                }
                .valueOverrideOf {
                    party.update(PartySetting.ALL_INVITE, it == "Enabled")
                    party.saveAndUpdateParty().thenRun {
                        PartyMessageStream.pushToStream(
                            party, FancyMessage()
                                .withMessage(
                                    "$prefix${QuickAccess.coloredName(player)} ${
                                        if (it == "Enabled")
                                            "${CC.GREEN}enabled All-Invite!" else
                                            "${CC.RED}disabled All-Invite."
                                    }"
                                )
                        )
                    }
                }
                .asButton()

            this[12] = MultiOptionPlayerSettingsBuilder()
                .titleOf("${CC.GREEN}Chat Muted")
                .materialOf(XMaterial.BLAZE_POWDER)
                .descriptionOf(
                    "${CC.GRAY}Do you want chat to",
                    "${CC.GRAY}be muted?",
                )
                .orderedValuesOf(
                    "Enabled",
                    "Disabled"
                )
                .fallbackOf("Disabled")
                .providerOverrideOf { _, _ ->
                    if (party.isEnabled(PartySetting.CHAT_MUTED))
                        Metadata("Enabled")
                    else
                        Metadata("Disabled")
                }
                .valueOverrideOf {
                    party.update(PartySetting.CHAT_MUTED, it == "Enabled")
                    party.saveAndUpdateParty().thenRun {
                        PartyMessageStream.pushToStream(
                            party, FancyMessage()
                                .withMessage(
                                    "$prefix${QuickAccess.coloredName(player)} ${
                                        if (it == "Enabled")
                                            "${CC.RED}disabled party chat!" else
                                            "${CC.GREEN}enabled party chat."
                                    }"
                                )
                        )
                    }
                }
                .asButton()

            this[13] = ItemBuilder(Material.IRON_DOOR)
                .name("${CC.GREEN}Party Limit")
                .addToLore(
                    "${CC.GRAY}limit the amount of players",
                    "${CC.GRAY}that are able to join your party",
                    "",
                    "${CC.YELLOW}Right-Click to decrease the limit by 1",
                    "${CC.YELLOW}Left-Click to increase the limit by 1",
                )
                .toButton { _, type ->
                    if (!player.hasPermission("party.update.limit"))
                    {
                        player.sendMessage("${CC.RED}You do not have permission to update your party's player limit.")
                        return@toButton
                    }

                    if (party.limit in 2..100)
                    {
                        player.sendMessage("${CC.RED}You have gone out of the party member limit bounds!")
                        return@toButton
                    }

                    when (type) {
                        ClickType.RIGHT -> party.limit--
                        ClickType.LEFT -> party.limit++
                        else -> {}
                    }
                    party.saveAndUpdateParty()
                }


            this[1] = ItemBuilder(Material.SIGN)
                .name("${CC.GREEN}Party Password")
                .addToLore(
                    "${CC.GRAY}Update your party password",
                    "${CC.GRAY}which is effective during",
                    "${CC.GRAY}party protected mode.",
                    ""
                )
                .apply {
                    if (party.status == PartyStatus.PROTECTED)
                    {
                        addToLore("${CC.YELLOW}Right-Click to update password.")
                        addToLore("${CC.YELLOW}Left-Click to view password.")
                    } else
                    {
                        addToLore("${CC.RED}The party must be in protected mode!")
                    }
                }
                .toButton { _, type ->
                    if (party.status != PartyStatus.PROTECTED)
                    {
                        player.sendMessage("${CC.RED}Your party is not in protected mode!")
                        return@toButton
                    }

                    if (type!!.isRightClick)
                    {
                        player.closeInventory()

                        InputPrompt().apply {
                            this.withText("${CC.GREEN}Please enter a new server password!")
                            this.acceptInput { _, password ->
                                val fancyMessage = FancyMessage()
                                party.password = password

                                fancyMessage.withMessage(
                                    "${CC.SEC}The password is now: ${CC.WHITE}${
                                        "*".repeat(party.password.length)
                                    } ${CC.I_GRAY}(hover over to view)"
                                )

                                fancyMessage.andHoverOf(party.password)

                                party.saveAndUpdateParty().thenRun {
                                    fancyMessage.sendToPlayer(player)
                                }
                            }

                            this.start(player)
                        }
                    } else
                    {
                        if (party.password.isEmpty())
                        {
                            player.sendMessage("${CC.RED}Your party does not have a password!")
                            return@toButton
                        }

                        val fancyMessage = FancyMessage()
                        fancyMessage.withMessage(
                            "$prefix${CC.SEC}The password is: ${CC.WHITE}${
                                "*".repeat(party.password.length)
                            } ${CC.I_GRAY}(hover over to view)"
                        )

                        fancyMessage.andHoverOf(party.password)
                        fancyMessage.sendToPlayer(player)
                    }
                }

            this[2] = ItemBuilder(Material.REDSTONE_COMPARATOR)
                .name("${CC.RED}Reset Password")
                .addToLore(
                    "${CC.GRAY}Set your party password",
                    "${CC.GRAY}back to its default.",
                    "",
                    "${CC.YELLOW}Click to reset password."
                )
                .toButton { _, _ ->
                    if (party.password.isEmpty())
                    {
                        player.sendMessage("${CC.RED}Your party does not have a password!")
                        return@toButton
                    }

                    party.password = ""
                    party.saveAndUpdateParty().thenRun {
                        player.sendMessage("${CC.GREEN}Your party's password has been reset.")
                    }
                }

            this[18] = ItemBuilder
                .copyOf(
                    PaginatedMenu.PLACEHOLDER
                        .getButtonItem(player)
                )
                .data(1)
                .name("${CC.GOLD}Private Games")
                .toButton()

            this[19] = ItemBuilder
                .of(Material.BOOK_AND_QUILL)
                .name("${CC.GREEN}Create a Server")
                .addToLore(
                    "${CC.GRAY}Click to go through the",
                    "${CC.GRAY}private game creation",
                    "${CC.GRAY}process.",
                    "",
                    "${CC.RED}Private games are disabled."
                )
                .toButton()

            this[9] = ItemBuilder
                .copyOf(
                    PaginatedMenu.PLACEHOLDER
                        .getButtonItem(player)
                )
                .data(5)
                .name("${CC.GREEN}Settings")
                .toButton()

            this[0] = ItemBuilder
                .copyOf(
                    PaginatedMenu.PLACEHOLDER
                        .getButtonItem(player)
                )
                .data(3)
                .name("${CC.D_AQUA}Password")
                .toButton()

            if (role == PartyRole.LEADER)
            {
                val redDye = ColorUtil.toDyeData(ChatColor.RED)

                listOf(7, 16, 25).forEach {
                    this[it] = PaginatedMenu.PLACEHOLDER
                }

                this[8] = ItemBuilder(Material.FISHING_ROD)
                    .name("${CC.GREEN}Manage Players")
                    .addToLore(
                        "${CC.GRAY}Manage your party members!",
                        "",
                        "${CC.YELLOW}Click to open menu!"
                    )
                    .toButton { _, _ ->
                        PartyMemberMenu(party, role).openMenu(player)
                    }

                this[8 + 9] = ItemBuilder(Material.BEACON)
                    .name("${CC.GREEN}Warp your party")
                    .addToLore(
                        "${CC.GRAY}Send all your party members",
                        "${CC.GRAY}to your current server!",
                        "",
                        "${CC.YELLOW}Click to warp members!"
                    )
                    .toButton { _, _ ->
                        AwareMessage.of(
                            "party-warp", PartyReceiverHandler.aware,
                            "uniqueId" to party.uniqueId.toString(),
                            "server" to Lemon.instance.settings.id
                        ).publish()

                        player.closeInventory()
                        player.sendMessage("$prefix${CC.GREEN}You've warped party members to your server!")
                    }

                this[8 + 18] = ItemBuilder(Material.INK_SACK)
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
