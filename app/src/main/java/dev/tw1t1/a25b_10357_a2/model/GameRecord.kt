package dev.tw1t1.a25b_10357_a2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class GameRecord(
    val id: Long,
    val score: Int,
    val distance: Int,
    val date: Date,
    val latitude: Double,
    val longitude: Double
) : Parcelable, Comparable<GameRecord> {

    override fun compareTo(other: GameRecord): Int {
        // Sort by score (higher first), then by distance (higher first)
        return when {
            score != other.score -> other.score.compareTo(score) // Higher score first
            distance != other.distance -> other.distance.compareTo(distance) // Higher distance first
            else -> other.date.compareTo(date) // More recent date first
        }
    }
}