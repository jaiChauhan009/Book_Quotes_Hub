package com.example.book_quotes_hub.navigation

import androidx.annotation.StringRes
import com.example.book_quotes_hub.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppScreen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Books : AppScreen("books_root", R.string.text_books, Icons.Default.AutoStories)
    object Quotes : AppScreen("quotes_root", R.string.text_quotes, Icons.Default.FormatQuote)
}

// --- Nested Screens for Book Flow ---
sealed class BookScreens(val route: String) {
    object List : BookScreens("book_list")
    object Details : BookScreens("book_details/{isbnNo}") {
        fun createRoute(isbnNo: String) = "book_details/$isbnNo"
    }
}

// --- Nested Screens for Quote Flow ---
sealed class QuoteScreens(val route: String) {
    object List : QuoteScreens("quote_list")
    object Details : QuoteScreens("quote_details/{quoteId}") {
        fun createRoute(quoteId: String) = "quote_details/$quoteId" // Changed to String
    }
}

// --- Argument Keys ---
object EndPoints {
    const val ISBN_NO = "isbnNo"
    const val QUOTE_ID = "quoteId"
}


