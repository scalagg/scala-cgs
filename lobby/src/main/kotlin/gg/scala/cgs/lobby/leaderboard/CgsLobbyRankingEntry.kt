package gg.scala.cgs.lobby.leaderboard

import net.evilblock.cubed.serializers.Serializers
import org.bson.Document
import java.util.UUID

fun Document.parseIntoLeaderboardResult(): LeaderboardResult =
    Serializers.gson
        .fromJson(
            toJson(), LeaderboardResult::class.java
        )

data class LeaderboardResult(
    val uniqueId: UUID,
    val value: Int
)

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
interface CgsLobbyRankingEntry
{
    fun getId(): String
    fun getDisplay(): String

    fun getStatLabel(): String

    fun buildAggregation() = listOf(
        Document(
            "\$project",
            Document("uniqueId", "\$_id")
                .append("value", "\$${getStatLabel()}.value")
        ),
        Document(
            "\$sort",
            Document("value", -1L)
        ),
        Document("\$limit", 10)
    )

    fun buildAggregationMatchingUser(player: UUID) = listOf(
        Document(
            "\$match",
            Document("_id", player.toString())
        ),
        Document(
            "\$project",
            Document("uniqueId", "\$_id")
                .append("value", "\$${getStatLabel()}.value")
        )
    )
}
