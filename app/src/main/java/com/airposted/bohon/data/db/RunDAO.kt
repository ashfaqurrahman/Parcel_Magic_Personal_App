package com.airposted.bohon.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Location)

    @Delete
    suspend fun deleteRun(run: Location)

    @Query("SELECT name FROM location_table GROUP BY name ORDER BY id DESC LIMIT 5")
    fun getAllLocations(): LiveData<List<Location>>

    @Query("SELECT name FROM location_table ORDER BY id DESC LIMIT 1")
    fun getLastLocation(): LiveData<List<Location>>
}










