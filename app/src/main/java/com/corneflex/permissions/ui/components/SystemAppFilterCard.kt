package com.corneflex.permissions.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel
import com.corneflex.permissions.viewmodel.AppPermissionsViewModel.SystemAppFilterMode

@Composable
fun SystemAppFilterCard(viewModel: AppPermissionsViewModel) {
    val isSystemAppFilterActive by viewModel.isSystemAppFilterActive.observeAsState(initial = false)
    val systemAppFilterMode by viewModel.systemAppFilterMode.observeAsState(initial = SystemAppFilterMode.EXCLUDE_SYSTEM_APPS)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System App Filter",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isSystemAppFilterActive,
                    onCheckedChange = { viewModel.toggleSystemAppFilter() }
                )
            }

            if (isSystemAppFilterActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
                }
            }
        }
    }
} 