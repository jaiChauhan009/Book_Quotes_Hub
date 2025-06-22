package com.example.book_quotes_hub.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.book_quotes_hub.ui.theme.Typography
import com.example.book_quotes_hub.ui.theme.text // Assuming you have a 'text' color in your theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.ImeAction


@Composable
fun LabelView(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.displayMedium, // Use MaterialTheme.typography
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.inversePrimary, // Use MaterialTheme.colorScheme
        modifier = modifier
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Default), // Default to ImeAction.Default
    keyboardActions: KeyboardActions = KeyboardActions.Default // Default keyboard actions
) {
    // If you need specific keyboard control, uncomment and use LocalSoftwareKeyboardController here
    // val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) }, // Simple Text for label, LabelView is also an option if you want to reuse its styling.
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Consistent horizontal padding
        singleLine = true, // Often true for general text inputs
        colors = OutlinedTextFieldDefaults.colors( // Use Material3 TextFieldDefaults for colors
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surface, // Container background
            unfocusedContainerColor = MaterialTheme.colorScheme.surface, // Container background
            // Input text colors
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            // If you use placeholder, configure focusedPlaceholderColor, unfocusedPlaceholderColor
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

// The `textFieldColors` composable is no longer needed as colors are set directly in TextInputField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() = TextFieldDefaults.colors( // Use TextFieldDefaults.colors for Material3
    focusedTextColor   = text, // Changed to 'text' color for input, keeps it consistent
    unfocusedTextColor = text, // Ensure unfocused text also uses 'text' color
    focusedLabelColor = colorScheme.primary,
    unfocusedLabelColor = Color.LightGray, // For unfocused label
    focusedIndicatorColor = colorScheme.primary,
    unfocusedIndicatorColor = Color.LightGray,
    cursorColor = colorScheme.primary,
    // Placeholder colors:
    focusedPlaceholderColor  = colorScheme.onSurfaceVariant, // Use a variant for placeholder
    unfocusedPlaceholderColor = colorScheme.onSurfaceVariant,
    disabledPlaceholderColor = colorScheme.onSurfaceVariant
    // Removed focusedPlaceholderColor, disabledPlaceholderColor as they're now part of TextFieldDefaults.colors
    // You might also want to set containerColor to Color.Transparent or a specific surface color
    // containerColor = Color.Transparent,
)