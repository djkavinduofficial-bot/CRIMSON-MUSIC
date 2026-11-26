package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY addedAt ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracks(tracks: List<TrackEntity>): List<Long>

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE uriString = :uriString")
    suspend fun setFavorite(uriString: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET durationMs = :durationMs WHERE uriString = :uriString")
    suspend fun updateDuration(uriString: String, durationMs: Long)

    @Query("DELETE FROM tracks WHERE uriString = :uriString")
    suspend fun deleteTrack(uriString: String)
}
