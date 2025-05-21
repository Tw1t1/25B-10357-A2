package dev.tw1t1.a25b_10357_a2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.tw1t1.a25b_10357_a2.data.RecordsDatabase
import dev.tw1t1.a25b_10357_a2.model.GameRecord
import dev.tw1t1.a25b_10357_A2.R
import java.text.SimpleDateFormat
import java.util.Locale

class RecordsFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private var recordsList: List<GameRecord> = listOf()
    private var listener: OnRecordSelectedListener? = null
    private var selectedRowIndex: Int = -1

    interface OnRecordSelectedListener {
        fun onRecordSelected(record: GameRecord)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.recordsTable)

        // Load records from database
        loadRecords()
    }

    fun setOnRecordSelectedListener(listener: OnRecordSelectedListener) {
        this.listener = listener
    }

    private fun loadRecords() {
        context?.let { ctx ->
            val db = RecordsDatabase(ctx)

            // Get top records
            recordsList = db.getTopRecords(10)

            // Display records in table
            populateTable()
        }
    }

    private fun populateTable() {
        // Clear existing rows except header
        val headerRow = tableLayout.getChildAt(0)
        tableLayout.removeAllViews()
        tableLayout.addView(headerRow)

        // Add records to table
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        recordsList.forEachIndexed { index, record ->
            val row = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                // Set row tag to record position
                tag = index

                // Set click listener
                setOnClickListener {
                    selectRow(index)
                    listener?.onRecordSelected(record)
                }
            }

            // Add position column (1-based)
            addCell(row, "#${index + 1}")

            // Add score column
            addCell(row, record.score.toString())

            // Add distance column
            addCell(row, "${record.distance}m")

            // Add date column
            addCell(row, dateFormat.format(record.date))

            tableLayout.addView(row)
        }
    }

    private fun addCell(row: TableRow, text: String) {
        val textView = TextView(context).apply {
            this.text = text
            setPadding(16, 16, 16, 16)
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        row.addView(textView)
    }

    private fun selectRow(position: Int) {
        // Reset previously selected row
        if (selectedRowIndex >= 0 && selectedRowIndex < tableLayout.childCount - 1) {
            val previousRow = tableLayout.getChildAt(selectedRowIndex + 1) as? TableRow
            previousRow?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }

        // Highlight newly selected row
        val newRow = tableLayout.getChildAt(position + 1) as? TableRow
        context?.let { ctx ->
            newRow?.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_blue_light))
        }

        selectedRowIndex = position
    }
}