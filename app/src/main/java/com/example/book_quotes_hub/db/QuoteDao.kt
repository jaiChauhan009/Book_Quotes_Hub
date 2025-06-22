package com.example.book_quotes_hub.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow



@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>)

    @Query("SELECT * FROM quotes ORDER BY id ASC") // Or order by localId if preferred
    suspend fun getAllQuotes(): List<Quote>

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY id ASC")
    suspend fun getQuotesByCategory(category: String): List<Quote>

    @Query("SELECT * FROM quotes WHERE id = :quoteId") // Use 'id' from your entity for details
    suspend fun getQuoteById(quoteId: Int): Quote?

    @Query("DELETE FROM quotes")
    suspend fun clearAllQuotes()

    @Query("SELECT id FROM quotes") // Get the network IDs, not localId
    suspend fun getAllQuoteIds(): List<Int>

    // Added for checking if a category has quotes in DB
    @Query("SELECT COUNT(*) FROM quotes WHERE category = :category")
    suspend fun getQuoteCountForCategory(category: String?): Int // category can be null
}