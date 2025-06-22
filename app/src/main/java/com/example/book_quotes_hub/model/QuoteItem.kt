package com.example.book_quotes_hub.model

import com.example.book_quotes_hub.db.Quote

// This will be your UI Model (DTO - Data Transfer Object for the UI layer)
data class QuoteItem(
    val localId:Int,
    val id: Int, // Maps to the 'id' from your network/Room Quote entity
    val category: String,
    val quote: String, // The actual quote text
    val author: String,
    val description: String,
    val content: String // For consistency with your previous detail screen, 'content' will just be 'quote'
) {
    // A mapping function to convert your Room/Network Quote entity to QuoteItem
    companion object {
        fun fromQuoteEntity(quote: Quote): QuoteItem {
            return QuoteItem(
                localId = quote.localId,
                id = quote.id,
                category = quote.category,
                quote = quote.quote,
                author = quote.author,
                description = quote.description,
                content = quote.quote // 'content' in UI model is the 'quote' from entity
            )
        }
    }
}