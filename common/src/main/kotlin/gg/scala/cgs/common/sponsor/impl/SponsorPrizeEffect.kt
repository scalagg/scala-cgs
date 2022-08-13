package gg.scala.cgs.common.sponsor.impl

import gg.scala.cgs.common.sponsor.SponsorPrize
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author AgentRKID
 * @since 8/12/2022
 */
class SponsorPrizeEffect(var effect: EffectType, var amp: Int, var duration: Int, var material: Material, name: String, cost: Int) : SponsorPrize(name, cost)
{
    override fun canApply(player: Player): Boolean
    {
        return player.hasPotionEffect(effect.type)
    }

    override fun apply(player: Player)
    {
        player.addPotionEffect(PotionEffect(effect.type, duration, amp))
    }

    override fun toItemStack(): ItemStack
    {
        return ItemBuilder.of(material).build()
    }

    enum class EffectType(var type: PotionEffectType)
    {
        SPEED(PotionEffectType.SPEED),
        REGEN(PotionEffectType.REGENERATION)
    }
}