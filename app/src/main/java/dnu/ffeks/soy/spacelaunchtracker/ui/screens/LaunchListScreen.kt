package dnu.ffeks.soy.spacelaunchtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dnu.ffeks.soy.spacelaunchtracker.R
import dnu.ffeks.soy.spacelaunchtracker.data.network.SpaceLaunch
import dnu.ffeks.soy.spacelaunchtracker.ui.components.LaunchCard
import dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel.SpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchListScreen(
    viewModel: SpaceViewModel,
    launches: List<SpaceLaunch>,
    followedIds: Set<String>,
    onToggleFollow: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    var displayLimit by rememberSaveable { mutableIntStateOf(20) }

    val displayedLaunches = launches.take(displayLimit)
    val canLoadMore = displayLimit < launches.size

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadAllData() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, contentDescription = stringResource(id = R.string.filter_title))
                    Spacer(Modifier.width(4.dp))
                    Text(if (showFilters) stringResource(id = R.string.filter_hide) else stringResource(id = R.string.filter_show))
                }
            }

            AnimatedVisibility(visible = showFilters) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                ) {
                    LaunchFilterPanel(viewModel = viewModel, onClose = { showFilters = false })
                }
            }

            if (launches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.filter_empty_list))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(displayedLaunches) { launch ->
                        LaunchCard(
                            launch = launch,
                            isFollowed = followedIds.contains(launch.id),
                            onFollowClick = { onToggleFollow(launch.id) },
                            onClick = { onNavigateToDetails(launch.id) }
                        )
                    }

                    if (canLoadMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(onClick = { displayLimit += 20 }) {
                                    Text(stringResource(id = R.string.btn_load_more))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LaunchFilterPanel(viewModel: SpaceViewModel, onClose: () -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedCrewed by viewModel.selectedCrewed.collectAsState()
    val selectedRocket by viewModel.selectedRocket.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    val locationsList by viewModel.availableLocations.collectAsState()
    val rocketsList by viewModel.availableRockets.collectAsState()
    val providersList by viewModel.availableProviders.collectAsState()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            label = { Text(stringResource(id = R.string.filter_name_search)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DynamicFilterDropdown(
                    label = stringResource(id = R.string.filter_location),
                    selectedValue = selectedLocation,
                    options = locationsList,
                    onValueChange = { viewModel.selectedLocation.value = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DynamicFilterDropdown(
                    label = stringResource(id = R.string.filter_crewed),
                    selectedValue = selectedCrewed,
                    options = listOf("Yes", "No"),
                    onValueChange = { viewModel.selectedCrewed.value = it }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DynamicFilterDropdown(
                    label = stringResource(id = R.string.filter_rocket_config),
                    selectedValue = selectedRocket,
                    options = rocketsList,
                    onValueChange = { viewModel.selectedRocket.value = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DynamicFilterDropdown(
                    label = stringResource(id = R.string.filter_provider),
                    selectedValue = selectedProvider,
                    options = providersList,
                    onValueChange = { viewModel.selectedProvider.value = it }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.resetFilters() }) {
                Text(stringResource(id = R.string.filter_btn_reset))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onClose) {
                Text(stringResource(id = R.string.filter_btn_apply))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFilterDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("ALL") },
                onClick = { onValueChange("ALL"); expanded = false }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false }
                )
            }
        }
    }
}