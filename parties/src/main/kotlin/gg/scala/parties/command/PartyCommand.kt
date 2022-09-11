package gg.scala.parties.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.parties.event.PartySetupEvent
import gg.scala.parties.menu.PartyManageMenu
import gg.scala.parties.model.*
import gg.scala.parties.prefix
import gg.scala.parties.service.PartyInviteService
import gg.scala.parties.service.PartyService
import gg.scala.parties.service.PartyService.handlePartyJoin
import gg.scala.parties.stream.PartyMessageStream
import io.lettuce.core.api.StatefulRedisConnection
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.parties.event.PartyLeaveEvent
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 12/17/2021
 */
@AutoRegister
@CommandAlias("party|p|parties")
object PartyCommand : ScalaCommand()
{
    val connection: StatefulRedisConnection<String, String>
            by lazy {
                val internal = Lemon
                    .instance.aware.internal()

                internal.connect()
            }

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("role")
    @Description("Set a user's role in your party.")
    fun onRole(
        player: Player,
        target: UUID,
        role: PartyRole
    )
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        if (player.uniqueId != existing.leader.uniqueId)
        {
            throw ConditionFailedException("You must be the leader of your party to promote others.")
        }

        if (target == player.uniqueId)
        {
            throw ConditionFailedException("You're unable to modify your own party role.")
        }

        if (role == PartyRole.LEADER)
        {
            throw ConditionFailedException("You're unable to set another player's role to leader.")
        }

        val member = existing
            .findMember(target)
            ?: throw ConditionFailedException(
                "The player you specified is not a party member."
            )

        member.role = role

        val fancy = FancyMessage()
        fancy.withMessage("$prefix${CC.GREEN}${target.username()}'s ${CC.YELLOW}role has been set to ${role.formatted}${CC.YELLOW}.")

        PartyMessageStream.pushToStream(existing, fancy)
    }

    @Subcommand("info|view|show")
    @Description("View your party details!")
    fun onInfo(
        player: Player,
        @Optional target: UUID?
    ): CompletableFuture<Void>
    {
        return PartyService
            .loadPartyOfPlayerIfAbsent(
                target ?: player.uniqueId
            )
            .thenApply {
                val username = CubedCacheUtil
                    .fetchName(
                        target ?: player.uniqueId
                    )

                if (it == null)
                {
                    throw ConditionFailedException(
                        if (target == null)
                            "You're not in a party." else "${CC.YELLOW}${
                            CubedCacheUtil.fetchName(target)
                        }${CC.RED} is not in a party."
                    )
                }

                player.sendMessage("")
                player.sendMessage("  ${CC.B_PRI}$username's Party:")
                player.sendMessage("  ${CC.I_GRAY}${it.members.size} members.")
                player.sendMessage("")
                player.sendMessage("  ${CC.BL_PURPLE}⚫ ${CC.L_PURPLE}Status: ${it.status.formatted}")
                player.sendMessage("")

                val moderators = it.members
                    .filter { member ->
                        member.value.role == PartyRole.MODERATOR
                    }

                if (moderators.isNotEmpty())
                {
                    player.sendMessage("  ${CC.BD_GREEN}⚫ ${CC.D_GREEN}Moderators:")

                    for (member in moderators)
                    {
                        player.sendMessage("   - ${member.value.uniqueId.username()}")
                    }

                    player.sendMessage("")
                }

                return@thenApply null
            }
    }

    @Subcommand("leave")
    @Description("Leave your current party!")
    fun onLeave(player: Player): CompletableFuture<Void>
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        if (player.uniqueId == existing.leader.uniqueId)
        {
            throw ConditionFailedException("You cannot leave your own party! Use ${CC.BOLD}/party disband${CC.RED} to disband your party.")
        }

        return PartyService.handlePartyLeave(player.uniqueId)
    }

    @Subcommand("join")
    @CommandCompletion("@players")
    @Description("Join a public and/or password protected party!")
    fun onJoin(
        player: Player, target: UUID,
        @Optional password: String?
    ): CompletableFuture<Void>
    {
        return PartyService.loadPartyOfPlayerIfAbsent(target)
            .thenAccept {
                if (it == null)
                {
                    throw ConditionFailedException("${CC.YELLOW}${target.username()}${CC.RED} does not have a party!")
                }

                if (it.includedMembers().size >= it.limit)
                {
                    throw ConditionFailedException("You cannot join that party as its full!")
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

            val requestKey =
                "parties:invites:${player.uniqueId}:$uniqueId"

            connection.sync().hdel(
                requestKey, uniqueId.toString()
            )

            handlePartyJoin(player, uniqueId)
        }
    }

    @Subcommand("invite")
    @CommandCompletion("@players")
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

        if (existing.includedMembers().size >= existing.limit)
        {
            throw ConditionFailedException("You cannot invite anymore people to your party as its full.")
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

    @Subcommand("kick|remove")
    @CommandCompletion("@players")
    @Description("Kick a player from your party!")
    fun onKick(player: Player, target: UUID): CompletableFuture<Void>
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        val selfMember = existing
            .findMember(player.uniqueId)!!

        if (!(selfMember.role over PartyRole.MODERATOR))
        {
            throw ConditionFailedException("You do not have permission to kick members! Your role is: ${selfMember.role.formatted}")
        }

        val targetMember = existing
            .findMember(target)
            ?: throw ConditionFailedException(
                "${CC.YELLOW}${target.username()}${CC.RED} is not in your party."
            )

        if (target == existing.leader.uniqueId)
        {
            throw ConditionFailedException("You do not have permission to kick the party leader!")
        }

        if (target == player.uniqueId)
        {
            throw ConditionFailedException("You cannot kick yourself from your party!")
        }

        existing.members.remove(
            targetMember.uniqueId
        )

        return existing.saveAndUpdateParty()
            .thenRun {
                PartyLeaveEvent(
                    existing, targetMember, true
                ).call()

                existing.sendMessage(
                    FancyMessage().apply {
                        withMessage("$prefix${CC.GREEN}${target.username()}${CC.YELLOW} was kicked from the party.")
                    }
                )
            }
    }

    private fun handlePostOutgoingInvite(
        player: Player, target: UUID, party: Party
    ): CompletableFuture<Void>
    {
        return AsyncLemonPlayer.of(target, true).future
            .thenCompose {
                if (it.isEmpty())
                {
                    throw ConditionFailedException("${CC.YELLOW}${target.username()}${CC.RED} has never logged on the server.")
                }

                val disabled = it[0]
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

            val requestKey = "parties:invites:$target:${party.uniqueId}"

            connection.sync().hset(
                requestKey, party.uniqueId.toString(),
                System.currentTimeMillis().toString()
            )

            connection.sync().expire(
                requestKey,
                TimeUnit.MINUTES.toSeconds(5L)
            )

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
        fancy.withMessage(
            "$prefix ${CC.PRI}[${
                ServerSync.getLocalGameServer().id
            }] ${QuickAccess.coloredName(player, ignoreMinequest = true)}${CC.GRAY}: ${CC.WHITE}$message"
        )

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
    fun onDisband(player: Player): CompletableFuture<Void>
    {
        val existing = PartyService
            .findPartyByUniqueId(player)
            ?: throw ConditionFailedException("You're not in a party.")

        return existing.gracefullyForget()
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
            PartySetupEvent(party)
                .call()

            player.sendMessage("$prefix${CC.GREEN}Your new party has been setup!")
            player.sendMessage("$prefix${CC.YELLOW}Use ${CC.AQUA}/party help${CC.YELLOW} to view all party-related commands!")

            PartyService.loadedParties[party.identifier] = party
        }
    }
}
