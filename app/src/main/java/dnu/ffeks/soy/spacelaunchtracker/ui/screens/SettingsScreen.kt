package dnu.ffeks.soy.spacelaunchtracker.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.imageLoader
import dnu.ffeks.soy.spacelaunchtracker.R
import dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val is24hReminderEnabled by viewModel.is24hReminderEnabled.collectAsState()
    val is1hReminderEnabled by viewModel.is1hReminderEnabled.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggle24hReminder(true)
            viewModel.toggle1hReminder(true)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.settings_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.settings_appearance), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.settings_dark_mode), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(id = R.string.settings_dark_mode_desc), style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.settings_dynamic_colors), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(id = R.string.settings_dynamic_colors_desc), style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = useDynamicColors, onCheckedChange = { viewModel.toggleDynamicColors(it) })
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(id = R.string.settings_notifications), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.settings_launch_reminders_24h), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(id = R.string.settings_launch_reminders_24h_desc), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = is24hReminderEnabled,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.toggle24hReminder(checked)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.settings_launch_reminders_1h), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(id = R.string.settings_launch_reminders_1h_desc), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = is1hReminderEnabled,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.toggle1hReminder(checked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(id = R.string.settings_data), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            val cacheClearedMessage = stringResource(id = R.string.settings_cache_cleared)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.imageLoader.diskCache?.clear()
                        context.imageLoader.memoryCache?.clear()
                        Toast.makeText(context, cacheClearedMessage, Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.settings_clear_cache), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(id = R.string.settings_clear_cache_desc), style = MaterialTheme.typography.bodySmall)
                }
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.settings_clear_cache),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(id = R.string.settings_about), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(id = R.string.settings_about_app_name), fontWeight = FontWeight.Bold)
                    Text(stringResource(id = R.string.settings_about_version), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.settings_about_desc), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}