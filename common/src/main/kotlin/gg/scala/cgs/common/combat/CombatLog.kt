package gg.scala.cgs.common.combat

import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

/**
 * @author GrowlyX
 * @since 5/10/2023
 */
data class CombatLog(
    val player: UUID,
    val entity: Zombie,
    val timestamp: Instant,
    val drops: List<ItemStack>,
    val experience: Int
)
