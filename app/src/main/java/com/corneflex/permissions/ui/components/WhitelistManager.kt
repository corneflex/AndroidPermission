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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.corneflex.permissions.model.AppInfo
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel.PlayStoreFilterMode
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel.SystemAppFilterMode

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
    
    // Play Store filter states
    val isPlayStoreFilterActive by viewModel.playStoreFilterActive.observeAsState(initial = false)
    val playStoreFilterMode by viewModel.playStoreFilterMode.observeAsState(initial = PlayStoreFilterMode.SHOW_ONLY_PLAY_STORE)
    
    // System App filter states
    val isSystemAppFilterActive by viewModel.isSystemAppFilterActive.observeAsState(initial = false)
    val systemAppFilterMode by viewModel.systemAppFilterMode.observeAsState(initial = SystemAppFilterMode.EXCLUDE_SYSTEM_APPS)
    
    // State for new regex pattern input
    var newPattern by remember { mutableStateOf("") }
    var showRegexPatterns by remember { mutableStateOf(false) }
    
    // Section visibility states
    var showWhitelistSection by remember { mutableStateOf(true) }
    var showPlayStoreSection by remember { mutableStateOf(false) }
    var showSystemAppSection by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter Manager",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Control which applications are shown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter section toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { 
                        showWhitelistSection = true
                        showPlayStoreSection = false
                        showSystemAppSection = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Whitelist",
                        color = if (showWhitelistSection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                TextButton(
                    onClick = { 
                        showWhitelistSection = false 
                        showPlayStoreSection = true
                        showSystemAppSection = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Play Store",
                        color = if (showPlayStoreSection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                TextButton(
                    onClick = { 
                        showWhitelistSection = false
                        showPlayStoreSection = false
                        showSystemAppSection = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "System Apps",
                        color = if (showSystemAppSection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // WHITELIST SECTION
            if (showWhitelistSection) {
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
            
            // PLAY STORE SECTION
            if (showPlayStoreSection) {
                Text(
                    text = "Play Store Filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Filter apps based on installation source",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filter toggle with clickable text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.togglePlayStoreFilter(!isPlayStoreFilterActive) }
                ) {
                    Checkbox(
                        checked = isPlayStoreFilterActive,
                        onCheckedChange = null // Handled by the row's clickable modifier
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Enable Play Store filtering")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Filter mode selection
                if (isPlayStoreFilterActive) {
                    Text(
                        text = "Filter Mode:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Show only Play Store apps (clickable row)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            viewModel.setPlayStoreFilterMode(PlayStoreFilterMode.SHOW_ONLY_PLAY_STORE) 
                        }
                    ) {
                        RadioButton(
                            selected = playStoreFilterMode == PlayStoreFilterMode.SHOW_ONLY_PLAY_STORE,
                            onClick = null // Handled by the row's clickable modifier
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text("Show only Play Store apps")
                    }
                    
                    // Exclude Play Store apps (clickable row)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            viewModel.setPlayStoreFilterMode(PlayStoreFilterMode.EXCLUDE_PLAY_STORE) 
                        }
                    ) {
                        RadioButton(
                            selected = playStoreFilterMode == PlayStoreFilterMode.EXCLUDE_PLAY_STORE,
                            onClick = null // Handled by the row's clickable modifier
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text("Show only non-Play Store apps")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Information about Play Store apps
                    Text(
                        text = "Apps from the Play Store typically undergo Google's security screening process.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // SYSTEM APP SECTION
            if (showSystemAppSection) {
                Text(
                    text = "System App Filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Filter system applications installed by the manufacturer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable system app filtering",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isSystemAppFilterActive,
                        onCheckedChange = { viewModel.toggleSystemAppFilter() }
                    )
                }

                if (isSystemAppFilterActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Filter Mode:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                    ) {
                        // Show only system apps option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = systemAppFilterMode == SystemAppFilterMode.SHOW_ONLY_SYSTEM_APPS,
                                    onClick = { viewModel.setSystemAppFilterMode(SystemAppFilterMode.SHOW_ONLY_SYSTEM_APPS) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = systemAppFilterMode == SystemAppFilterMode.SHOW_ONLY_SYSTEM_APPS,
                                onClick = null // Handled by parent row
                            )
                            Text(
                                text = "Show only system apps",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Exclude system apps option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = systemAppFilterMode == SystemAppFilterMode.EXCLUDE_SYSTEM_APPS,
                                    onClick = { viewModel.setSystemAppFilterMode(SystemAppFilterMode.EXCLUDE_SYSTEM_APPS) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = systemAppFilterMode == SystemAppFilterMode.EXCLUDE_SYSTEM_APPS,
                                onClick = null // Handled by parent row
                            )
                            Text(
                                text = "Exclude system apps",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "System apps are pre-installed by the device manufacturer or Google.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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