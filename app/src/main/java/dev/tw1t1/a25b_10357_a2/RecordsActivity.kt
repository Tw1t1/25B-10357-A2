package dev.tw1t1.a25b_10357_a2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dev.tw1t1.a25b_10357_A2.R
import dev.tw1t1.a25b_10357_a2.data.RecordsDatabase
import dev.tw1t1.a25b_10357_a2.model.GameRecord
import dev.tw1t1.a25b_10357_a2.fragments.RecordsFragment
import dev.tw1t1.a25b_10357_a2.fragments.MapFragment

class RecordsActivity : AppCompatActivity(), RecordsFragment.OnRecordSelectedListener {

    private lateinit var recordsFragment: RecordsFragment
    private lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_records)
            Log.d("RecordsActivity", "Content view set successfully")

            // Set up toolbar with back button
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Top Records"

            // Get fragments
            try {
                recordsFragment = supportFragmentManager.findFragmentById(R.id.recordsFragment) as RecordsFragment
                mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
                Log.d("RecordsActivity", "Fragments found successfully")
            } catch (e: Exception) {
                Log.e("RecordsActivity", "Error finding fragments: ${e.message}")
                e.printStackTrace()
            }

            // Set listener for record selection
            recordsFragment.setOnRecordSelectedListener(this)

            // Load records data from database
            loadRecordsData()
        } catch (e: Exception) {
            Log.e("RecordsActivity", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            // If there's a critical error, return to the previous screen
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle back button in toolbar
        onBackPressed()
        return true
    }

    override fun onRecordSelected(record: GameRecord) {
        try {
            // Update map to focus on selected record
            mapFragment.highlightRecord(record)
        } catch (e: Exception) {
            Log.e("RecordsActivity", "Error highlighting record: ${e.message}")
        }
    }

    private fun loadRecordsData() {
        try {
            // Load records from database and update map
            val db = RecordsDatabase(this)
            val records = db.getTopRecords(10)
            Log.d("RecordsActivity", "Loaded ${records.size} records from database")

            // Pass records to map fragment
            mapFragment.setRecords(records)
        } catch (e: Exception) {
            Log.e("RecordsActivity", "Error loading records: ${e.message}")
            e.printStackTrace()
        }
    }
}