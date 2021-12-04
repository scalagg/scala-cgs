package gg.scala.cgs.common

import gg.scala.grape.GrapeSpigotPlugin
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.nms.MinecraftReflection
import net.kyori.adventure.audience.Audience
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import java.lang.reflect.Method


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
    val grapePlayer = GrapeSpigotPlugin.getInstance()
        .playerHandler.getByPlayer(this)

    if (grapePlayer != null)
    {
        grapePlayer.coins += information.first
        sendMessage("${CC.GOLD}+${information.first} coins (${information.second})!")
    }
}

val ENTITY_PLAYER = MinecraftReflection.getNMSClass("EntityPlayer")
val MINECRAFT_SERVER = MinecraftReflection.getMinecraftServer()

val PLAYER_LIST: Any = MINECRAFT_SERVER.javaClass
    .getDeclaredMethod("getPlayerList")
    .invoke(MINECRAFT_SERVER)

val WORLD_MOVEMENT: Method = PLAYER_LIST.javaClass.getMethod(
    "moveToWorld", ENTITY_PLAYER,
    Int::class.javaPrimitiveType,
    Boolean::class.javaPrimitiveType
)

fun respawnPlayer(event: PlayerDeathEvent)
{
    Tasks.delayed(2L)
    {
        WORLD_MOVEMENT.invoke(
            PLAYER_LIST,
            MinecraftReflection.getHandle(event.entity),
            0, false
        )
    }
}
