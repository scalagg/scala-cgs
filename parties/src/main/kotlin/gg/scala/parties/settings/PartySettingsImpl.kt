package gg.scala.parties.settings

import com.cryptomorin.xseries.XMaterial
import gg.scala.cookie.settings.PlayerSettings
import gg.scala.cookie.settings.annotation.RegisterPlayerSettings
import gg.scala.cookie.settings.builder.BooleanPlayerSettingsBuilder
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/27/2022
 */
@RegisterPlayerSettings
object PartySettingsImpl : PlayerSettings()
{
    override fun getButtons(player: Player) =
        mutableMapOf(
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

    override fun identifier() = "Parties"
    override fun description() = listOf(
        "${CC.GRAY}Party privacy, spam, and other options."
    )

    override fun hasPermission(player: Player) = true
}
