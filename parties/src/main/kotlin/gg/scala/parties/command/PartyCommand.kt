package gg.scala.parties.command

import gg.scala.lemon.util.QuickAccess
import gg.scala.parties.menu.PartyManageMenu
import gg.scala.parties.model.*
import gg.scala.parties.prefix
import gg.scala.parties.service.PartyService
import gg.scala.parties.stream.PartyMessageStream
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.Description
import net.evilblock.cubed.acf.annotation.HelpCommand
import net.evilblock.cubed.acf.annotation.Subcommand
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 12/17/2021
 */
@CommandAlias("party|p|parties")
object PartyCommand : BaseCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("chat")
    @Description("Send a message in party chat!")
    fun onPartyChat(player: Player, message: String)
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        val member = existing
            .findMember(player.uniqueId)!!

        val disabled = existing
            .isEnabled(PartySetting.CHAT_MUTED)

        if (disabled && !(member.role over PartyRole.MODERATOR))
        {
            throw ConditionFailedException("You do not have permission to talk while the chat is muted! Your role is: ${member.role.formatted}")
        }

        val fancy = FancyMessage()
        fancy.withMessage("$prefix${QuickAccess.coloredName(player)}${CC.WHITE}: $message")

        PartyMessageStream.pushToStream(existing, fancy)
    }

    @Subcommand("manage")
    @Description("Manage internal settings of your party!")
    fun onManage(player: Player)
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        val member = existing
            .findMember(player.uniqueId)!!

        if (!(member.role over PartyRole.MODERATOR))
        {
            throw ConditionFailedException("You do not have permission to access the party management menu! Your role is: ${member.role.formatted}")
        }

        PartyManageMenu(existing, member.role)
            .openMenu(player)
    }

    @Subcommand("disband")
    @Description("Disband your party!")
    fun onDisband(player: Player)
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        existing.gracefullyForget().join()
    }

    @Subcommand("create")
    @Description("Create a new party!")
    fun onCreate(player: Player)
    {
        val existing = PartyService
            .findPartyByUniqueId(player)

        if (existing != null)
        {
            throw ConditionFailedException("You're already in a party.")
        }

        val party = Party(
            UUID.randomUUID(),
            PartyMember(
                player.uniqueId, PartyRole.LEADER
            )
        )

        player.sendMessage("$prefix${CC.GOLD}Setting up your new party...")

        party.saveAndUpdateParty().thenRun {
            player.sendMessage("$prefix${CC.GREEN}Your new party has been setup!")
            player.sendMessage("$prefix${CC.YELLOW}Use ${CC.AQUA}/party help${CC.YELLOW} to view all party-related commands!")

            PartyService.loadedParties[party.identifier] = party
        }
    }
}
