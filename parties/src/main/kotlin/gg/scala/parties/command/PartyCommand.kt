package gg.scala.parties.command

import gg.scala.lemon.Lemon
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.parties.menu.PartyManageMenu
import gg.scala.parties.model.*
import gg.scala.parties.prefix
import gg.scala.parties.service.PartyInviteService
import gg.scala.parties.service.PartyService
import gg.scala.parties.service.PartyService.handlePartyJoin
import gg.scala.parties.stream.PartyMessageStream
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.Description
import net.evilblock.cubed.acf.annotation.HelpCommand
import net.evilblock.cubed.acf.annotation.Private
import net.evilblock.cubed.acf.annotation.Subcommand
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Option
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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

    @Subcommand("join")
    @Description("Join a public and/or password protected party!")
    fun onJoin(
        player: Player, target: UUID,
        @net.evilblock.cubed.acf.annotation.Optional password: String?
    ): CompletableFuture<Void>
    {
        return PartyService.loadPartyOfPlayerIfAbsent(target)
            .thenAccept {
                if (it == null)
                {
                    throw ConditionFailedException("${CC.YELLOW}${target.username()}${CC.RED} does not have a party!")
                }

                if (it.status != PartyStatus.PUBLIC)
                {
                    if (it.status == PartyStatus.PROTECTED)
                    {
                        if (password == null)
                        {
                            throw ConditionFailedException("You need to provide a password to join this party!")
                        } else
                        {
                            if (it.password != password)
                            {
                                throw ConditionFailedException("You provided the incorrect password for this party!")
                            }
                        }
                    } else
                    {
                        throw ConditionFailedException("You need an invitation to join this party!")
                    }
                }

                handlePartyJoin(player, it.uniqueId)
            }
    }

    @Private
    @Subcommand("accept")
    fun onAccept(player: Player, uniqueId: UUID): CompletableFuture<Void>
    {
        val existing = PartyService
            .findPartyByUniqueId(player)

        if (existing != null)
        {
            throw ConditionFailedException("You're already in a party! Please use ${CC.BOLD}/party leave${CC.RED} to join a new one.")
        }

        return PartyInviteService.hasOutgoingInvite(
            uniqueId, player.uniqueId
        ).thenCompose {
            if (!it)
            {
                throw ConditionFailedException("You do not have an invite from this party!")
            }

            Lemon.instance.banana.useResource { jedis ->
                jedis.hdel(
                    "friends:requests:${player.uniqueId}:$uniqueId",
                    uniqueId.toString()
                )
            }

            handlePartyJoin(player, uniqueId)
        }
    }

    @Subcommand("invite")
    @Description("Invite a player to your party!")
    fun onInvite(player: Player, target: UUID): CompletableFuture<Void>
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        if (target == player.uniqueId)
        {
            throw ConditionFailedException("You cannot invite yourself to your party!")
        }

        val member = existing
            .findMember(player.uniqueId)!!

        val allInvite = existing.isEnabled(PartySetting.ALL_INVITE)

        if (!allInvite && !(member.role over PartyRole.MEMBER))
        {
            throw ConditionFailedException("You do not have permission to invite members! Your role is: ${member.role.formatted}")
        }

        if (existing.findMember(target) != null)
        {
            throw ConditionFailedException("${CC.YELLOW}${target.username()}${CC.RED} is already in your party.")
        }

        return PartyInviteService.hasOutgoingInvite(
            existing.uniqueId, target
        ).thenCompose {
            if (it)
            {
                throw ConditionFailedException("A party invite was already sent out to this user.")
            }

            handlePostOutgoingInvite(player, target, existing)
        }
    }

    private fun handlePostOutgoingInvite(
        player: Player, target: UUID, party: Party
    ): CompletableFuture<Void>
    {
        return AsyncLemonPlayer.of(target).future
            .thenCompose {
                val lemonPlayer = it ?:
                    throw ConditionFailedException("${CC.YELLOW}${target.username()}${CC.RED} has never logged on the server.")


                val disabled = lemonPlayer
                    .getSetting("party-invites-disabled")

                if (disabled)
                {
                    throw ConditionFailedException("${CC.YELLOW}${target.username()}${CC.RED} has their party invites disabled.")
                }

                internalHandlePartyInviteDispatch(player, target, party)
            }
    }

    private fun internalHandlePartyInviteDispatch(
        player: Player, target: UUID, party: Party
    ): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            val message = FancyMessage()
            message.withMessage(
                "",
                " ${CC.PRI}${player.name}${CC.SEC} sent you a party invite!",
                " ${CC.GREEN}(Click to accept)",
                ""
            )
            message.andHoverOf(
                "${CC.GREEN}Click to accept ${CC.YELLOW}${player.name}'s${CC.GREEN} party invite!"
            )
            message.andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/party accept ${party.uniqueId}"
            )

            Lemon.instance.banana.useResource {
                val requestKey = "parties:invites:$target:${party.uniqueId}"

                it.hset(
                    requestKey, party.uniqueId.toString(),
                    System.currentTimeMillis().toString()
                )

                it.expire(
                    requestKey,
                    TimeUnit.MINUTES.toSeconds(5L)
                )
            }

            QuickAccess.sendGlobalPlayerFancyMessage(
                fancyMessage = message, uuid = target
            )

            PartyMessageStream.pushToStream(party, FancyMessage().apply {
                withMessage("$prefix${CC.GREEN}${target.username()} ${CC.SEC}was invited to the party!")
            })

            player.sendMessage("$prefix${CC.GREEN}You've sent a party invite to ${CC.YELLOW}${target.username()}${CC.GREEN}!")
        }
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

            PartyService.loadedParties[party.identifier] = party
        }
    }
}
