package com.airposted.bitoronbd.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class Location(
    var name: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}