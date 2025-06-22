package com.example.book_quotes_hub.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "books_table")
@Serializable // Keep this for reading from JSON asset
data class BookItem(
    val authors: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    @PrimaryKey // ISBN is unique for books
    val isbn: String = "",
    val longDescription: String? = null, // Make nullable as it can be missing
    val pageCount: Int = 0,
    val shortDescription: String? = null, // Make nullable
    val status: String = "",
    val thumbnailUrl: String? = null, // Make nullable
    val title: String = ""
)