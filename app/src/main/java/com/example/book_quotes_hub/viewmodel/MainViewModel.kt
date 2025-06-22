package com.example.bookhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book_quotes_hub.utils.DetailViewState
import com.example.book_quotes_hub.utils.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.book_quotes_hub.db.BookItem
import com.example.book_quotes_hub.repository.BookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // Internal state to hold all books fetched from the repository
    private val _allBooks = MutableStateFlow<List<BookItem>>(emptyList())

    // UI state for the overall loading/error/empty status of the book list operation
    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val books: StateFlow<ViewState> = _viewState.asStateFlow()

    // UI state for a single book's details
    private val _detailViewState = MutableStateFlow<DetailViewState>(DetailViewState.Loading)
    val bookDetails: StateFlow<DetailViewState> = _detailViewState.asStateFlow()

    // State for the search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Combined flow that filters _allBooks based on _searchQuery
    val filteredBooks: StateFlow<List<BookItem>> = combine(
        _allBooks,
        _searchQuery
    ) { allBooks, query ->
        if (query.isBlank()) {
            allBooks // If query is empty, show all books
        } else {
            // Filter by title or any author name (case-insensitive)
            allBooks.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                        book.authors.any { author -> author.contains(query, ignoreCase = true) }
            }
        }
    }.stateIn( // Lazily start collection when there's an active collector
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    init {
        getAllBooks() // Load all books on ViewModel initialization
    }

    fun getAllBooks() = viewModelScope.launch {
        _viewState.value = ViewState.Loading // Set loading state for the overall list
        try {
            val bookList = bookRepository.getAllBooks()
            _allBooks.value = bookList // Update the internal list of all books
            if (bookList.isNotEmpty()) {
                _viewState.value = ViewState.Success(bookList) // Set success state with all books
            } else {
                _viewState.value = ViewState.Empty // Set empty state
            }
        } catch (e: Exception){
            _viewState.value = ViewState.Error(e) // Set error state
        }
    }

    fun getBookByID(isbnNO: String) = viewModelScope.launch {
        _detailViewState.value = DetailViewState.Loading // Set loading state for details
        try {
            val book = bookRepository.getBookById(isbnNO)

            if (book != null) {
                _detailViewState.value = DetailViewState.Success(book) // Set success state for details
            } else {
                _detailViewState.value = DetailViewState.Empty // Set empty state for details
            }
        } catch (e: Exception){
            _detailViewState.value = DetailViewState.Error(e) // Set error state for details
        }
    }

    fun refreshBooks() = viewModelScope.launch {
        _viewState.value = ViewState.Loading // Set loading state for refresh
        try {
            val bookList = bookRepository.refreshBooks() // Clear cache and reload
            _allBooks.value = bookList // Update the internal list of all books
            if (bookList.isNotEmpty()) {
                _viewState.value = ViewState.Success(bookList) // Set success state
            } else {
                _viewState.value = ViewState.Empty // Set empty state
            }
            _searchQuery.value = "" // Clear search query on refresh
        } catch (e: Exception){
            _viewState.value = ViewState.Error(e) // Set error state
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query // Update search query state
    }
}



