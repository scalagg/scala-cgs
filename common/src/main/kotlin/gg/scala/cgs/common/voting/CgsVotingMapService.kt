package gg.scala.cgs.common.voting

import com.cryptomorin.xseries.XMaterial
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.voting.event.VoteCompletionEvent
import gg.scala.cgs.common.voting.menu.VoteMenu
import gg.scala.cgs.common.voting.selection.VoteSelectionType
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.util.task.DiminutionRunnable
import me.lucko.helper.Events
import me.lucko.helper.terminable.composite.CompositeTerminable
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.ItemUtils
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 7/16/2022
 */
@Service
@IgnoreAutoScan
object CgsVotingMapService : DiminutionRunnable(61)
{
    @JvmStatic
    val VOTING_ITEM = ItemBuilder
        .of(XMaterial.NETHER_STAR)
        .name("${CC.YELLOW}Map Voting")
        .build()

    @Inject
    lateinit var engine: CgsGameEngine<*>

    lateinit var configuration: VotingMapConfiguration

    var votingEnabled = false
    val selections = mutableMapOf<String, MutableMap<UUID, Int>>()

    val terminable = CompositeTerminable.create()

    @Configure
    fun configure()
    {
        val config = engine.getVotingConfig()
            ?: throw IllegalArgumentException(
                "Voting is enabled yet no voting config was supplied in game implementation."
            )

        this.configuration = config

        for (entry in config.entries())
        {
            this.selections[entry.id] = mutableMapOf()
        }

        Events.subscribe(PlayerQuitEvent::class.java)
            .handler { event ->
                this.selections.onEach {
                    it.value.remove(event.player.uniqueId)
                }
            }

        Events.subscribe(PlayerQuitEvent::class.java)
            .filter {
                engine.gameState == CgsGameState.WAITING
            }
            .filter {
                Players.all().size - 1 < this
                    .configuration.minimumPlayersForVotingStart &&
                        this.votingEnabled
            }
            .handler {
                this.closeVoting(choose = false)

                val required = configuration
                    .minimumPlayersForVotingStart - (Players.all().size - 1)

                engine.sendMessage(
                    "${CC.PRI}$required${CC.SEC} more player${
                        if (required == 1) "" else "s"
                    } required for map voting to open!"
                )
            }
            .bindWith(terminable)

