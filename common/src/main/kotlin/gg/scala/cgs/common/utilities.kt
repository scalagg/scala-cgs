package gg.scala.cgs.common

import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.kyori.adventure.audience.Audience
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
val startMessage by lazy {
    FancyMessage().apply {
        withMessage(
            "",
            " ${CC.B_PRI}${
                CgsGameEngine.INSTANCE.gameInfo.fancyNameRender
            } is currently in BETA!",
            " ${CC.SEC}Remember, there may be bugs/incomplete features!",
            "",
            " ${CC.SEC}If you think you have found a bug, report it at:",
            " ${CC.WHITE}${Lemon.instance.lemonWebData.discord}",
            ""
        )

        andHoverOf(
            "${CC.YELLOW}Click to join our discord server!"
        )

        andCommandOf(
            ClickEvent.Action.OPEN_URL,
            Lemon.instance.lemonWebData.discord
        )
    }
}

fun Exception.printStackTraceV2(
    rootedFrom: String = "N/A"
)
{
    CgsGameEngine.INSTANCE.plugin.logger.severe {
        """
            An exception was thrown from $rootedFrom!
              Compressed: $message
              Localized: $localizedMessage
              
            Complete stack trace:
            ${stackTraceToString()}
        """.trimIndent()
    }
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
                this, information.first
            )

        sendMessage("${CC.GOLD}+${information.first} coins (${information.second})!")
    }
}

fun respawnPlayer(event: PlayerDeathEvent)
{
    Tasks.delayed(1L)
    {
        event.entity.spigot().respawn()
    }
}
