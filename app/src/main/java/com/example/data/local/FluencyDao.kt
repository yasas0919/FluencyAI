package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FluencyDao {

    // Conversations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations ORDER BY startedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)

    // Messages
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesListForConversation(conversationId: Long): List<MessageEntity>

    // Saved Words (Pronunciation notebook)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedWord(word: SavedWordEntity): Long

    @Query("SELECT * FROM saved_words ORDER BY createdAt DESC")
    fun getAllSavedWords(): Flow<List<SavedWordEntity>>

    @Query("SELECT * FROM saved_words WHERE word = :word LIMIT 1")
    suspend fun findSavedWord(word: String): SavedWordEntity?

    @Update
    suspend fun updateSavedWord(word: SavedWordEntity)

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun deleteSavedWord(id: Long)

    // Presentations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresentation(presentation: PresentationEntity): Long

    @Query("SELECT * FROM presentations ORDER BY timestamp ASC")
    fun getAllPresentations(): Flow<List<PresentationEntity>>

    @Query("SELECT * FROM presentations ORDER BY timestamp DESC LIMIT 10")
    fun getRecentPresentations(): Flow<List<PresentationEntity>>

    @Query("SELECT AVG(scoreOutOfTen) FROM presentations")
    fun getAveragePresentationScore(): Flow<Double?>

    @Query("SELECT MAX(scoreOutOfTen) FROM presentations")
    fun getBestPresentationScore(): Flow<Float?>

    // Book Readings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookReading(reading: BookReadingEntity): Long

    @Query("SELECT * FROM book_readings ORDER BY timestamp DESC")
    fun getAllBookReadings(): Flow<List<BookReadingEntity>>

    @Query("SELECT AVG(accuracyScore) FROM book_readings")
    fun getAverageReadingAccuracy(): Flow<Double?>

    // Statistics
    @Query("SELECT COUNT(*) FROM conversations")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT AVG(fluencyScore) FROM messages WHERE sender = 'user' AND fluencyScore > 0")
    fun getOverallAverageFluency(): Flow<Double?>

    @Query("SELECT AVG(pronunciationScore) FROM messages WHERE sender = 'user' AND pronunciationScore > 0")
    fun getOverallAveragePronunciation(): Flow<Double?>
}
