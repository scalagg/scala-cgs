package gg.scala.cgs.common.spectator

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.refresh
import net.evilblock.cubed.nametag.NametagHandler.reloadPlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.visibility.VisibilityHandler.update
import net.evilblock.cubed.visibility.VisibilityHandler.updateToAll
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*


/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsSpectatorHandler
{
    private val engine = CgsGameEngine.INSTANCE

    private val invisibility = PotionEffect(
        PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false
    )

    private val spectateMenu = ItemBuilder(Material.ITEM_FRAME)
        .name(CC.B_PRI + "Spectate Menu")
        .addToLore(
            CC.SEC + "See a list of players",
            CC.SEC + "that you're able to",
            CC.SEC + "teleport to and spectate."
        ).build()

    private val returnToLobby = ItemBuilder(Material.BED)
        .name(CC.B_PRI + "Return to Lobby")
        .addToLore(
            CC.SEC + "See a list of players",
            CC.SEC + "that you're able to",
            CC.SEC + "teleport to and spectate."
        ).build()

    fun setSpectator(player: Player, reason: String)
    {
        val cgsGamePlayer = CgsPlayerHandler.find(player)!!
        player.setMetadata("spectator", FixedMetadataValue(engine.plugin, true))

        Tasks.delayed(1L)
        {
            player refresh (true to GameMode.CREATIVE)
            player.addPotionEffect(invisibility)

        }
    }
}
