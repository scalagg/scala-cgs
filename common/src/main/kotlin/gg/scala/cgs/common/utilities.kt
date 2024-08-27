package gg.scala.cgs.common

import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.kyori.adventure.audience.Audience
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*
import java.util.logging.Level

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
val alive: List<Player>
    get() = Players.all()
        .filterNot {
            it.hasMetadata("spectator")
        }

val startMessage by lazy {
    FancyMessage().apply {
        withMessage(
            "${CC.GRAY}${CC.STRIKE_THROUGH}--------------------------------------",
            "${CC.RED}This game is currently in BETA.",
            "${CC.RED}Please report any bugs to our Discord.",
            "${CC.GRAY}${CC.STRIKE_THROUGH}--------------------------------------",
        )

        andHoverOf(
            "${CC.RED}Click to join our Discord server."
        )

        andCommandOf(
            ClickEvent.Action.OPEN_URL,
            LemonConstants.DISCORD_LINK
        )
    }
}

fun Exception.printStackTraceV2(
    rootedFrom: String = "N/A"
)
{
    CgsGameEngine.INSTANCE.plugin.logger.log(
        Level.SEVERE, rootedFrom, this
    )
}

infix fun Player.refresh(
    information: Pair<Boolean, GameMode>
)
{
    health = maxHealth
    foodLevel = 20
    saturation = 12.8f
    maximumNoDamageTicks = 20
    fireTicks = 0
    fallDistance = 0.0f
    level = 0
    exp = 0.0f
    walkSpeed = 0.2f
    inventory.heldItemSlot = 0

    if (information.first)
    {
        allowFlight = true
        isFlying = true
    } else
    {
        isFlying = false
        allowFlight = false
    }

    inventory.clear()
    inventory.armorContents = null

    closeInventory()
    updateInventory()

    gameMode = information.second

    for (potionEffect in activePotionEffects)
    {
        removePotionEffect(potionEffect.type)
    }
}

val uniqueIdGlobal = UUID.randomUUID()

infix fun Player.adventure(lambda: (Audience) -> Unit)
{
    CgsGameEngine.INSTANCE.audience
        .player(this).apply(lambda)
}

infix fun Player.giveCoins(
    information: Pair<Int, String>
)
{
    if (CgsGameEngine.INSTANCE.gameInfo.awards.awardCoins)
    {
        CgsGameEngine.INSTANCE.platform
            .giveCoins(
                this, information, true
            )
    }
}

fun respawnPlayer(event: PlayerDeathEvent)
{
    Tasks.delayed(1L)
    {
        event.entity.spigot().respawn()
    }
}
