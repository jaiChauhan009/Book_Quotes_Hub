package com.example.book_quotes_hub.view.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Recommended for lifecycle-aware collection
import com.example.book_quotes_hub.R
import com.example.book_quotes_hub.components.ItemBookList
import com.example.book_quotes_hub.components.TextInputField
import com.example.book_quotes_hub.utils.ViewState
import com.example.bookhub.viewmodel.MainViewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.book_quotes_hub.db.BookItem

// The main composable for the list screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(viewModel: MainViewModel, onBookClick: (String) -> Unit) {
    val viewState by viewModel.books.collectAsState() // Overall loading/error state
    val filteredBooks by viewModel.filteredBooks.collectAsState() // The list to display (filtered)
    val searchQuery by viewModel.searchQuery.collectAsState() // Current search query

    // Ensure books are loaded when screen is entered or refreshed
    LaunchedEffect(Unit) {
        viewModel.getAllBooks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BookHub Books") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.refreshBooks() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh books")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Align content to top
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search books by title or author") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (viewState) {
                is ViewState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Text("Loading books...", modifier = Modifier.padding(top = 16.dp))
                }
                is ViewState.Error -> {
                    val error = (viewState as ViewState.Error).exception
                    Text(
                        text = "Error: ${error.localizedMessage ?: "Unknown error"}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                    // Optionally, add a retry button
                    Button(onClick = { viewModel.getAllBooks() }) {
                        Text("Retry")
                    }
                }
                is ViewState.Empty -> {
                    Text(
                        text = "No books available. Tap refresh to load.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = Color.Gray
                    )
                }
                is ViewState.Success -> {
                    if (filteredBooks.isEmpty()) {
                        Text(
                            text = "No books found matching your search.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(filteredBooks, key = { it.isbn }) { book ->
                                BookCard(book = book) {
                                    onBookClick(book.isbn)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(book: BookItem, onClick: (BookItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(book) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Thumbnail
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = "${book.title} thumbnail",
                modifier = Modifier
                    .size(90.dp, 120.dp) // Maintain aspect ratio for books
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop, // Crop to fill bounds
                error = painterResource(id = R.drawable.placeholder_book_thumbnail) // Placeholder for error
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "by ${book.authors.joinToString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.shortDescription ?: "No description available.",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


