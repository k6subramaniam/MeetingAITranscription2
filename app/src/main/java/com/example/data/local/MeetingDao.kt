package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY createdAt DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    fun getMeetingById(id: Long): Flow<MeetingEntity?>

    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getMeetingByIdDirect(id: Long): MeetingEntity?

    @Query("SELECT * FROM meetings WHERE title LIKE '%' || :query || '%' OR rawTranscript LIKE '%' || :query || '%' OR executiveSummary LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMeetings(query: String): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE category = :category ORDER BY createdAt DESC")
    fun getMeetingsByCategory(category: String): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteMeetings(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeetings(meetings: List<MeetingEntity>)

    @Update
    suspend fun updateMeeting(meeting: MeetingEntity)

    @Delete
    suspend fun deleteMeeting(meeting: MeetingEntity)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: Long)

    @Query("UPDATE meetings SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("SELECT COUNT(*) FROM meetings")
    suspend fun getMeetingCount(): Int
}
