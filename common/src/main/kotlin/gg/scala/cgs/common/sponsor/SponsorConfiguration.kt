package gg.scala.cgs.common.sponsor

import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
interface SponsorConfiguration
{
    fun getPrizes(): List<SponsorPrize>
}
