package gg.scala.parties.command

import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyMember
import gg.scala.parties.model.PartyRole
import gg.scala.parties.prefix
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.Description
import net.evilblock.cubed.acf.annotation.HelpCommand
import net.evilblock.cubed.acf.annotation.Subcommand
import net.evilblock.cubed.util.CC
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

    @Subcommand("disband")
    fun onDisband(player: Player)
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        existing.gracefullyForget()
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
        }
    }
}
