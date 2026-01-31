package com.eshot.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lines")
data class Line(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val startStop: String,
    val endStop: String
)
