package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val uriString: String,
    val title: String,
    val artist: String,
    val format: String,
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
