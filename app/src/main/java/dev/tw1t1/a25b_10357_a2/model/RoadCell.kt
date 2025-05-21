package dev.tw1t1.a25b_10357_a2.model

import dev.tw1t1.a25b_10357_A2.R
import dev.tw1t1.a25b_10357_a2.logic.GameManager.RoadCellType

/**
 * Represents a cell in the game's road grid.
 * The cell can be empty, contain a car, or contain a rock.
 */
class RoadCell {
    /**
     * The type of this road cell.
     * Setting this property automatically updates the associated image resource.
     */
    var type: RoadCellType = RoadCellType.EMPTY
        set(value) {
            field = value
            updateImageResource()
        }

    /**
     * The image resource ID to display for this cell.
     * This value is automatically updated when the cell type changes.
     */
    var imageResource: Int = 0
        private set

    init {
        updateImageResource()
    }

    /**
     * Updates the image resource based on the cell type.
     */
    private fun updateImageResource() {
        imageResource = when (type) {
            RoadCellType.CAR -> R.drawable.car
            RoadCellType.ROCK -> R.drawable.rock
            RoadCellType.COIN -> R.drawable.coin
            RoadCellType.EMPTY -> 0
        }
    }
}