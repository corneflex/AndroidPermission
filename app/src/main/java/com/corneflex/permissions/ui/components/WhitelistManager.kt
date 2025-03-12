package com.corneflex.permissions.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.corneflex.permissions.model.AppInfo
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel

@Composable
fun WhitelistManagerCard(
    viewModel: AppPermissionsViewModel,
    modifier: Modifier = Modifier
) {
    val isWhitelistActive by viewModel.isWhitelistFilterActive.observeAsState(initial = false)
    val whitelistMode by viewModel.whitelistFilterMode.observeAsState(initial = AppPermissionsViewModel.WhitelistFilterMode.SHOW_ONLY_WHITELISTED)
    val whitelistedApps by viewModel.whitelistedApps.observeAsState(initial = emptySet())
    
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
            
            // Whitelist toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isWhitelistActive,
                    onCheckedChange = { viewModel.toggleWhitelistFilter(it) }
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = whitelistMode == AppPermissionsViewModel.WhitelistFilterMode.SHOW_ONLY_WHITELISTED,
                        onClick = { 
                            viewModel.setWhitelistFilterMode(AppPermissionsViewModel.WhitelistFilterMode.SHOW_ONLY_WHITELISTED) 
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Show only whitelisted apps")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = whitelistMode == AppPermissionsViewModel.WhitelistFilterMode.EXCLUDE_WHITELISTED,
                        onClick = { 
                            viewModel.setWhitelistFilterMode(AppPermissionsViewModel.WhitelistFilterMode.EXCLUDE_WHITELISTED) 
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Exclude whitelisted apps (blacklist mode)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Whitelist stats
                Text(
                    text = "${whitelistedApps.size} apps in whitelist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    TextButton(
                        onClick = { viewModel.clearWhitelist() }
                    ) {
                        Text("Clear Whitelist")
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