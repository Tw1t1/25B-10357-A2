package dev.tw1t1.a25b_10357_a2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dev.tw1t1.a25b_10357_a2.model.GameRecord
import java.util.Date

class RecordsDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "GameRecords.db"

        private const val TABLE_RECORDS = "records"
        private const val COLUMN_ID = "id"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_DISTANCE = "distance"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_RECORDS_TABLE = ("CREATE TABLE " + TABLE_RECORDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SCORE + " INTEGER,"
                + COLUMN_DISTANCE + " INTEGER,"
                + COLUMN_DATE + " INTEGER,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL" + ")")
        db.execSQL(CREATE_RECORDS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        onCreate(db)
    }

    fun addRecord(record: GameRecord): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_SCORE, record.score)
        values.put(COLUMN_DISTANCE, record.distance)
        values.put(COLUMN_DATE, record.date.time)
        values.put(COLUMN_LATITUDE, record.latitude)
        values.put(COLUMN_LONGITUDE, record.longitude)

        // Insert the new row, returning the primary key value
        val id = db.insert(TABLE_RECORDS, null, values)
        db.close()
        return id
    }

    fun getTopRecords(limit: Int = 10): List<GameRecord> {
        val records = ArrayList<GameRecord>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RECORDS ORDER BY $COLUMN_SCORE DESC, $COLUMN_DISTANCE DESC, $COLUMN_DATE DESC LIMIT $limit"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val record = GameRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                    distance = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                    date = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE))),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                )
                records.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
    }

}