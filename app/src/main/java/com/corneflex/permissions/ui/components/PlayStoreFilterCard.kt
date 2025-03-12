package com.corneflex.permissions.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel

@Composable
fun PlayStoreFilterCard(
    viewModel: AppPermissionsViewModel,
    modifier: Modifier = Modifier
) {
    val isPlayStoreFilterActive by viewModel.playStoreFilterActive.observeAsState(initial = false)
    val playStoreFilterMode by viewModel.playStoreFilterMode.observeAsState(initial = AppPermissionsViewModel.PlayStoreFilterMode.SHOW_ONLY_PLAY_STORE)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Play Store Filter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
                        viewModel.setPlayStoreFilterMode(AppPermissionsViewModel.PlayStoreFilterMode.SHOW_ONLY_PLAY_STORE) 
                    }
                ) {
                    RadioButton(
                        selected = playStoreFilterMode == AppPermissionsViewModel.PlayStoreFilterMode.SHOW_ONLY_PLAY_STORE,
                        onClick = null // Handled by the row's clickable modifier
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Show only Play Store apps")
                }
                
                // Exclude Play Store apps (clickable row)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { 
                        viewModel.setPlayStoreFilterMode(AppPermissionsViewModel.PlayStoreFilterMode.EXCLUDE_PLAY_STORE) 
                    }
                ) {
                    RadioButton(
                        selected = playStoreFilterMode == AppPermissionsViewModel.PlayStoreFilterMode.EXCLUDE_PLAY_STORE,
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
    }
} 