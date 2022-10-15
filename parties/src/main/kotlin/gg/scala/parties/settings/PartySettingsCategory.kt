package gg.scala.parties.settings

import com.cryptomorin.xseries.XMaterial
import gg.scala.basics.plugin.conversation.settings.MessagingSettingValue
import gg.scala.basics.plugin.settings.SettingCategory
import gg.scala.basics.plugin.settings.SettingContainer.buildEntry
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
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
object PartySettingsCategory : SettingCategory
{
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
