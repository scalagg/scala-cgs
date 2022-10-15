package gg.scala.parties.command

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Default
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
@AutoRegister
@CommandAlias("togglepartyinvites|partyinvites|pi|tpi")
object TogglePartyInvitesCommand : ScalaCommand()
{
    @Default
    fun onToggleFriendRequests(player: Player)
    {
        if (Bukkit.getPluginManager().isPluginEnabled("ScBasics"))
        {
            val profile = BasicsProfileService.find(player)
                ?: throw ConditionFailedException(
                    "Sorry, your profile did not load properly."
                )

            val messagesRef = profile.settings["party_invites"]!!
            val mapped = messagesRef.map<StateSettingValue>()

            if (mapped == StateSettingValue.ENABLED)
            {
                messagesRef.value = "DISABLED"

                player.sendMessage(
                    "${CC.RED}You're now receiving party invites."
                )
            } else
            {
                messagesRef.value = "ENABLED"

                player.sendMessage(
                    "${CC.GREEN}You're no longer receiving party invites."
                )
            }

            profile.save()
            return
        }

        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)
        val partyInvites = lemonPlayer.getSetting("party-invites-disabled")

        if (partyInvites)
        {
            lemonPlayer remove "party-invites-disabled"
            player.sendMessage("${CC.GREEN}You've enabled party invitations.")
        } else
        {
            lemonPlayer.updateOrAddMetadata("party-invites-disabled", Metadata(true))
            player.sendMessage("${CC.RED}You've disabled party invitations.")
        }
    }
}
