package com.example.book_quotes_hub.utils


import android.content.Context
import com.example.book_quotes_hub.data.repository.QuoteRepository
import com.example.book_quotes_hub.db.AppDatabase
import com.example.book_quotes_hub.db.BookDao
import com.example.book_quotes_hub.db.QuoteDao
import com.example.book_quotes_hub.network.QuoteApiService
import com.example.book_quotes_hub.repository.BookRepository
import com.example.book_quotes_hub.repository.BookRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // RapidAPI Headers
    private const val RAPID_API_KEY = "01bf8f577dmsh8b8acf9571b4088p1a0b79jsnc1fcc9dac7b9"
    private const val RAPID_API_HOST = "get-quotes-api.p.rapidapi.com"

    // Provides the Json instance for Kotlinx Serialization (for Books)
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json { ignoreUnknownKeys = true }
    }

    // Provides Gson instance for Retrofit (for Quotes)
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    // Provides OkHttpClient with headers for RapidAPI
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("x-rapidapi-key", RAPID_API_KEY)
                    .header("x-rapidapi-host", RAPID_API_HOST)
                val request = requestBuilder.build()
                chain.proceed(request)
            })
            .build()
    }

    // Provides the Retrofit API service for quotes.
    @Provides
    @Singleton
    fun provideQuoteApiService(okHttpClient: OkHttpClient, gson: Gson): QuoteApiService { // INJECT OkHttpClient and Gson here
        return Retrofit.Builder()
            .baseUrl("https://get-quotes-api.p.rapidapi.com/")
            .client(okHttpClient) // USE the injected OkHttpClient here
            .addConverterFactory(GsonConverterFactory.create(gson)) // Use GsonConverterFactory with provided Gson
            .build()
            .create(QuoteApiService::class.java)
    }

    // Provides the Room database instance
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // Provides the QuoteDao
    @Provides
    @Singleton
    fun provideQuoteDao(appDatabase: AppDatabase): QuoteDao {
        return appDatabase.quoteDao()
    }

    // Provides the BookDao
    @Provides
    @Singleton
    fun provideBookDao(appDatabase: AppDatabase): BookDao {
        return appDatabase.bookDao()
    }

    // Provides the QuoteRepository
    @Provides
    @Singleton
    fun provideQuoteRepository(
        apiService: QuoteApiService,
        quoteDao: QuoteDao,
        @ApplicationContext context: Context
    ): QuoteRepository {
        return QuoteRepository(apiService, quoteDao, context)
    }

    // Bind the BookRepository interface to its implementation
    @Module
    @InstallIn(SingletonComponent::class)
    interface BookRepositoryModule {
        @Binds
        @Singleton
        fun bindBookRepository(impl: BookRepositoryImpl): BookRepository
    }
}
