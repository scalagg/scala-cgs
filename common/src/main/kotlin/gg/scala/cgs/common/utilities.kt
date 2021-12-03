package gg.scala.cgs.common

import net.kyori.adventure.audience.Audience
import org.bukkit.GameMode
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
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

    isFlying = information.first
    allowFlight = information.first

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
