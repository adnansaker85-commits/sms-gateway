package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadMetaDao {
    @Query("SELECT * FROM thread_meta")
    fun getAllThreadMeta(): Flow<List<ThreadMetaEntity>>

    @Query("SELECT * FROM thread_meta WHERE threadId = :threadId LIMIT 1")
    suspend fun getMetaForThread(threadId: Long): ThreadMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(meta: ThreadMetaEntity)

    @Query("UPDATE thread_meta SET isPinned = :isPinned WHERE threadId = :threadId")
    suspend fun setPinned(threadId: Long, isPinned: Boolean)

    @Query("UPDATE thread_meta SET isArchived = :isArchived WHERE threadId = :threadId")
    suspend fun setArchived(threadId: Long, isArchived: Boolean)

    @Query("UPDATE thread_meta SET draft = :draft WHERE threadId = :threadId")
    suspend fun setDraft(threadId: Long, draft: String?)
}

@Dao
interface StarredMessageDao {
    @Query("SELECT * FROM starred_messages ORDER BY date DESC")
    fun getAllStarred(): Flow<List<StarredMessageEntity>>

    @Query("SELECT messageId FROM starred_messages")
    fun getStarredMessageIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStarred(entity: StarredMessageEntity)

    @Query("DELETE FROM starred_messages WHERE messageId = :messageId")
    suspend fun deleteStarred(messageId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM starred_messages WHERE messageId = :messageId)")
    suspend fun isStarred(messageId: Long): Boolean
}

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages WHERE status = 'PENDING' ORDER BY scheduledTime ASC")
    fun getPendingScheduled(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE recipientPhone = :phone AND status = 'PENDING' ORDER BY scheduledTime ASC")
    fun getPendingForRecipient(phone: String): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduledMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduledMessageEntity): Long

    @Query("UPDATE scheduled_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE phone = :phone)")
    suspend fun isBlocked(phone: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE phone = :phone")
    suspend fun unblock(phone: String)
}

