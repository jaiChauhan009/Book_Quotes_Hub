// data/repository/QuoteRepository.kt (UPDATED)
package com.example.book_quotes_hub.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.book_quotes_hub.db.Quote
import com.example.book_quotes_hub.db.QuoteDao
import com.example.book_quotes_hub.model.QuoteItem
import com.example.book_quotes_hub.network.QuoteApiResponse
import com.example.book_quotes_hub.network.QuoteApiService
import com.example.book_quotes_hub.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext // Import withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map


@Singleton
class QuoteRepository @Inject constructor(
    private val apiService: QuoteApiService,
    private val quoteDao: QuoteDao,
    private val context: Context // For NetworkUtils
) {
    private val _currentCategoryQuotes = MutableStateFlow<List<Quote>>(emptyList())
    // This flow maps the Room entities to your UI model (QuoteItem)
    val currentCategoryQuotes: Flow<List<QuoteItem>> = _currentCategoryQuotes.map { quotes ->
        quotes.map { QuoteItem.fromQuoteEntity(it) }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: Flow<String?> = _errorMessage.asStateFlow()

    private var isLastPageReached = false // Still useful for signalling no more data

    suspend fun loadQuotes(category: String? = null, forceRefresh: Boolean = false) {
        if (_isLoading.value && !forceRefresh) return // Prevent duplicate requests
        _isLoading.value = true
        _errorMessage.value = null

        if (forceRefresh) {
            isLastPageReached = false
            if (category == null) {
                quoteDao.clearAllQuotes()
            }
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                Log.d("QuoteRepository", "Fetching quotes from API, category: $category")
                val apiResponse: QuoteApiResponse = if (category.isNullOrBlank() || category == "All Quotes") {
                    apiService.getQuotes() // Call for all quotes
                } else {
                    apiService.getQuotesByCategory(category) // Call for specific category
                }

                // CORRECTED: Handle potential null `apiResponse.quotes` using the Elvis operator
                val quotesFromApi = apiResponse.quotes ?: emptyList() // If null, default to empty list

                if (quotesFromApi.isEmpty()) { // Now this is safe to call
                    isLastPageReached = true // No quotes found for this request
                    if (quoteDao.getQuoteCountForCategory(category) == 0) {
                        _errorMessage.value = "No quotes available from the server for this category."
                    }
                } else {
                    quoteDao.insertQuotes(quotesFromApi)
                    isLastPageReached = true // Assuming each call fetches the full set for the category
                }

                updateCurrentCategoryQuotesFromDb(category)

            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error fetching quotes from API: ${e.message}", e)
                _errorMessage.value = "Failed to load quotes from network: ${e.message}. Loading from cache."
                updateCurrentCategoryQuotesFromDb(category)
            } finally {
                _isLoading.value = false
            }
        } else {
            Log.d("QuoteRepository", "No internet, loading from cache for category: $category")
            _errorMessage.value = "No internet connection. Loading cached quotes."
            updateCurrentCategoryQuotesFromDb(category)
            _isLoading.value = false
        }
    }

    private suspend fun updateCurrentCategoryQuotesFromDb(category: String?) {
        val quotesFromDb = if (category.isNullOrBlank() || category == "All Quotes") {
            quoteDao.getAllQuotes()
        } else {
            quoteDao.getQuotesByCategory(category)
        }
        _currentCategoryQuotes.value = quotesFromDb
        if (quotesFromDb.isEmpty() && !NetworkUtils.isInternetAvailable(context)) {
            _errorMessage.value = "No cached quotes available for this category."
        }
    }

    suspend fun getQuoteDetails(quoteId: Int): QuoteItem? {
        _errorMessage.value = null
        var quote = quoteDao.getQuoteById(quoteId)

        return quote?.let { QuoteItem.fromQuoteEntity(it) }
    }

    fun resetPagination() {
        isLastPageReached = false
    }

    fun isLastPage(): Boolean = isLastPageReached
}
