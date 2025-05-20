package dev.tw1t1.a25b_10357_A2.logic

import dev.tw1t1.a25b_10357_A2.model.RoadCell
import kotlin.random.Random

class GameManager(
    val maxLives: Int = 3,
    val rows: Int = 7,
    val lanes: Int = 5

) {
    enum class RoadCellType { CAR, EMPTY, ROCK }
    enum class Direction { LEFT, RIGHT }
    enum class GameStatus { OK, CRASHED, BLOCKED, GAME_OVER }

    companion object {
        const val DELAY: Long = 1000
    }

    var livesRemaining: Int = maxLives
        private set

    var carLane: Int = lanes / 2
        private set

    val road: Array<Array<RoadCell>> = Array(rows) {
        Array(lanes) { RoadCell() }
    }

    init {
        placeCar()
    }

    fun isGameOver() = livesRemaining <= 0

    private fun placeCar() {
        carLane = lanes / 2
        road[rows - 1][carLane].type = RoadCellType.CAR
    }

    private fun carCollision(): GameStatus {
        livesRemaining--
        return if (isGameOver()) GameStatus.GAME_OVER else GameStatus.CRASHED
    }

    fun reset() {
        // Clear road
        for (r in rows - 1 downTo 0) {
            for (l in 0 until lanes) {
                road[r][l].type = RoadCellType.EMPTY
            }
        }

        // Reset car and lives
        placeCar()
        livesRemaining = maxLives
    }

    fun moveCar(direction: Direction): GameStatus {
        return when (direction) {
            Direction.LEFT -> moveCarLeft()
            Direction.RIGHT -> moveCarRight()
        }
    }

    private fun moveCarLeft(): GameStatus {
        // Check if at left edge
        if (carLane <= 0) return GameStatus.BLOCKED

        // Check for collision with rock
        val targetLane = carLane - 1
        val hasRock = road[rows - 1][targetLane].type == RoadCellType.ROCK

        // Move car left
        road[rows - 1][targetLane].type = RoadCellType.CAR
        road[rows - 1][carLane].type = RoadCellType.EMPTY
        carLane = targetLane

        // Return appropriate status
        return if (hasRock) carCollision() else GameStatus.OK
    }

    private fun moveCarRight(): GameStatus {
        // Check if at right edge
        if (carLane >= lanes - 1) return GameStatus.BLOCKED

        // Check for collision with rock
        val targetLane = carLane + 1
        val hasRock = road[rows - 1][targetLane].type == RoadCellType.ROCK

        // Move car right
        road[rows - 1][targetLane].type = RoadCellType.CAR
        road[rows - 1][carLane].type = RoadCellType.EMPTY
        carLane = targetLane

        // Return appropriate status
        return if (hasRock) carCollision() else GameStatus.OK
    }

    fun moveRoad(): GameStatus {
        var status = GameStatus.OK
        var rocksInTopRow = 0

        // Process existing road cells (bottom to top)
        for (r in rows - 1 downTo 0) {
            for (l in 0 until lanes) {
                if (road[r][l].type == RoadCellType.ROCK) {
                    if (r < rows - 1) {
                        val cellBelow = road[r + 1][l]

                        // Check if rock would hit car
                        if (cellBelow.type == RoadCellType.CAR) {
                            status = carCollision()
                        } else {
                            // Move rock down
                            cellBelow.type = RoadCellType.ROCK
                        }
                    }

                    // Clear current position
                    road[r][l].type = RoadCellType.EMPTY
                }
            }
        }

        // Generate new rocks in top row
        for (l in 0 until lanes) {
            if (rocksInTopRow < lanes - 1 && Random.nextInt(10) == 0) {
                road[0][l].type = RoadCellType.ROCK
                rocksInTopRow++
            }
        }

        return status
    }
}