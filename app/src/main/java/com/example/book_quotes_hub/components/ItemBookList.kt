package com.example.book_quotes_hub.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ItemBookList(
    title: String,
    authors: List<String>,
    thumbnailUrl: String,
    categories: List<String>,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp), // Padding around the card
        shape = RoundedCornerShape(20.dp), // Apply shape to the card itself
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Set card's background color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface), // Explicitly set background for the row if needed, otherwise parent card handles it
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Book Thumbnail", // Added content description
                modifier = Modifier
                    .size(98.dp, 145.dp)
                    .padding(16.dp),
                contentScale = ContentScale.Inside
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = authors.joinToString(", "),
                    style = MaterialTheme.typography.displayMedium, // Use MaterialTheme.typography
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7F) // Use MaterialTheme.colorScheme
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, // Use MaterialTheme.typography
                    color = MaterialTheme.colorScheme.onSurface // Use MaterialTheme.colorScheme
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between chips horizontally
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between rows of chips vertically
                ) {
                    categories.forEach {
                        ChipView(category = it)
                    }
                }
            }
        }
    }
}

@Composable
fun ChipView(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10F)) // Use MaterialTheme.colorScheme
            .padding(horizontal = 12.dp, vertical = 5.dp), // Simplified padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.displayMedium, // Use MaterialTheme.typography
            color = MaterialTheme.colorScheme.primary // Use MaterialTheme.colorScheme
        )
    }
}