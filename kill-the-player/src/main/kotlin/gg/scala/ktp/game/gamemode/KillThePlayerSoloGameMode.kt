package gg.scala.ktp.game.gamemode

import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.ktp.game.arena.KillThePlayerNv6Arena
import net.evilblock.cubed.util.CC
import org.bukkit.Material

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerSoloGameMode : CgsGameMode
{
    override fun getId() = "solo"
    override fun getName() = "Solo"

    override fun getMaterial() = Pair(Material.DIAMOND, 0)

    override fun getDescription() = "${CC.GRAY}Play an FFA match of Kill the Player!"

    override fun getArenas() = listOf(
        KillThePlayerNv6Arena
    )

    override fun getTeamSize() = 1
    override fun getMaxTeams() = 4
}
