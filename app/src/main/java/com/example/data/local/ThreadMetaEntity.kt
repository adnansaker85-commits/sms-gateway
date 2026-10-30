package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "thread_meta")
data class ThreadMetaEntity(
    @PrimaryKey
    val threadId: Long,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val draft: String? = null
)

@Entity(tableName = "starred_messages")
data class StarredMessageEntity(
    @PrimaryKey
    val messageId: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long
)
