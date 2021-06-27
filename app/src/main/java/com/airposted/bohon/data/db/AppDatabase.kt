package com.airposted.bohon.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Location::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDAO

    companion object {

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        @JvmField
        val MIGRATION_1_2 = Migration1To2()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "location_table.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }

    class Migration1To2 : Migration(1,2) {
        private val TABLE_NAME = "location_table"

        override fun migrate(database: SupportSQLiteDatabase) {
            val TABLE_NAME_TEMP = "location_table_new"

            // 1. Create new table
            database.execSQL("CREATE TABLE " + TABLE_NAME_TEMP + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " name TEXT, latitude DOUBLE, longitude DOUBLE)")

            // 2. Copy the data
            database.execSQL("INSERT INTO $TABLE_NAME_TEMP (name, latitude, longitude) "
                    + "SELECT name, latitude, longitude "
                    + "FROM $TABLE_NAME")

            // 3. Remove the old table
            database.execSQL("DROP TABLE $TABLE_NAME")

            // 4. Change the table name to the correct one
            database.execSQL("ALTER TABLE $TABLE_NAME_TEMP RENAME TO $TABLE_NAME")
        }
    }
}