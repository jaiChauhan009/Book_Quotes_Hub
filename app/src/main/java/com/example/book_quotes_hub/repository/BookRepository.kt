package com.example.book_quotes_hub.repository

import android.content.Context
import com.example.book_quotes_hub.db.BookItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface BookRepository {
    suspend fun getAllBooks(): List<BookItem>
    suspend fun getBookById(isbnNo: String): BookItem?
    suspend fun refreshBooks(): List<BookItem> // Add a method for refreshing data
}