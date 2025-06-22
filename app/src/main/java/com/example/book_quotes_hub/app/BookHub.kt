package com.example.book_quotes_hub.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BookHub: Application(){
    override fun onCreate() {
        super.onCreate()
    }
}