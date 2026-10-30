package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey
    val phone: String,
    val name: String = "",
    val reason: String = "Spam/Unwanted",
    val blockedAt: Long = System.currentTimeMillis()
)
