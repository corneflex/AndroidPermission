package com.corneflex.permissions.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.corneflex.permissions.model.AppInfo
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistManagerCard(
    viewModel: AppPermissionsViewModel,
    modifier: Modifier = Modifier
) {
    val isWhitelistActive by viewModel.isWhitelistFilterActive.observeAsState(initial = false)
    val whitelistMode by viewModel.whitelistFilterMode.observeAsState(initial = AppPermissionsViewModel.WhitelistFilterMode.SHOW_ONLY_WHITELISTED)
    val whitelistedApps by viewModel.whitelistedApps.observeAsState(initial = emptySet())
    val regexPatterns by viewModel.regexPatterns.observeAsState(initial = emptySet())
    
    // State for new regex pattern input
    var newPattern by remember { mutableStateOf("") }
    var showRegexPatterns by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Whitelist Filter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Filter apps based on a predefined whitelist",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Whitelist toggle with clickable text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.toggleWhitelistFilter(!isWhitelistActive) }
            ) {
                Checkbox(
                    checked = isWhitelistActive,
                    onCheckedChange = null // Handled by the row's clickable modifier
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Enable whitelist filtering")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Whitelist mode selection
            if (isWhitelistActive) {
                Text(
                    text = "Filter Mode:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Show only whitelisted apps (clickable row)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { 
                        viewModel.setWhitelistFilterMode(AppPermissionsViewModel.WhitelistFilterMode.SHOW_ONLY_WHITELISTED)
                    }
                ) {
                    RadioButton(
                        selected = whitelistMode == AppPermissionsViewModel.WhitelistFilterMode.SHOW_ONLY_WHITELISTED,
                        onClick = null // Handled by the row's clickable modifier
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Show only whitelisted apps")
                }
                
                // Exclude whitelisted apps (clickable row)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { 
                        viewModel.setWhitelistFilterMode(AppPermissionsViewModel.WhitelistFilterMode.EXCLUDE_WHITELISTED)
                    }
                ) {
                    RadioButton(
                        selected = whitelistMode == AppPermissionsViewModel.WhitelistFilterMode.EXCLUDE_WHITELISTED,
                        onClick = null // Handled by the row's clickable modifier
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Exclude whitelisted apps (blacklist mode)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Regex pattern input
                Text(
                    text = "Add Regex Pattern:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        label = { Text("Pattern (e.g., com.android.*)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (newPattern.isNotEmpty()) {
                                viewModel.addRegexPattern(newPattern)
                                newPattern = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add pattern")
                    }
                }
                
                Text(
                    text = "Examples: com.android.*, com\\.google\\..*",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Whitelist stats and controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "${whitelistedApps.size} apps in whitelist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "${regexPatterns.size} regex patterns defined",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(
                        onClick = { showRegexPatterns = !showRegexPatterns }
                    ) {
                        Text(if (showRegexPatterns) "Hide Patterns" else "Show Patterns")
                    }
                }
                
                // Show regex patterns list
                if (showRegexPatterns && regexPatterns.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Divider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Regex Patterns:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        items(regexPatterns.toList()) { pattern ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = pattern,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(
                                    onClick = { viewModel.removeRegexPattern(pattern) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove pattern",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Divider()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(
                        onClick = { viewModel.clearWhitelist() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }
    }
}

@Composable
fun AppWhitelistAction(
    app: AppInfo,
    viewModel: AppPermissionsViewModel,
    modifier: Modifier = Modifier
) {
    val whitelistedApps by viewModel.whitelistedApps.observeAsState(initial = emptySet())
    val isWhitelisted = app.packageName in whitelistedApps
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (isWhitelisted) {
            TextButton(onClick = { viewModel.removeFromWhitelist(app.packageName) }) {
                Text("Remove from Whitelist")
            }
        } else {
            TextButton(onClick = { viewModel.addToWhitelist(app.packageName) }) {
                Text("Add to Whitelist")
            }
        }
    }
} 