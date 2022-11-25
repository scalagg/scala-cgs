package gg.scala.parties.menu

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 9/12/2021
 */
class MultiOptionPlayerSettingsBuilder {

    var title = "N/A"
    var material = Material.AIR

    var description = listOf<String>()
    var values = listOf<String>()

    var metaDataSetting = "N/A"

    var autoAddData = true

    private var valueHandleOverride: ((String) -> Unit)? = null
    private var valueProviderOverride: (Player, String) -> String? = { lemonPlayer, string -> string }

    var handle: ((Player) -> Unit)? = null

    lateinit var fallback: String

    fun titleOf(title: String): MultiOptionPlayerSettingsBuilder {
        this.title = title
        return this
    }

    fun materialOf(material: XMaterial): MultiOptionPlayerSettingsBuilder {
        this.material = material.parseMaterial()!!
        return this
    }

    fun fallbackOf(fallback: String): MultiOptionPlayerSettingsBuilder {
        this.fallback = fallback
        return this
    }

    fun afterHandleOf(handle: (Player) -> Unit): MultiOptionPlayerSettingsBuilder {
        this.handle = handle
        return this
    }

    fun valueOverrideOf(handle: (String) -> Unit): MultiOptionPlayerSettingsBuilder {
        this.valueHandleOverride = handle
        return this
    }

    fun providerOverrideOf(handle: (Player, String) -> String?): MultiOptionPlayerSettingsBuilder {
        this.valueProviderOverride = handle
        return this
    }

    fun descriptionOf(vararg description: String): MultiOptionPlayerSettingsBuilder {
        this.description = listOf(*description)
        return this
    }

    fun orderedValuesOf(vararg values: String): MultiOptionPlayerSettingsBuilder {
        this.values = listOf(*values)
        return this
    }

    fun settingOf(metaDataSetting: String): MultiOptionPlayerSettingsBuilder {
        this.metaDataSetting = metaDataSetting
        return this
    }

    fun asButton(): Button {
        return object : Button() {

            override fun getButtonItem(player: Player): ItemStack {
                val formattedLore = mutableListOf<String>()
                val currentValue = valueProviderOverride.invoke(player, metaDataSetting)

                description.forEach {
                    formattedLore.add("${CC.GRAY}$it")
                }

                formattedLore.add("")

                values.forEach {
                    formattedLore.add("${if (currentValue == it) CC.B_GREEN else CC.B_GRAY}■ ${CC.WHITE}${it.capitalize()}")
                }

                formattedLore.add("")
                formattedLore.add("${CC.YELLOW}Click to scroll through.")

                return ItemBuilder(material)
                    .name("${CC.GREEN}$title")
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .setLore(formattedLore).build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                val currentValue = valueProviderOverride.invoke(player, metaDataSetting)

                val amount = if (clickType.isRightClick) -1 else 1
                val index = values.indexOf(currentValue) + amount

                val newVal = if (index >= values.size) {
                    values[0]
                } else if (index < 0) {
                    values[values.size - 1]
                } else {
                    values[index]
                }

                valueHandleOverride!!.invoke(newVal)
                handle?.invoke(player)
            }
        }
    }
}
