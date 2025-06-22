package com.example.book_quotes_hub.network

import com.example.book_quotes_hub.db.Quote
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query


data class QuoteApiResponse(
    @SerializedName("message") // Matches the "message" key in JSON
    val message: String,
    @SerializedName("Quotes")  // Matches the "Quotes" array key in JSON
    val quotes: List<Quote>?
)

interface QuoteApiService {
    @Headers(
        "x-rapidapi-key: 01bf8f577dmsh8b8acf9571b4088p1a0b79jsnc1fcc9dac7b9",
        "x-rapidapi-host: get-quotes-api.p.rapidapi.com"
    )
    @GET("quotes")
    suspend fun getQuotes(): QuoteApiResponse // For all quotes

    @Headers(
        "x-rapidapi-key: 01bf8f577dmsh8b8acf9571b4088p1a0b79jsnc1fcc9dac7b9",
        "x-rapidapi-host: get-quotes-api.p.rapidapi.com"
    )
    @GET("category/{categoryName}") // Endpoint for category
    suspend fun getQuotesByCategory(@Path("categoryName") categoryName: String): QuoteApiResponse
}