package com.example.book_quotes_hub.view.quote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.book_quotes_hub.model.QuoteItem
import com.example.book_quotes_hub.utils.NetworkUtils
import com.example.bookhub.viewmodel.QuoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryQuotesScreen( // This was previously named QuoteListScreen in the confusing example.
    // I'm assuming you mean *this* screen when you say "categoryquotescreen"
    // because it's the one showing quotes and has the category concept.
    navController: NavController,
    category: String?, // This parameter should already be here from your existing setup
    onQuoteClick: (String) -> Unit
) {
    val quoteViewModel: QuoteViewModel = hiltViewModel()
    val quotes by quoteViewModel.quotes.collectAsState()
    val isLoading by quoteViewModel.isLoading.collectAsState()
    val errorMessage by quoteViewModel.errorMessage.collectAsState()

    // CHANGE: Use rememberLazyListState for LazyColumn
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            // Adjust calculation for a single-column LazyColumn (if you had 5 rows before, now it's just 5 items)
            lastVisibleItemIndex != null && lastVisibleItemIndex >= quotes.size - 5 && !isLoading
        }
    }

    LaunchedEffect(category) {
        // Trigger a new load whenever the category changes
        quoteViewModel.selectCategory(category)
        // Optionally reset scroll position when category changes
        if (quotes.isNotEmpty()) { // Only scroll if there are items to prevent error on empty list
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            quoteViewModel.loadNextPage()
        }
    }

    val context = LocalContext.current
    val isOnline = NetworkUtils.isInternetAvailable(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${category ?: "All"} Quotes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
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
                .padding(paddingValues)
        ) {
            Text(
                text = if (isOnline) "Online. Fetching new random quotes." else "No internet. Loading from cache/offline.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            if (isLoading && quotes.isEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }

            if (quotes.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No quotes available for ${category ?: "All Quotes"}. Tap 'Refresh' to fetch some.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // CHANGE: Replaced LazyVerticalGrid with LazyColumn
                LazyColumn(
                    state = listState, // Use LazyColumn's state
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // CHANGE: Use itemsIndexed for LazyColumn
                    itemsIndexed(quotes, key = { _, quote -> quote.localId }) { index, quote ->
                        QuoteCard(quote = quote) {
                            onQuoteClick(quote.id.toString())
                        }
                    }
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
fun QuoteCard(quote: QuoteItem, onClick: (QuoteItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(quote) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"${quote.content}\"",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "- ${quote.author}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}