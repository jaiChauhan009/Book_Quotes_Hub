package com.example.book_quotes_hub.view.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Recommended for lifecycle-aware collection
import com.example.book_quotes_hub.R
import com.example.book_quotes_hub.utils.DetailViewState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage // Import your custom modifier
import com.example.bookhub.viewmodel.MainViewModel
import androidx.compose.ui.layout.ContentScale // For AsyncImage content scale
import androidx.compose.ui.text.font.FontWeight
import com.example.book_quotes_hub.components.AppBar
import com.example.book_quotes_hub.utils.coloredShadow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.book_quotes_hub.db.BookItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(viewModel: MainViewModel, isbnNo: String, onBackClick: () -> Unit) {
    val detailViewState by viewModel.bookDetails.collectAsState()

    LaunchedEffect(isbnNo) {
        viewModel.getBookByID(isbnNo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to books list")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (detailViewState) {
                is DetailViewState.Loading -> {
                    CircularProgressIndicator()
                    Text(text = "Loading book details...", modifier = Modifier.padding(top = 80.dp))
                }
                is DetailViewState.Error -> {
                    val error = (detailViewState as DetailViewState.Error).exception
                    Text(
                        text = "Error: ${error.localizedMessage ?: "Unknown error"}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
                is DetailViewState.Empty -> {
                    Text(
                        text = "Book details not found.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = Color.Gray
                    )
                }
                is DetailViewState.Success -> {
                    val book = (detailViewState as DetailViewState.Success).data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Book Thumbnail at the top
                        book.thumbnailUrl?.let { url ->
                            Card(
                                modifier = Modifier
                                    .size(180.dp, 240.dp) // Larger size for detail view
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "${book.title} thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.placeholder_book_thumbnail)
                                )
                            }
                        } ?: run {
                            // Placeholder if no thumbnail URL
                            Image(
                                painter = painterResource(id = R.drawable.placeholder_book_thumbnail),
                                contentDescription = "No book thumbnail available",
                                modifier = Modifier
                                    .size(180.dp, 240.dp)
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                                alpha = 0.5f // Make it slightly transparent
                            )
                        }


                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "by ${book.authors.joinToString()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Categories: ${book.categories.joinToString()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ISBN: ${book.isbn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Page Count: ${book.pageCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Status: ${book.status}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                book.shortDescription?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Short Description:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                book.longDescription?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Long Description:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
fun TopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}




@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
        Text(text = "Loading books...", modifier = Modifier.padding(top = 80.dp))
    }
}

@Composable
private fun ErrorState(message: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Error found: ${message ?: "Unknown error"}",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No books found!", modifier = Modifier.padding(20.dp))
    }
}

@Composable
fun BookDetails(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val result by viewModel.bookDetails.collectAsStateWithLifecycle()

    Column(modifier = modifier) { // Apply modifier passed from parent
        when (result) {
            DetailViewState.Loading -> LoadingState()
            is DetailViewState.Error -> ErrorState((result as DetailViewState.Error).exception.localizedMessage)
            is DetailViewState.Success -> {
                val book = (result as DetailViewState.Success).data
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        BookDetailsCard(book)
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.text_description), // Assuming this string resource exists
                            style = MaterialTheme.typography.titleLarge, // Use MaterialTheme.typography
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 20.dp) // Consistent padding
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = book.longDescription.toString(),
                            style = MaterialTheme.typography.bodyLarge, // Use MaterialTheme.typography
                            textAlign = TextAlign.Justify,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F),
                            modifier = Modifier.padding(horizontal = 20.dp) // Consistent padding
                        )
                    }
                }
            }
            DetailViewState.Empty -> EmptyState()
        }
    }
}

@Composable
fun BookDetailsCard(book: BookItem) { // Simplified to take BookItem directly
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .coloredShadow(
                color = MaterialTheme.colorScheme.primary,
                borderRadius = 12.dp,
                shadowRadius = 20.dp,
                offsetY = 10.dp
            ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = "Book Cover",
                modifier = Modifier
                    .size(180.dp, 240.dp)
                    .coloredShadow(
                        color = MaterialTheme.colorScheme.secondary,
                        borderRadius = 8.dp,
                        shadowRadius = 10.dp,
                        offsetY = 5.dp
                    ),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineSmall, // Use MaterialTheme.typography
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "by ${book.authors.joinToString()}",
                style = MaterialTheme.typography.titleMedium, // Use MaterialTheme.typography
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Category: ${book.categories.joinToString()}",
                style = MaterialTheme.typography.bodyLarge, // Use MaterialTheme.typography
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = "Pages: ${book.pageCount}",
                style = MaterialTheme.typography.bodyLarge, // Use MaterialTheme.typography
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = "ISBN: ${book.isbn}",
                style = MaterialTheme.typography.bodyLarge, // Use MaterialTheme.typography
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

