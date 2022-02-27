package gg.scala.parties.settings

import com.cryptomorin.xseries.XMaterial
import gg.scala.cookie.settings.PlayerSettings
import gg.scala.cookie.settings.builder.BooleanPlayerSettingsBuilder
import net.evilblock.cubed.menu.Button
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/27/2022
 */
object PartySettingsImpl : PlayerSettings()
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf(
            0 to BooleanPlayerSettingsBuilder()
                .titleOf("Party Invites")
                .descriptionOf(
                    "Would you like to",
                    "receive party invites?"
                )
                .materialOf(XMaterial.FIRE_CHARGE)
                .settingOf("party-invites-disabled")
                .asButton()
        )
    }

    override fun hasPermission(player: Player): Boolean = true
}
