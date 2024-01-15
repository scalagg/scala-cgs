package gg.scala.cgs.common.combat

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.giveCoins
import gg.scala.cgs.common.player.handler.CgsDeathHandler
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.util.QuickAccess.username
import me.lucko.helper.Events
import me.lucko.helper.utils.entityspawner.EntitySpawner
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.metadata.FixedMetadataValue
import java.time.Instant
import java.util.*

/**
 * @author GrowlyX
 * @since 5/10/2023
 */
@Service
@IgnoreAutoScan
object CombatLogService
{
    private val combatLogs = mutableMapOf<UUID, CombatLog>()
    private val combatLogEntities = mutableSetOf<Int>()

    @Configure
    fun configure()
    {
        Events
            .subscribe(EntityDamageByEntityEvent::class.java)
            .filter {
                it.entity.entityId in combatLogEntities && it.damager !is Player
            }
            .handler {
                it.isCancelled = true
            }

        Events
            .subscribe(EntityTargetLivingEntityEvent::class.java)
            .filter {
                it.entity.entityId in combatLogEntities
            }
            .handler {
                it.isCancelled = true
            }

        Events
            .subscribe(CgsGameEngine.CgsGameParticipantConnectEvent::class.java)
            .handler {
                if (it.reconnectCalled)
                {
                    invalidate(it.participant)
                }
            }

        Events
            .subscribe(EntityDeathEvent::class.java)
            .handler {
                // prevent double call of this in #filter
                val combatLog = combatLog(it.entity)
                    ?: return@handler

                it.drops.addAll(combatLog.drops)
                it.droppedExp = it.droppedExp + combatLog.experience

                val killer = it.entity.killer

                if (killer != null)
                {
                    val cgsGameKiller = CgsPlayerHandler.find(killer)!!
                    val killerStatistics = CgsGameEngine.INSTANCE
                        .getStatistics(cgsGameKiller)

                    killerStatistics.kills++
                    killerStatistics.save()

                    killerStatistics.gameKills++

                    CgsGameTeamService.getTeamOf(killer)
                        ?.apply {
                            this.totalKills += 1
                        }

                    killer.giveCoins(100 to "Killing a player")
                }

                if (!it.entity.hasMetadata("broadcasted"))
                {
                    Bukkit.broadcastMessage(
                        CgsDeathHandler.formDeathMessage(it.entity, killer)
                    )
                }

                CgsGameEngine.CgsCombatLogDeathEvent(combatLog.player).callNow()

                combatLogs.remove(combatLog.player)
                combatLogEntities.remove(it.entity.entityId)
            }
    }

    fun combatLogFor(uniqueId: UUID) = combatLogs[uniqueId]

    fun combatLog(entity: Entity) =
        combatLogs.values.firstOrNull {
            it.entity.entityId == entity.entityId
        }

    fun combatLog(entityId: Int) =
        combatLogs.values.firstOrNull {
            it.entity.entityId == entityId
        }

    fun invalidate(player: Player) =
        combatLogs[player.uniqueId]?.apply {
            combatLogEntities.remove(entity.entityId)
            entity.remove()

            combatLogs.remove(this.player)
        }

    fun create(player: Player, relogTimeSeconds: Long)
    {
        val villager = EntitySpawner.INSTANCE
            .spawn(player.location, Villager::class.java) { _ ->

            }

        villager.maxHealth = player.maxHealth
        villager.health = player.health

        villager.customName = "${CC.GRAY}(Combat Log) ${CC.GREEN}${player.name}"
        villager.isCustomNameVisible = true

        villager.canPickupItems = false

        villager.equipment.armorContents = player.inventory.armorContents
        villager.equipment.itemInHand = player.itemInHand

        val combatLog = CombatLog(
            player = player.uniqueId,
            entity = villager,
            timestamp = Instant.now(),
            drops = player.inventory.contents.toList(),
            experience = ((player as CraftPlayer).handle).expReward
        )

        val entityId = combatLog.entity.entityId
        combatLogs[player.uniqueId] = combatLog
        combatLogEntities.add(entityId)

        delayed(relogTimeSeconds * 20L) {
            val combatLogZombie = combatLog(entityId)
                ?: return@delayed

            combatLogZombie.entity.setMetadata(
                "broadcasted",
                FixedMetadataValue(CgsGameEngine.INSTANCE.plugin, true)
            )
            combatLogZombie.entity.health = 0.0

            Bukkit.broadcastMessage(
                "${CC.GRAY}(Combat Log) ${CC.RED}${combatLogZombie.player.username()}${CC.SEC} was disconnected for too long."
            )
        }
    }
}
