package dev.tw1t1.a25b_10357_a2

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import dev.tw1t1.a25b_10357_a2.logic.GameManager
import dev.tw1t1.a25b_10357_a2.logic.GameManager.GameStatus
import dev.tw1t1.a25b_10357_a2.logic.GameManager.Direction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import dev.tw1t1.a25b_10357_A2.R
import dev.tw1t1.a25b_10357_a2.input.TiltController
import dev.tw1t1.a25b_10357_a2.model.GameRecord
import dev.tw1t1.a25b_10357_a2.data.RecordsDatabase
import java.util.Timer
import java.util.TimerTask
import java.util.Date

// Added imports for location handling
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class GameActivity : AppCompatActivity() {

    private lateinit var backgroundImage: ShapeableImageView
    private lateinit var heartImages: Array<ShapeableImageView>
    private lateinit var scoreLabel: TextView
    private lateinit var distanceLabel: TextView
    private lateinit var roadImages: Array<Array<ShapeableImageView>>
    private lateinit var leftButton: FloatingActionButton
    private lateinit var rightButton: FloatingActionButton
    private lateinit var tiltController: TiltController
    private lateinit var gameManager: GameManager

    private var tiltControlEnabled = false
    private var gameTimer: Timer? = null

    // Added for location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        loadBackgroundImage()

        // Initialize location services
        initializeLocationServices()

        // Initialize game manager first
        gameManager = GameManager(
            maxLives = heartImages.size,
            rows = roadImages.size,
            lanes = roadImages[0].size
        )

        // Then apply settings that might modify gameManager
        applyUserSettings()

        startGameLoop()
        updateUI()
    }

    private fun applyUserSettings() {
        // Get settings preferences
        val sharedPreferences = getSharedPreferences(MenuActivity.PREF_NAME, MODE_PRIVATE)
        val difficulty = sharedPreferences.getString(MenuActivity.PREF_DIFFICULTY, MenuActivity.DIFFICULTY_SLOW)
        val controls = sharedPreferences.getString(MenuActivity.PREF_CONTROLS, MenuActivity.CONTROLS_BUTTONS)

        // Apply difficulty setting - update the game manager's delay
        val isFastMode = difficulty == MenuActivity.DIFFICULTY_FAST
        gameManager.setDifficulty(isFastMode)

        // Apply controls setting
        if (controls == MenuActivity.CONTROLS_SENSOR) {
            toggleTiltControls(true)
        } else {
            toggleTiltControls(false)
        }
    }

    // Start/stop tilt sensing according to activity lifecycle
    override fun onResume() {
        super.onResume()
        if (tiltControlEnabled) {
            tiltController.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        tiltController.stopListening()

        // Stop location updates when app is paused
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to cancel timer when activity is destroyed
        gameTimer?.cancel()
        gameTimer = null
    }

    private fun loadBackgroundImage() {
        Glide.with(this)
            .load(R.drawable.road)
            .placeholder(R.drawable.ic_launcher_background)
            .into(backgroundImage)
    }

    private fun startGameLoop() {
        // Cancel any existing timer first
        gameTimer?.cancel()

        // Create a new timer
        gameTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread { tick() }
                }
            }, gameManager.delay, gameManager.delay)
        }
    }

    private fun tick() {
        gameManager.moveRoad().let { status ->
            handleGameStatus(status)
        }

        if (gameManager.isGameOver()) {
            // Save the game record when game is over
            saveGameRecord(gameManager.score, gameManager.distance)

            // Stop the game loop
            gameTimer?.cancel()
            gameTimer = null

            // Navigate to records view
            navigateToRecordsView()
        } else {
            updateUI()
        }
    }

    private fun navigateToRecordsView() {
        try {
            // Add logging
            android.util.Log.d("GameActivity", "Attempting to navigate to RecordsActivity")

            // Use the fully qualified class name to ensure correct navigation
            val intent = Intent(this, dev.tw1t1.a25b_10357_a2.RecordsActivity::class.java)

            // Add FLAG_ACTIVITY_CLEAR_TOP to ensure we don't create multiple instances
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(intent)

            android.util.Log.d("GameActivity", "RecordsActivity started successfully")
            finish() // Close this activity
        } catch (e: Exception) {
            // Log any exceptions
            android.util.Log.e("GameActivity", "Error navigating to RecordsActivity: ${e.message}")
            e.printStackTrace()

            // As a fallback, if we can't navigate to RecordsActivity, go to MenuActivity
            try {
                val fallbackIntent = Intent(this, dev.tw1t1.a25b_10357_a2.MenuActivity::class.java)
                startActivity(fallbackIntent)
                finish()
            } catch (e2: Exception) {
                android.util.Log.e("GameActivity", "Error navigating to fallback activity: ${e2.message}")
            }
        }
    }

    private fun initViews() {
        // Find all views
        backgroundImage = findViewById(R.id.main_IMG_background)
        leftButton = findViewById(R.id.main_FAB_left)
        rightButton = findViewById(R.id.main_FAB_right)
        tiltController = TiltController(this)

        // Setup hearts array
        findViewById<LinearLayoutCompat>(R.id.main_LLC_hearts).run {
            heartImages = Array(childCount) { i -> getChildAt(i) as ShapeableImageView }
        }

        scoreLabel = findViewById(R.id.main_LBL_score)
        distanceLabel = findViewById(R.id.main_LBL_distance)


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

        // Set tilt action callbacks
        tiltController.onTiltLeft = {
            if (tiltControlEnabled) {
                moveCar(Direction.LEFT)
            }
        }

        tiltController.onTiltRight = {
            if (tiltControlEnabled) {
                moveCar(Direction.RIGHT)
            }
        }

        // Add forward/backward tilt callbacks for speed control
        tiltController.onTiltForward = {
            if (tiltControlEnabled) {
                changeSpeed(true)
            }
        }

        tiltController.onTiltBackward = {
            if (tiltControlEnabled) {
                changeSpeed(false)
            }
        }
    }

    // New function to change game speed
    private fun changeSpeed(increase: Boolean) {
        val changed = if (increase) {
            gameManager.increaseSpeed()
        } else {
            gameManager.decreaseSpeed()
        }

        if (changed) {
            // Restart game loop with new delay
            startGameLoop()

            // Give feedback to the user
            val message = if (increase) "Speed increased!" else "Speed decreased!"
            showToast(message)
        }
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
                playSound()
                showToast("You Crashed!")
            }
            GameStatus.GAME_OVER -> {
                deviceVibrate(700)
                playSound()
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

    private fun playSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.crash_sound)
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }

    fun toggleTiltControls(enabled: Boolean) {
        tiltControlEnabled = enabled

        // Update UI based on control mode
        if (enabled) {
            // Hide buttons when using tilt controls
            leftButton.visibility = View.INVISIBLE
            rightButton.visibility = View.INVISIBLE
            tiltController.startListening()
        } else {
            // Show buttons when not using tilt controls
            leftButton.visibility = View.VISIBLE
            rightButton.visibility = View.VISIBLE
            tiltController.stopListening()
        }
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

        // Update score
        scoreLabel.text = gameManager.score.toString()

        // Update distance
        distanceLabel.text = gameManager.distance.toString()

    }

    // This function is called when a game ends to save the record
    private fun saveGameRecord(score: Int, distance: Int) {
        // Get current location (using the location services we've implemented)
        val location = getLastKnownLocation() ?: LatLng(32.0853, 34.7818) // Default to Tel Aviv if no location

        val record = GameRecord(
            id = 0, // Will be auto-generated
            score = score,
            distance = distance,
            date = Date(),
            latitude = location.latitude,
            longitude = location.longitude
        )

        // Save to database
        val db = RecordsDatabase(this)
        db.addRecord(record)
    }

    // Helper function to get location
    private fun getLastKnownLocation(): LatLng? {
        lastKnownLocation?.let {
            return LatLng(it.latitude, it.longitude)
        }
        return null
    }

    // Location services initialization
    private fun initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                showToast("Location permission denied. Using default location.")
            }
        }
    }

    // Start location updates
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lastKnownLocation = location
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
        }
    }
}