package gg.scala.cgs.common.handler

import net.evilblock.cubed.util.CC;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.apache.commons.lang.WordUtils;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


/**
 * @author GrowlyX
 * @since 12/2/2021
 */
class CgsDeathHandler
{

    fun getDeathMessage(entity: LivingEntity, killer: Entity?): String
    {
        var output = getEntityName(entity) + CC.SEC

        if (entity.getLastDamageCause() != null)
        {
            val killerName = getEntityName(killer)

            when (entity.getLastDamageCause().getCause())
            {
                BLOCK_EXPLOSION, ENTITY_EXPLOSION -> output += " was blown to smithereens"
                CONTACT -> output += " was pricked to death"
                DROWNING -> output += if (killer != null)
                {
                    " drowned while fighting $killerName"
                } else
                {
                    " drowned"
                }
                ENTITY_ATTACK -> if (killer != null)
                {
                    output += " was slain by $killerName"
                    if (killer is Player)
                    {
                        val hand: ItemStack? = (killer as Player).itemInHand
                        val handString =
                            if (hand == null) "their fists" else if (hand.hasItemMeta() && hand.getItemMeta()
                                    .hasDisplayName()
                            ) hand.getItemMeta().getDisplayName() else WordUtils.capitalizeFully(
                                hand.getType().name().replace("_", " ")
                            )
                        output += CC.SEC + " using " + CC.RED + handString
                    }
                }
                FALL -> output += if (killer != null)
                {
                    " hit the ground too hard thanks to $killerName"
                } else
                {
                    " hit the ground too hard"
                }
                FALLING_BLOCK ->
                {
                }
                FIRE_TICK, FIRE -> output += if (killer != null)
                {
                    " burned to death thanks to $killerName"
                } else
                {
                    " burned to death"
                }
                LAVA -> output += if (killer != null)
                {
                    " tried to swim in lava while fighting $killerName"
                } else
                {
                    " tried to swim in lava"
                }
                MAGIC -> output += " died"
                MELTING -> output += " died of melting"
                POISON -> output += " was poisoned"
                LIGHTNING -> output += " was struck by lightning"
                PROJECTILE -> if (killer != null)
                {
                    output += " was shot to death by $killerName"
                }
                STARVATION -> output += " starved to death"
                SUFFOCATION -> output += " suffocated in a wall"
                SUICIDE -> output += " committed suicide"
                THORNS -> output += " died whilst trying to kill $killerName"
                VOID -> output += if (killer != null)
                {
                    " fell into the void thanks to $killerName"
                } else
                {
                    " fell into the void"
                }
                WITHER -> output += " withered away"
                CUSTOM -> output += " died "
            }
        } else
        {
            output += " died"
        }
        return output + CC.SEC.toString() + " died."
    }

    private fun getEntityName(entity: Entity?): String
    {
        if (entity == null)
        {
            return ""
        }
        val output: String
        output = if (entity is Player)
        {
            val player = entity
            val gamePlayer: GamePlayer = Meetup.getInstance().getPlayerHandler().getByPlayer(player)
            player.displayName + " " + CC.GRAY + "[" + CC.RED + gamePlayer.getGameKills() + CC.GRAY + "]"
        } else
        {
            val entityName: String =
                if (entity.getCustomName() != null) entity.getCustomName() else entity.getType().name()
            CC.SEC + "a " + CC.RED + WordUtils.capitalizeFully(entityName.replace("_", ""))
        }
        return output
    }

    fun getKiller(player: Player): CraftEntity?
    {
        val lastAttacker = (player as CraftPlayer).handle.lastDamager
        return lastAttacker?.bukkitEntity
    }
}
