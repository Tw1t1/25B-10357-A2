package dev.tw1t1.a25b_10357_A2

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import dev.tw1t1.a25b_10357_A2.logic.GameManager
import dev.tw1t1.a25b_10357_A2.logic.GameManager.GameStatus
import dev.tw1t1.a25b_10357_A2.logic.GameManager.Direction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var backgroundImage: ShapeableImageView
    private lateinit var heartImages: Array<ShapeableImageView>
    private lateinit var roadImages: Array<Array<ShapeableImageView>>
    private lateinit var leftButton: FloatingActionButton
    private lateinit var rightButton: FloatingActionButton
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadBackgroundImage()

        gameManager = GameManager(
            maxLives = heartImages.size,
            rows = roadImages.size,
            lanes = roadImages[0].size
        )

        startGameLoop()
        updateUI()
    }

    private fun loadBackgroundImage() {
        Glide.with(this)
            .load(R.drawable.road)
            .placeholder(R.drawable.ic_launcher_background)
            .into(backgroundImage)
    }

    private fun startGameLoop() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread { tick() }
            }
        }, GameManager.DELAY, GameManager.DELAY)
    }

    private fun tick() {
        gameManager.moveRoad().let { status ->
            handleGameStatus(status)
        }

        if (gameManager.isGameOver()) {
            gameManager.reset()
        }

        updateUI()
    }

    private fun initViews() {
        // Find all views
        backgroundImage = findViewById(R.id.main_IMG_background)
        leftButton = findViewById(R.id.main_FAB_left)
        rightButton = findViewById(R.id.main_FAB_right)

        // Setup hearts array
        findViewById<LinearLayoutCompat>(R.id.main_LLC_hearts).run {
            heartImages = Array(childCount) { i -> getChildAt(i) as ShapeableImageView }
        }

        // Setup road matrix
        val roadLayout = findViewById<LinearLayoutCompat>(R.id.main_LLC_road)
        val lanes = roadLayout.childCount
        val laneLayouts = Array(lanes) { i -> roadLayout.getChildAt(i) as LinearLayoutCompat }
        val rows = laneLayouts[0].childCount

        roadImages = Array(rows) { r ->
            Array(lanes) { l ->
                laneLayouts[l].getChildAt(r) as ShapeableImageView
            }
        }

        // Set button click listeners
        leftButton.setOnClickListener { moveCar(Direction.LEFT) }
        rightButton.setOnClickListener { moveCar(Direction.RIGHT) }
    }

    private fun moveCar(direction: Direction) {
        gameManager.moveCar(direction).let { status ->
            handleGameStatus(status)
        }
        updateUI()
    }

    private fun handleGameStatus(status: GameStatus) {
        when (status) {
            GameStatus.BLOCKED -> deviceVibrate(50)
            GameStatus.CRASHED -> {
                deviceVibrate(400)
                showToast("You Crashed!")
            }
            GameStatus.GAME_OVER -> {
                deviceVibrate(700)
                showToast("Game Over, try again ><")
            }
            GameStatus.OK -> {} // No action needed
        }
    }

    private fun deviceVibrate(milliseconds: Int) {
        (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrate(VibrationEffect.createOneShot(milliseconds.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrate(milliseconds.toLong())
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        // Update road
        for (r in roadImages.indices) {
            for (l in roadImages[0].indices) {
                roadImages[r][l].setImageResource(gameManager.road[r][l].imageResource)
            }
        }

        // Update hearts
        heartImages.forEachIndexed { index, heart ->
            heart.visibility = if (index < gameManager.livesRemaining) View.VISIBLE else View.INVISIBLE
        }
    }
}