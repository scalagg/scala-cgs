package gg.scala.parties.menu

import gg.scala.parties.model.Party
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/25/2022
 */
class PartyManageMenu(
    private val party: Party
) : Menu("Party ${Constants.DOUBLE_ARROW_RIGHT} Management")
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().apply {
            this[0]
        }
    }
}
