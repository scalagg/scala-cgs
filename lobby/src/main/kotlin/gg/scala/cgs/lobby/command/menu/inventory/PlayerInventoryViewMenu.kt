package gg.scala.cgs.lobby.command.menu.inventory

import gg.scala.cgs.common.snapshot.inventory.CgsInventorySnapshot
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.StaticItemStackButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
class PlayerInventoryViewMenu(
    private val playerInventoryMenu: PlayerInventoryMenu,
    private val snapshot: CgsInventorySnapshot
) : Menu("Recent Games ${Constants.DOUBLE_ARROW_RIGHT} Inventories ${CubedCacheUtil.fetchName(snapshot.uniqueId)}")
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = hashMapOf<Int, Button>()

        snapshot.internal.inventoryContents.forEachIndexed { index, item ->
            if (item != null)
            {
                buttons[index] = StaticItemStackButton(item)
            }
        }

        for (i in 36..44)
        {
            buttons[i] = GlassButton(15)
        }

        snapshot.internal.armorContents.reversed().forEachIndexed { index, item ->
            if (item != null)
            {
                buttons[45 + index] = StaticItemStackButton(item)
            }
        }

        buttons[49] = GlassButton(15)
        buttons[50] = EffectsButton(snapshot.internal.potionEffects)
        buttons[52] = HealthButton(snapshot.internal.health, 20.0)

        buttons[53] = FoodLevelButton(
            snapshot.internal.foodLevel.toDouble(), 20.0
        )

        return buttons
    }

    override fun onClose(
        player: Player, manualClose: Boolean
    )
    {
        if (manualClose)
        {
            Tasks.delayed(1L) {
                playerInventoryMenu.openMenu(player)
            }
        }
    }

    inner class HealthButton(
        private val health: Double,
        private val maxHealth: Double
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder(Material.INK_SACK)
                .data(1)
                .name("${CC.PRI}Health: ${CC.WHITE + String.format("%.2f", health)}/${String.format("%.2f", maxHealth)}")
                .build()
        }
    }

    inner class FoodLevelButton(
        private val foodLevel: Double,
        private val maxFoodLevel: Double
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder(Material.INK_SACK)
                .data(1)
                .name("${CC.PRI}Food Level: ${CC.WHITE + String.format("%.2f", foodLevel)}/${String.format("%.2f", maxFoodLevel)}")
                .build()
        }
    }

    inner class EffectsButton(
        private val effects: Collection<PotionEffect>
    ) : Button()
    {

        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder(Material.POTION)
                .name("${CC.PRI}Potion Effects")
                .apply {
                    if (effects.isEmpty())
                    {
                        addToLore("${CC.RED}No active potion effects.")
                    } else
                    {
                        effects.forEach {
                            addToLore(
                                "${QuickAccess.toNiceString(it.type.name.lowercase())} ${it.amplifier + 1} - ${
                                    TimeUtil.formatIntoMMSS(
                                        it.duration
                                    )
                                }"
                            )
                        }
                    }
                }
                .build()
        }
    }
}
