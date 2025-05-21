package dev.tw1t1.a25b_10357_a2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import dev.tw1t1.a25b_10357_A2.R

class MenuActivity : AppCompatActivity() {

    private lateinit var difficultyGroup: RadioGroup
    private lateinit var controlsGroup: RadioGroup
    private lateinit var btnStartGame: Button
    private lateinit var btnViewRecords: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREF_NAME = "GamePreferences"
        const val PREF_DIFFICULTY = "gameDifficulty"
        const val PREF_CONTROLS = "gameControls"

        const val DIFFICULTY_SLOW = "slow"
        const val DIFFICULTY_FAST = "fast"
        const val CONTROLS_BUTTONS = "buttons"
        const val CONTROLS_SENSOR = "sensor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Initialize views
        difficultyGroup = findViewById(R.id.difficultyGroup)
        controlsGroup = findViewById(R.id.controlsGroup)
        btnStartGame = findViewById(R.id.btnStartGame)
        btnViewRecords = findViewById(R.id.btnViewRecords)

        // Load saved preferences
        loadSavedPreferences()

        // Set up listeners
        setupListeners()
    }

    private fun loadSavedPreferences() {
        // Load difficulty setting
        val savedDifficulty = sharedPreferences.getString(PREF_DIFFICULTY, DIFFICULTY_SLOW)
        val difficultyRadioId = if (savedDifficulty == DIFFICULTY_FAST) {
            R.id.radioDifficultyFast
        } else {
            R.id.radioDifficultySlow
        }
        difficultyGroup.check(difficultyRadioId)

        // Load controls setting
        val savedControls = sharedPreferences.getString(PREF_CONTROLS, CONTROLS_BUTTONS)
        val controlsRadioId = if (savedControls == CONTROLS_SENSOR) {
            R.id.radioControlsSensor
        } else {
            R.id.radioControlsButtons
        }
        controlsGroup.check(controlsRadioId)
    }

    private fun setupListeners() {
        // Save preferences when radio selection changes
        difficultyGroup.setOnCheckedChangeListener { _, checkedId ->
            val difficulty = if (checkedId == R.id.radioDifficultyFast) {
                DIFFICULTY_FAST
            } else {
                DIFFICULTY_SLOW
            }
            sharedPreferences.edit().putString(PREF_DIFFICULTY, difficulty).apply()
        }

        controlsGroup.setOnCheckedChangeListener { _, checkedId ->
            val controls = if (checkedId == R.id.radioControlsSensor) {
                CONTROLS_SENSOR
            } else {
                CONTROLS_BUTTONS
            }
            sharedPreferences.edit().putString(PREF_CONTROLS, controls).apply()
        }

        // Start game button
        btnStartGame.setOnClickListener {
            val gameIntent = Intent(this, GameActivity::class.java)
            startActivity(gameIntent)
        }

        // View records button
        btnViewRecords.setOnClickListener {
            val recordsIntent = Intent(this, RecordsActivity::class.java)
            startActivity(recordsIntent)
        }
    }
}