package com.example.book_quotes_hub.repository

import android.content.Context
import android.util.Log
import com.example.book_quotes_hub.db.BookDao
import com.example.book_quotes_hub.db.BookItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Indicate that Hilt should create a single instance
class BookRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json, // Injected Json instance
    private val bookDao: BookDao // Injected BookDao
) : BookRepository {

    private val TAG = "BookRepositoryImpl"
    private val JSON_ASSET_NAME = "books.json" // Name of your JSON asset file

    // Reads books from the local assets file
    private suspend fun readBooksFromAsset(): List<BookItem> = withContext(Dispatchers.IO) {
        try {
            context.assets.open(JSON_ASSET_NAME).bufferedReader().use {
                json.decodeFromString<List<BookItem>>(it.readText())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading books from asset: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getAllBooks(): List<BookItem> {
        // First, try to get from DB (cache)
        val cachedBooks = bookDao.getAllBooks()
        if (cachedBooks.isNotEmpty()) {
            Log.d(TAG, "Loading books from cache.")
            return cachedBooks
        }

        // If no cache, or on refresh, load from asset and then cache
        return refreshBooks()
    }

    override suspend fun getBookById(isbnNo: String): BookItem? {
        // Try to get from DB first
        val cachedBook = bookDao.getBookById(isbnNo)
        if (cachedBook != null) {
            Log.d(TAG, "Loading book by ID from cache: $isbnNo")
            return cachedBook
        }

        // If not in DB, try to load from asset and then find
        Log.d(TAG, "Book not in cache, attempting to load from asset: $isbnNo")
        val booksFromAsset = readBooksFromAsset()
        val foundBook = booksFromAsset.firstOrNull { it.isbn.contentEquals(isbnNo, ignoreCase = true) }
        foundBook?.let {
            bookDao.insertBooks(listOf(it)) // Cache the found book
            Log.d(TAG, "Book $isbnNo found in asset and cached.")
        }
        return foundBook
    }

    override suspend fun refreshBooks() : List<BookItem> {
        Log.d(TAG, "Refreshing books: Clearing cache and reloading from asset.")
        bookDao.clearAllBooks()
        val booksFromAsset = readBooksFromAsset()
        if (booksFromAsset.isNotEmpty()) {
            bookDao.insertBooks(booksFromAsset) // Cache all books from asset
            Log.d(TAG, "Inserted ${booksFromAsset.size} books from asset to cache.")
        } else {
            Log.w(TAG, "No books found in asset to refresh.")
        }
        return booksFromAsset
    }
}