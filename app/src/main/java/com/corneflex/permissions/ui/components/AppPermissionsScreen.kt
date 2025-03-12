package com.corneflex.permissions.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corneflex.permissions.model.DangerLevel
import com.corneflex.permissions.model.PermissionGroup
import com.corneflex.permissions.ui.components.WhitelistManagerCard
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPermissionsScreen(
    viewModel: AppPermissionsViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsByDangerLevel by viewModel.permissionsByDangerLevel.observeAsState(emptyMap())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val isSearchActive by viewModel.isSearchActive.observeAsState(initial = false)
    val searchResults by viewModel.searchResults.observeAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.observeAsState(initial = "")
    
    // Filter toggle
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // Scrolling state
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    
    // Animation state for filter menu
    val filterMenuVisibleState = remember { MutableTransitionState(false) }
    filterMenuVisibleState.targetState = showFilterMenu
    
    // Track scroll position changes and close filter menu with animation when scrolling starts
    val isScrolling by remember { derivedStateOf { scrollState.isScrollInProgress } }
    LaunchedEffect(isScrolling) {
        if (isScrolling && showFilterMenu) {
            showFilterMenu = false
        }
    }
    
    // Load data when the screen is first shown
    DisposableEffect(lifecycleOwner) {
        viewModel.loadInstalledApps()
        onDispose { }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Permissions") },
                actions = {
                    // Combined filter menu button
                    IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Filter Options"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Main content column that's scrollable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // Handle taps outside the filter menu
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (showFilterMenu) {
                            showFilterMenu = false
                            focusManager.clearFocus()
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Single filter card (only shown when toggled) with animation
                AnimatedVisibility(
                    visibleState = filterMenuVisibleState,
                    enter = slideInVertically(
                        initialOffsetY = { -it }, // Slide in from top
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(150)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it }, // Slide out to top
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Prevent clicks from passing through the filter menu
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { /* consume the click */ })
                            }
                    ) {
                        WhitelistManagerCard(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
                
                // Search bar
                PermissionSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchPermissions(it) },
                    onClearSearch = { viewModel.clearSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                if (isLoading) {
                    LoadingScreen()
                } else if (isSearchActive) {
                    // Show search results
                    SearchResultsScreen(
                        searchResults = searchResults,
                        searchQuery = searchQuery
                    )
                } else {
                    // Show categorized permissions
                    PermissionsTabs(
                        permissionsByDangerLevel = permissionsByDangerLevel,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var active by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    SearchBar(
        query = searchQuery,
        onQueryChange = onSearchQueryChange,
        onSearch = {
            active = false
            keyboardController?.hide()
        },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Search permissions, apps or package IDs...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = onClearSearch) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        modifier = modifier
    ) {
        // Search suggestions
        if (active) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Search for:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "• Permission names (e.g., 'camera', 'location')",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Text(
                    text = "• App names (e.g., 'WhatsApp', 'Chrome')",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Text(
                    text = "• Package IDs (e.g., 'com.android', 'org.mozilla')",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SearchResultsScreen(
    searchResults: List<PermissionGroup>,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Search results count
        Text(
            text = "${searchResults.size} result${if (searchResults.size != 1) "s" else ""} for \"$searchQuery\"",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (searchResults.isEmpty()) {
            // No results found
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matches found for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Display search results without using weight
            PermissionGroupsList(
                permissionGroups = searchResults,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun PermissionsTabs(
    permissionsByDangerLevel: Map<DangerLevel, List<PermissionGroup>>,
    modifier: Modifier = Modifier
) {
    // Skip empty categories
    val nonEmptyCategories = permissionsByDangerLevel.filter { it.value.isNotEmpty() }
    val tabTitles = nonEmptyCategories.keys.map { it.name.lowercase().capitalize() }
    
    if (tabTitles.isEmpty()) {
        EmptyScreen(modifier = modifier)
        return
    }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val keys = nonEmptyCategories.keys.toList()
    
    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Show the content for the selected tab
        if (selectedTabIndex < keys.size) {
            val selectedDangerLevel = keys[selectedTabIndex]
            val permissionGroups = nonEmptyCategories[selectedDangerLevel] ?: emptyList()
            
            // Use Box instead of weight to prevent issues with scrolling
            Box(modifier = Modifier.fillMaxWidth()) {
                PermissionGroupsList(
                    permissionGroups = permissionGroups,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun EmptyScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No permissions found")
    }
}

private fun String.capitalize(): String {
    return if (this.isNotEmpty()) {
        this.first().uppercase() + this.substring(1)
    } else {
        this
    }
} 