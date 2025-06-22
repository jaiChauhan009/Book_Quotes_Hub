package com.example.book_quotes_hub.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if book with same ISBN exists
    suspend fun insertBooks(books: List<BookItem>)

    @Query("SELECT * FROM books_table")
    suspend fun getAllBooks(): List<BookItem>

    @Query("SELECT * FROM books_table WHERE isbn = :isbnNo")
    suspend fun getBookById(isbnNo: String): BookItem?

    @Query("DELETE FROM books_table")
    suspend fun clearAllBooks()
}