        Events.subscribe(VoteCompletionEvent::class.java)
            .handler {
                if (it.tie)
                {
                    engine.sendMessage("${CC.GOLD}This vote has resulted in a tie. A random map has been chosen.")
                }

                engine.sendMessage(
                    "${CC.B_YELLOW}${it.selected.displayName}${CC.GREEN} has been chosen with ${CC.YELLOW}${
                        this.selections[it.selected.id]!!.size
                    } votes${CC.GREEN}."
                )
            }

        Events.subscribe(PlayerInteractEvent::class.java)
            .filter {
                engine.gameState == CgsGameState.WAITING
            }
            .filter {
                it.hasItem() && it.action.name.contains("RIGHT")
            }
            .handler {
                when (this.configuration.selectionType)
                {
                    VoteSelectionType.PLAYER_INVENTORY ->
                    {
                        if (ItemUtils.itemTagHasKey(it.item, "voting"))
                        {
                            val key = ItemUtils
                                .readItemTagKey(
                                    it.item, "voting"
                                )

                            if (key == "random")
                            {
                                invalidatePlayerVote(it.player)
                                registerVote(it.player, null)
                            } else
                            {
                                invalidatePlayerVote(it.player)
                                registerVote(it.player, key.toString())
                            }
                        }
                    }

                    VoteSelectionType.GUI ->
                    {
                        VoteMenu().openMenu(it.player)
                    }
                }
            }
            .bindWith(terminable)

        Events.subscribe(PlayerJoinEvent::class.java)
            .filter {
                engine.gameState == CgsGameState.WAITING
            }
            .handler {
                it.player.inventory.clear()

                if (votingEnabled)
                {
                    if (this.configuration.selectionType == VoteSelectionType.GUI)
                    {
                        it.player.inventory.addItem(VOTING_ITEM)
                        it.player.updateInventory()
                    } else
                    {
                        this.configureInventory(it.player)
                    }
                }

                if (Bukkit.getOnlinePlayers().size < configuration.minimumPlayersForVotingStart)
                {
                    val required = configuration.minimumPlayersForVotingStart - Bukkit.getOnlinePlayers().size

                    it.player.sendMessage(
                        "${CC.PRI}$required${CC.SEC} more player${
                            if (required == 1) "" else "s"
                        } required for map voting to open!"
                    )
                    return@handler
                }

                if (
                    Bukkit.getOnlinePlayers().size >= configuration.minimumPlayersForVotingStart &&
                    !votingEnabled
                )
                {
                    start()
                    return@handler
                }

                if (
                    Bukkit.getOnlinePlayers().size < configuration.minimumPlayersForVotingStart &&
                    votingEnabled
                )
                {
                    closeVoting()
                    return@handler
                }
            }
            .bindWith(terminable)
    }

    fun registerVote(
        player: Player,
        id: String? = null
    )
    {
        val entryId = id ?: this.configuration
            .entries().random().id

        val entry = this
            .selections[entryId]!!

        if (entry[player.uniqueId] == null)
        {
            entry[player.uniqueId] = 1

            player.sendMessage(
                "${CC.SEC}You voted for the ${CC.PRI}${
                    this.configuration.entries()
                        .find { it.id == entryId }!!
                        .displayName
                }${CC.SEC} map."
            )

            if (
                configuration.selectionType ==
                VoteSelectionType.PLAYER_INVENTORY
            )
            {
                Players.forEach {
                    this.configureInventory(it)
                }
            }
        }
    }

    fun invalidatePlayerVote(
        player: Player
    )
    {
        this.selections.onEach { entry ->
            entry.value.remove(player.uniqueId)
        }
    }

    fun configureInventory(
        player: Player
    )
    {
        val indexed = this.configuration.entries().withIndex()

        for ((index, entry) in indexed)
        {
            val votes = this
                .selections[entry.id]!!.size

            val stack = ItemBuilder
                .of(entry.item)
                .name("${CC.YELLOW}: ${CC.AQUA}$votes")
                .build()

            ItemUtils.addToItemTag(
                stack, "voting", entry.id, false
            )

            player.inventory.setItem(
                index, ItemBuilder
                    .of(entry.item)
                    .name("${CC.YELLOW}${entry.displayName}: ${CC.AQUA}$votes")
                    .build()
            )
        }

        player.inventory.setItem(
            8, ItemBuilder
                .of(Material.SNOW_BALL)
                .name("${CC.YELLOW}Random Map")
                .build().apply {
                    ItemUtils.addToItemTag(
                        this,
                        "voting", "random",
                        false
                    )
                }
        )

        player.updateInventory()
    }

    private fun start()
    {
        this.votingEnabled = true

        this.seconds = this.configuration
            .votingAutoCloseDuration
            .seconds.toInt() + 1

        Players.forEach {
            when (this.configuration.selectionType)
            {
                VoteSelectionType.PLAYER_INVENTORY ->
                    configureInventory(it)
                VoteSelectionType.GUI -> {
                    it.player.inventory.addItem(VOTING_ITEM)
                    it.player.updateInventory()
                }
            }
        }

        engine.sendMessage("${CC.GREEN}Map voting has started.")

        this.runTaskTimer(
            engine.plugin, 0L, 20L
        )
    }

    private fun closeVoting(
        choose: Boolean = true
    )
    {
        this.votingEnabled = false

        Players.forEach {
            it.inventory.clear()
            it.updateInventory()
        }

        engine.sendMessage(
            "${CC.GREEN}Map voting has ended!"
        )

        if (choose)
        {
            val mapped = this.selections
                .mapValues {
                    it.value.size
                }

            val highest = mapped
                .entries
                .maxByOrNull { it.value }!!

            val tie = mapped
                .toMutableMap().entries
                .apply {
                    removeIf { it.key == highest.key }
                }
                .any {
                    it.value == highest.value
                }

            if (tie)
            {
                val random = this.configuration
                    .entries().random()

                val event = VoteCompletionEvent(
                    random, tie = true
                )

                event.callEvent()
            } else
            {
                val matching = this.configuration
                    .entries()
                    .find {
                        it.id == highest.key
                    }
                    ?: throw IllegalArgumentException(
                        "Entry gone?!"
                    )

                val event = VoteCompletionEvent(
                    matching, tie = false
                )

                event.callEvent()
            }
        }

        this.selections.onEach { it.value.clear() }
    }

    override fun getSeconds() =
        listOf(
            18000, 14400, 10800, 7200, 3600, 2700, 1800,
            900, 600, 300, 240, 180, 120, 60, 50, 40, 30,
            15, 10, 5, 4, 3, 2, 1
        )

    override fun onEnd()
    {
        closeVoting()
    }

    override fun onRun()
    {
        if (
            engine.gameState != CgsGameState.WAITING || Players.all().size < this
                .configuration.minimumPlayersForVotingStart && this.votingEnabled
        )
        {
            this.cancel()
            return
        }

        broadcast("${CC.SEC}Map voting will end in ${CC.PRI}${TimeUtil.formatIntoDetailedString(seconds)}${CC.SEC}.")
    }
}
