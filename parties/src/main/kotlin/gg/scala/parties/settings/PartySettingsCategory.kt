package gg.scala.parties.settings

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.SettingCategory
import gg.scala.basics.plugin.settings.SettingContainer.buildEntry
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.parties.PartySpigotPlugin
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/27/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("ScBasics")
object PartySettingsCategory : SettingCategory
{
    @Inject
    lateinit var plugin: PartySpigotPlugin

    @Configure
    fun configure()
    {
        plugin.commandManager
            .registerCommand(object : ScalaCommand()
            {
                @CommandAlias(
                    "togglepartyinvites|partyinvites|pi|tpi"
                )
                fun onTogglePartyInvites(player: ScalaPlayer)
                {
                    val profile = BasicsProfileService.find(player.bukkit())
                        ?: throw ConditionFailedException(
                            "Sorry, your profile did not load properly."
                        )

                    val messagesRef = profile.settings["party_invites"]!!
                    val mapped = messagesRef.map<StateSettingValue>()

                    if (mapped == StateSettingValue.ENABLED)
                    {
                        messagesRef.value = "DISABLED"

                        player.sendMessage(
                            "${CC.RED}You're no longer receiving party invites."
                        )
                    } else
                    {
                        messagesRef.value = "ENABLED"

                        player.sendMessage(
                            "${CC.GREEN}You're now receiving party invites."
                        )
                    }

                    profile.save()
                }
            })
    }

    override val description = listOf(
        "Party privacy, spam, and",
        "other options."
    )
    override val displayName = "Parties"

    override val items = listOf(
        buildEntry {
            id = "party_invites"
            displayName = "Party invites"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Allows you to disable"
            description += "party invites through"
            description += "/party invite."

            item = ItemBuilder.of(Material.FIREBALL)
        }
    )

    override fun display(player: Player) = true
}
