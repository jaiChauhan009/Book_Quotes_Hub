package com.example.book_quotes_hub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel // Correct import for hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.book_quotes_hub.view.book.BookDetailsScreen
import com.example.book_quotes_hub.view.book.BookListScreen
import com.example.bookhub.viewmodel.MainViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.book_quotes_hub.view.quote.QuoteDetailScreen
import com.example.bookhub.viewmodel.QuoteViewModel
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.book_quotes_hub.view.quote.QuoteListScreen

// import com.example.bookhub.MainActivity // Not directly needed in navigation composables


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(rootNavController: NavController) {
    val pagerState = rememberPagerState(pageCount = {
        AppScreen::class.sealedSubclasses.size
    })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        AppScreen.Books,
        AppScreen.Quotes
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                pages.forEachIndexed { index, screen ->
                    val isSelected = selectedTabIndex == index

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.resourceId)) },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = isSelected,
                        onClick = {
                            selectedTabIndex = index
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (pages[page]) {
                AppScreen.Books -> BookNavHost(rootNavController)
                AppScreen.Quotes -> QuoteNavHost(rootNavController)
            }
        }
    }
}

// BookNavHost - No changes needed here based on previous discussion, but including for completeness
@Composable
fun BookNavHost(rootNavController: NavController) {
    val bookNavController = rememberNavController()
    // No explicit viewModel passed to BookListScreen/BookDetailsScreen here
    // as they should obtain it via hiltViewModel() internally if they are HiltViewModels.
    // If MainViewModel is NOT a HiltViewModel, then passing it like this might be correct.
    // Assuming BookListScreen/BookDetailsScreen internally use hiltViewModel() for MainViewModel.
    // If not, and MainViewModel is a regular ViewModel, then it needs to be provided.
    // For consistency with QuoteViewModel, assuming MainViewModel also uses hiltViewModel() internally.

    NavHost(bookNavController, startDestination = BookScreens.List.route) {
        composable(BookScreens.List.route) {
            // If BookListScreen takes viewModel: MainViewModel = hiltViewModel(),
            // then remove the 'viewModel = bookViewModel' here.
            // If BookListScreen does NOT take hiltViewModel() internally,
            // and MainViewModel is NOT a HiltViewModel, then this is correct.
            // Assuming it behaves like QuoteScreen, so:
            val bookViewModel: MainViewModel = hiltViewModel() // Get it once here for BookNavHost scope
            BookListScreen(viewModel = bookViewModel, onBookClick = { isbnNo ->
                bookNavController.navigate(BookScreens.Details.createRoute(isbnNo))
            })
        }
        composable(
            BookScreens.Details.route,
            arguments = listOf(navArgument(EndPoints.ISBN_NO) { type = NavType.StringType })
        ) { backStackEntry ->
            val isbnNo = backStackEntry.arguments?.getString(EndPoints.ISBN_NO)
                ?: throw IllegalStateException("'Book ISBN No' shouldn't be null")
            // Same assumption for BookDetailsScreen
            val bookViewModel: MainViewModel = hiltViewModel() // Get it again for this composable's scope
            BookDetailsScreen(viewModel = bookViewModel, isbnNo = isbnNo, onBackClick = { bookNavController.popBackStack() })
        }
    }
}

// QuoteNavHost - CORRECTED
@Composable
fun QuoteNavHost(rootNavController: NavController) {
    val quoteNavController = rememberNavController()

    NavHost(quoteNavController, startDestination = QuoteScreens.List.route) {
        composable(QuoteScreens.List.route) {
            QuoteListScreen(
                navController = quoteNavController, // Pass navController as it's a required parameter
                onQuoteClick = { quoteId ->
                    quoteNavController.navigate(QuoteScreens.Details.createRoute(quoteId.toString()))
                }
            )
        }
        composable(
            QuoteScreens.Details.route,
            arguments = listOf(navArgument(EndPoints.QUOTE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getInt(EndPoints.QUOTE_ID)
                ?: throw IllegalStateException("'Quote ID' shouldn't be null")
            QuoteDetailScreen(
                navController = quoteNavController,
                quoteId = quoteId.toString() // Correctly convert Int from navArgument to String for QuoteDetailScreen
            )
        }
    }
}