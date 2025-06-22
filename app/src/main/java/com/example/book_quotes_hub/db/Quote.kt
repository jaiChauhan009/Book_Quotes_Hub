package com.example.book_quotes_hub.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "quotes",indices = [Index(value = ["id"], unique = true)])
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0, // Primary key for Room
    @SerializedName("id")
    val id: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("quote")
    val quote: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("description")
    val description: String
)