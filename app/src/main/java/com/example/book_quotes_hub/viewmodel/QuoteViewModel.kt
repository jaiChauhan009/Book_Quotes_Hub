package com.example.bookhub.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book_quotes_hub.data.repository.QuoteRepository
import com.example.book_quotes_hub.db.Quote
import com.example.book_quotes_hub.model.QuoteItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest // Import for flatMapLatest
import kotlinx.coroutines.flow.firstOrNull

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val repository: QuoteRepository
) : ViewModel() {

    // --- State for QuoteListScreen ---
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val quotes: StateFlow<List<QuoteItem>> = _selectedCategory
        .flatMapLatest { category ->
            // repository.currentCategoryQuotes already filters based on the last load
            // The map here is to ensure the UI only gets items matching the current selected category.
            // Note: The repository's `updateCurrentCategoryQuotesFromDb` is responsible for
            // actually loading the data into `repository.currentCategoryQuotes`.
            repository.currentCategoryQuotes.map { allQuotes ->
                if (category == null || category == "All Quotes") {
                    allQuotes
                } else {
                    allQuotes.filter { it.category.equals(category, ignoreCase = true) }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val isLoading: StateFlow<Boolean> = repository.isLoading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val errorMessage: StateFlow<String?> = repository.errorMessage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- State for QuoteDetailScreen ---
    private val _selectedQuote = MutableStateFlow<QuoteItem?>(null)
    val selectedQuote: StateFlow<QuoteItem?> = _selectedQuote.asStateFlow()

    private val _detailLoading = MutableStateFlow(false)
    val detailLoading: StateFlow<Boolean> = _detailLoading.asStateFlow()

    private val _detailErrorMessage = MutableStateFlow<String?>(null)
    val detailErrorMessage: StateFlow<String?> = _detailErrorMessage.asStateFlow()

    init {
        // Initial load of quotes when ViewModel is created
        // This will fetch "All Quotes" initially
        loadNextPage() // Renamed from initialLoad to be consistent with usage
    }

    fun loadNextPage() {
        // With the new API, "loading next page" effectively means "load more data for the current category"
        // If the API returns all data for a category at once, this call might just refresh what's already there
        // or ensure the latest data for that category is fetched.
        viewModelScope.launch {
            repository.loadQuotes(category = _selectedCategory.value)
        }
    }

    fun refreshQuotes() {
        viewModelScope.launch {
            repository.resetPagination() // Resets internal state
            repository.loadQuotes(category = _selectedCategory.value, forceRefresh = true) // Force API fetch
        }
    }

    fun selectCategory(category: String?) {
        val effectiveCategory = if (category == "All Quotes") null else category
        if (_selectedCategory.value != effectiveCategory) {
            _selectedCategory.value = effectiveCategory
            viewModelScope.launch {
                repository.resetPagination() // Reset pagination state for the new category
                repository.loadQuotes(category = effectiveCategory, forceRefresh = true) // Force fetch for the new category
            }
        }
    }

    fun fetchQuoteDetails(quoteId: Int) {
        _detailLoading.value = true
        _detailErrorMessage.value = null
        viewModelScope.launch {
            try {
                val quote = repository.getQuoteDetails(quoteId)
                _selectedQuote.value = quote
                if (quote == null) {
                    _detailErrorMessage.value = "Quote details not found for ID: $quoteId."
                }
            } catch (e: Exception) {
                _detailErrorMessage.value = "Error fetching details: ${e.message}"
            } finally {
                _detailLoading.value = false
            }
        }
    }
}
