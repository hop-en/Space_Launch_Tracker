package dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dnu.ffeks.soy.spacelaunchtracker.R
import dnu.ffeks.soy.spacelaunchtracker.data.local.AppDatabase
import dnu.ffeks.soy.spacelaunchtracker.data.local.FollowPreferences
import dnu.ffeks.soy.spacelaunchtracker.data.local.calculateTimeRemaining
import dnu.ffeks.soy.spacelaunchtracker.data.network.ApiClient
import dnu.ffeks.soy.spacelaunchtracker.data.network.SpaceLaunch
import dnu.ffeks.soy.spacelaunchtracker.data.settings.SettingsRepository
import dnu.ffeks.soy.spacelaunchtracker.data.worker.LaunchReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class SpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val followPrefs = FollowPreferences(application)
    val followedIds = followPrefs.followedIds.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val dao = AppDatabase.getDatabase(application).spaceLaunchDao()
    private val settingsRepo = SettingsRepository(application)
    private val strAll = application.getString(R.string.filter_all)
    private val strUnknown = application.getString(R.string.details_unknown)

    val searchQuery = MutableStateFlow("")
    val selectedLocation = MutableStateFlow(strAll)
    val selectedCrewed = MutableStateFlow(strAll)
    val selectedRocket = MutableStateFlow(strAll)
    val selectedProvider = MutableStateFlow(strAll)

    private val filtersFlow = combine(
        searchQuery, selectedLocation, selectedCrewed, selectedRocket, selectedProvider
    ) { search, loc, crewed, rocket, provider ->
    }

    private val allLaunchesFromDb = dao.getAllLaunches().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    private val _upcomingLaunches = allLaunchesFromDb.map { launches ->
        val now = Instant.now()
        launches.filter { launch ->
            try { Instant.parse(launch.net).isAfter(now) } catch (e: Exception) { false }
        }.sortedBy { it.net }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _pastLaunches = allLaunchesFromDb.map { launches ->
        val now = Instant.now()
        launches.filter { launch ->
            try { !Instant.parse(launch.net).isAfter(now) } catch (e: Exception) { true }
        }.sortedByDescending { it.net }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val upcomingLaunches = combine(_upcomingLaunches, filtersFlow) { launches, _ ->
        applyFilters(launches)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pastLaunches = combine(_pastLaunches, filtersFlow) { launches, _ ->
        applyFilters(launches)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val upcomingNext7DaysCount = _upcomingLaunches.map { launches ->
        val now = Instant.now()
        val sevenDaysLater = now.plus(Duration.ofDays(7))
        launches.count { launch ->
            try {
                val launchTime = Instant.parse(launch.net)
                launchTime.isAfter(now) && launchTime.isBefore(sevenDaysLater)
            } catch (e: Exception) {
                false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val availableLocations = combine(_upcomingLaunches, _pastLaunches) { upcoming, past ->
        (upcoming + past).mapNotNull { it.pad?.location?.name }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableRockets = combine(_upcomingLaunches, _pastLaunches) { upcoming, past ->
        (upcoming + past).mapNotNull { it.rocket?.configuration?.name }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableProviders = combine(_upcomingLaunches, _pastLaunches) { upcoming, past ->
        (upcoming + past).mapNotNull { it.launch_service_provider?.name }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _launchDetails = MutableStateFlow<SpaceLaunch?>(null)
    val launchDetails = _launchDetails.asStateFlow()

    private val _timerText = MutableStateFlow(application.getString(R.string.state_loading))
    val timerText = _timerText.asStateFlow()

    init {
        observeAndStartTimer()
        loadAllData()
    }
    private fun observeAndStartTimer() {
        val app = getApplication<Application>()
        viewModelScope.launch {
            while (true) {
                val nextLaunch = upcomingLaunches.value.firstOrNull()

                if (nextLaunch != null) {
                    _timerText.value = calculateTimeRemaining(nextLaunch.net)
                } else {
                    _timerText.value = app.getString(R.string.state_tbd)
                }

                delay(1000)
            }
        }
    }

    fun loadAllData() {
        if (_isRefreshing.value) return

        val app = getApplication<Application>()
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                val rawNetworkLaunches = mutableListOf<SpaceLaunch>()

                for (offset in 0 until 500 step 100) {
                    val response = ApiClient.apiService.getUpcomingLaunches(limit = 100, offset = offset)
                    if (response.results.isEmpty()) break
                    rawNetworkLaunches.addAll(response.results)
                }

                for (offset in 0 until 500 step 100) {
                    val response = ApiClient.apiService.getPastLaunches(limit = 100, offset = offset)
                    if (response.results.isEmpty()) break
                    rawNetworkLaunches.addAll(response.results)
                }
                val allNetworkLaunches = rawNetworkLaunches.filter { launch ->
                    val providerName = launch.launch_service_provider?.name ?: ""
                    val isBadProvider = providerName.contains("Russian", ignoreCase = true) ||
                            providerName.contains("ROSCOSMOS", ignoreCase = true)  ||
                            providerName.contains("RKK", ignoreCase = true)

                    !isBadProvider
                }

                withContext(Dispatchers.IO) {
                    dao.insertLaunches(allNetworkLaunches)
                }
                updateRemindersWithFreshData(allNetworkLaunches)
            } catch (e: Exception) {
                if (allLaunchesFromDb.value.isEmpty()) {
                    _timerText.value = app.getString(R.string.state_network_error)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadLaunchDetails(id: String) {
        viewModelScope.launch {
            _launchDetails.value = null
            try {
                val cached = allLaunchesFromDb.value.find { it.id == id }
                if (cached != null) {
                    _launchDetails.value = cached
                }

                val response = ApiClient.apiService.getLaunchById(id)

                _launchDetails.value = response

            } catch (e: Exception) {
            }
        }
    }

    fun toggleFollow(id: String) {
        viewModelScope.launch {
            val isCurrentlyFollowed = followedIds.value.contains(id)
            followPrefs.toggleFollow(id)

            if (isCurrentlyFollowed) {
                WorkManager.getInstance(getApplication()).cancelAllWorkByTag(id)
            } else {
                val launch = allLaunchesFromDb.value.find { it.id == id } ?: _launchDetails.value
                launch?.let { scheduleReminder(it) }
            }
        }
    }

    private fun scheduleReminder(launch: SpaceLaunch) {
        viewModelScope.launch {
            try {
                val launchDate = Instant.parse(launch.net)
                val now = Instant.now()
                val workManager = WorkManager.getInstance(getApplication())
                val is24hEnabled = settingsRepo.is24hReminderEnabled.first()
                val is1hEnabled = settingsRepo.is1hReminderEnabled.first()

                if (is24hEnabled) {
                    val reminderTime24h = launchDate.minus(Duration.ofHours(24))
                    val delayDuration24h = Duration.between(now, reminderTime24h)

                    if (!delayDuration24h.isNegative && !delayDuration24h.isZero) {
                        val inputData24h = Data.Builder()
                            .putString(LaunchReminderWorker.KEY_LAUNCH_NAME, launch.name)
                            .putString(LaunchReminderWorker.KEY_LAUNCH_ID, launch.id)
                            .putString(LaunchReminderWorker.KEY_TIME_LEFT, "24h")
                            .build()

                        val workRequest24h = OneTimeWorkRequestBuilder<LaunchReminderWorker>()
                            .setInitialDelay(delayDuration24h.toMillis(), TimeUnit.MILLISECONDS)
                            .setInputData(inputData24h)
                            .addTag(launch.id)
                            .build()

                        workManager.enqueueUniqueWork(
                            "reminder_24h_${launch.id}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest24h
                        )
                    }
                }

                if (is1hEnabled) {
                    val reminderTime1h = launchDate.minus(Duration.ofHours(1))
                    val delayDuration1h = Duration.between(now, reminderTime1h)

                    if (!delayDuration1h.isNegative && !delayDuration1h.isZero) {
                        val inputData1h = Data.Builder()
                            .putString(LaunchReminderWorker.KEY_LAUNCH_NAME, launch.name)
                            .putString(LaunchReminderWorker.KEY_LAUNCH_ID, launch.id)
                            .putString(LaunchReminderWorker.KEY_TIME_LEFT, "1h")
                            .build()

                        val workRequest1h = OneTimeWorkRequestBuilder<LaunchReminderWorker>()
                            .setInitialDelay(delayDuration1h.toMillis(), TimeUnit.MILLISECONDS)
                            .setInputData(inputData1h)
                            .addTag(launch.id)
                            .build()

                        workManager.enqueueUniqueWork(
                            "reminder_1h_${launch.id}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest1h
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetFilters() {
        searchQuery.value = ""
        selectedLocation.value = strAll
        selectedCrewed.value = strAll
        selectedRocket.value = strAll
        selectedProvider.value = strAll
    }

    private fun applyFilters(launches: List<SpaceLaunch>): List<SpaceLaunch> {
        return launches.filter { launch ->
            val matchesName = searchQuery.value.isBlank() ||
                    launch.name.contains(searchQuery.value, ignoreCase = true)

            val locationName = launch.pad?.location?.name ?: strUnknown
            val matchesLocation = selectedLocation.value == strAll || locationName == selectedLocation.value

            val isCrewedMission = launch.mission?.type?.contains("Human", ignoreCase = true) == true
            val matchesCrewed = when (selectedCrewed.value) {
                "Yes" -> isCrewedMission
                "No" -> !isCrewedMission
                else -> true
            }

            val rocketName = launch.rocket?.configuration?.name ?: strUnknown
            val matchesRocket = selectedRocket.value == strAll || rocketName == selectedRocket.value

            val providerName = launch.launch_service_provider?.name ?: strUnknown
            val matchesProvider = selectedProvider.value == strAll || providerName == selectedProvider.value

            matchesName && matchesLocation && matchesCrewed && matchesRocket && matchesProvider
        }
    }
    private fun updateRemindersWithFreshData(latestLaunches: List<SpaceLaunch>) {
        val currentFollowed = followedIds.value
        latestLaunches.filter { currentFollowed.contains(it.id) }.forEach { launch ->
            scheduleReminder(launch)
        }
    }
}