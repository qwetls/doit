package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val iconKey: String,
    val colorHex: Long
)
