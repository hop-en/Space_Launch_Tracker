package dnu.ffeks.soy.spacelaunchtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel.SpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(viewModel: SpaceViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedCrewed by viewModel.selectedCrewed.collectAsState()
    val selectedRocket by viewModel.selectedRocket.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    val locationsList by viewModel.availableLocations.collectAsState()
    val rocketsList by viewModel.availableRockets.collectAsState()
    val providersList by viewModel.availableProviders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            label = { Text("Name Search") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdown(
                    label = "Location",
                    selectedValue = selectedLocation,
                    options = locationsList,
                    onValueChange = { viewModel.selectedLocation.value = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdown(
                    label = "Crewed",
                    selectedValue = selectedCrewed,
                    options = listOf("Yes", "No"),
                    onValueChange = { viewModel.selectedCrewed.value = it }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdown(
                    label = "Rocket",
                    selectedValue = selectedRocket,
                    options = rocketsList,
                    onValueChange = { viewModel.selectedRocket.value = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdown(
                    label = "Launch Service Provider",
                    selectedValue = selectedProvider,
                    options = providersList,
                    onValueChange = { viewModel.selectedProvider.value = it }
                )
            }
        }
        TextButton(
            onClick = { viewModel.resetFilters() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RESET FILTERS")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
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
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("ALL") },
                onClick = {
                    onValueChange("ALL")
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}