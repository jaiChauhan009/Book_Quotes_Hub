package com.example.book_quotes_hub.view.quote


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh // Changed from Info to Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.book_quotes_hub.db.Quote
import com.example.book_quotes_hub.model.QuoteItem
import com.example.book_quotes_hub.utils.NetworkUtils
import com.example.bookhub.viewmodel.QuoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteListScreen(
    navController: NavController, // Use hiltViewModel for injection
    onQuoteClick: (String) -> Unit
) {
    val quoteViewModel: QuoteViewModel = hiltViewModel()
    val quotes by quoteViewModel.quotes.collectAsState()
    val isLoading by quoteViewModel.isLoading.collectAsState()
    val errorMessage by quoteViewModel.errorMessage.collectAsState()
    val selectedCategory by quoteViewModel.selectedCategory.collectAsState()

    val listState = rememberLazyGridState() // Use rememberLazyGridState for LazyVerticalGrid
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            // Trigger load more when user scrolls to within 10 items of the end
            // (considering a 5-column grid, this is roughly 2 rows from the end)
            lastVisibleItemIndex != null && lastVisibleItemIndex >= quotes.size - (5 * 2) && !isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            quoteViewModel.loadNextPage()
        }
    }

    val categories = listOf("All Quotes", "Motivation", "Courage", "Nature", "Love")
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daily Quotes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { quoteViewModel.refreshQuotes() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh quotes")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply scaffold padding
        ) {
            // Network Status Indicator
            val isOnline = NetworkUtils.isInternetAvailable(context)
            Text(
                text = if (isOnline) "Online. Fetching new random quotes." else "No internet. Loading from cache/offline.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            // Category Selection Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { category ->
                    val isSelected = when (category) {
                        "All Quotes" -> selectedCategory == null
                        else -> selectedCategory == category
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newCategory = if (category == "All Quotes") null else category
                            quoteViewModel.selectCategory(newCategory)
                        },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors( // <--- CORRECTED HERE
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Initial loading indicator (when list is empty)
            if (isLoading && quotes.isEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Error message display
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }

            // Empty state message
            if (quotes.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No quotes available for ${selectedCategory ?: "All Quotes"}. Tap 'Refresh' to fetch some.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5), // Five-column grid as requested
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(quotes, key = { _, quote -> quote.localId }) { index, quote -> // THIS IS THE FIX!
                        QuoteCard(quote = quote) {
                            onQuoteClick(quote.id.toString()) // Pass the quote ID (Int) as a String
                        }
                    }
                    // Loading indicator at the bottom of the list
                    if (isLoading && quotes.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
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
fun QuoteCard(quote: QuoteItem, onClick: (QuoteItem) -> Unit) { // Use QuoteItem here
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(quote) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp), // Slightly more compact for 5-column grid
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"${quote.content}\"", // QuoteItem's content
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center // Center text in grid card
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "- ${quote.author}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, // Center author in grid card
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

