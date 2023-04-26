package gg.scala.cgs.common.player.handler

import com.cryptomorin.xseries.XMaterial
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.adventure
import gg.scala.cgs.common.refresh
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.nms.MinecraftProtocol
import net.evilblock.cubed.visibility.VisibilityHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsSpectatorHandler
{
    private val engine = CgsGameEngine.INSTANCE

    private val spectateMenu = ItemBuilder(Material.ITEM_FRAME)
        .name(CC.GOLD + "Spectate Menu ${CC.GRAY}(Right Click)")
        .addToLore(
            CC.GRAY + "See a list of players",
            CC.GRAY + "that you're able to",
            CC.GRAY + "teleport to and spectate."
        ).build()

    private val returnToLobby = ItemBuilder(XMaterial.RED_DYE)
        .name(CC.RED + "Return to Lobby ${CC.GRAY}(Right Click)")
        .addToLore(
            CC.GRAY + "Return to the ${engine.gameInfo.fancyNameRender} lobby."
        ).build()

    private val spectateTitle = Title.title(
        Component.text("YOU DIED")
            .decorate(TextDecoration.BOLD)
            .color(TextColor.fromHexString("#FF4F4B")),
        Component.text("Better luck next time!")
            .color(TextColor.fromHexString("#FFFFFF"))
    )

    fun removeSpectator(
        player: Player
    )
    {
        if (player.hasMetadata("spectator"))
        {
            player.removeMetadata("spectator", engine.plugin)
        }

        player refresh (false to GameMode.SURVIVAL)
        removeGhost(player)

        player.inventory.clear()
        player.updateInventory()

        QuickAccess.reloadPlayer(player.uniqueId, false)

        val cgsSpectatorRemove = CgsGameEngine
            .CgsGameSpectatorRemoveEvent(player)

        cgsSpectatorRemove.callNow()
    }

    private const val GHOST_VIS = "ghost"

    fun addGhost(player: Player)
    {
        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.INVISIBILITY, 10000, 1, false, false
            )
        )

        if (MinecraftProtocol.getPlayerVersion(player) > 5)
        {
            var scoreboard = player.scoreboard
            if (scoreboard == null)
            {
                player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
                scoreboard = player.scoreboard
            }
            var ghostVisibility = scoreboard!!.getTeam(GHOST_VIS)
            if (ghostVisibility == null)
            {
                ghostVisibility = scoreboard
                    .registerNewTeam(GHOST_VIS)
                ghostVisibility.setAllowFriendlyFire(true)
                ghostVisibility.setCanSeeFriendlyInvisibles(true)
            }
            ghostVisibility!!.addPlayer(player)
        }
    }

    fun removeGhost(player: Player)
    {
        player.removePotionEffect(PotionEffectType.INVISIBILITY)

        if (MinecraftProtocol.getPlayerVersion(player) > 5)
        {
            val scoreboard = player.scoreboard ?: return
            val ghostVisibility = scoreboard.getTeam(GHOST_VIS) ?: return
            ghostVisibility.removePlayer(player)
        }
    }

    @JvmOverloads
    fun setSpectator(
        player: Player, sendTitle: Boolean = true,
        teleportLocation: Location = engine.gameArena!!.getSpectatorLocation()
    )
    {
        player.setMetadata("spectator", FixedMetadataValue(engine.plugin, true))

        player.health = player.maxHealth
        player.foodLevel = 20
        player.saturation = 20f

        Tasks.delayed(1L)
        {
            player refresh (true to GameMode.CREATIVE)
            addGhost(player)

            player.sendMessage("${CC.D_RED}✘ ${CC.RED}You've been made a spectator.")

            if (sendTitle)
            {
                player adventure {
                    it.showTitle(spectateTitle)
                }
            }

            player.teleport(teleportLocation)
            player.playerListName = "${CC.GRAY}${player.name}"

            player.inventory.setItem(0, spectateMenu)
            player.inventory.setItem(8, returnToLobby)

            player.updateInventory()

            VisibilityHandler.update(player)
            NametagHandler.reloadPlayer(player)

            val cgsSpectatorAdd = CgsGameEngine
                .CgsGameSpectatorAddEvent(player)

            cgsSpectatorAdd.callNow()
        }
    }
}
