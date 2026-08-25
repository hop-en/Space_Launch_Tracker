package dnu.ffeks.soy.spacelaunchtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dnu.ffeks.soy.spacelaunchtracker.R
import dnu.ffeks.soy.spacelaunchtracker.data.local.formatToLocalTime
import dnu.ffeks.soy.spacelaunchtracker.data.network.SpaceLaunch
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImageContent
import kotlinx.coroutines.delay
import dnu.ffeks.soy.spacelaunchtracker.data.local.calculateTimeRemaining
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.automirrored.filled.ArrowBack
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchDetailsScreen(
    launch: SpaceLaunch?,
    isFollowed: Boolean,
    onToggleFollow: () -> Unit,
    onBackClick: () -> Unit
) {
    if (launch == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scrollState = rememberScrollState()
    val unknownText = stringResource(id = R.string.details_unknown)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.details_back_desc))
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFollow) {
                        Icon(
                            imageVector = if (isFollowed) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(id = R.string.details_follow_desc),
                            tint = if (isFollowed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (!launch.image.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = launch.image,
                    contentDescription = launch.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                ) {
                    val state = painter.state
                    if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Success) {
                        SubcomposeAsyncImageContent(modifier = Modifier.height(250.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = launch.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val statusName = launch.status?.name ?: unknownText
                    Text(
                        text = stringResource(id = R.string.details_status, statusName),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = formatToLocalTime(launch.net))
                }
                if (launch.status?.name?.contains("Go", ignoreCase = true) == true) {
                    DetailsCountdownTimer(netTime = launch.net)
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DetailRow(
                    label = stringResource(id = R.string.details_provider),
                    value = launch.launch_service_provider?.name ?: unknownText
                )
                DetailRow(
                    label = stringResource(id = R.string.details_rocket),
                    value = launch.rocket?.configuration?.name ?: unknownText
                )
                DetailRow(
                    label = stringResource(id = R.string.details_location),
                    value = launch.pad?.location?.name ?: unknownText
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = stringResource(id = R.string.details_mission_desc_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = launch.mission?.description ?: stringResource(id = R.string.details_no_description),
                    style = MaterialTheme.typography.bodyMedium
                )

                val crew = launch.rocket?.spacecraft_stage?.launch_crew
                if (!crew.isNullOrEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = stringResource(id = R.string.details_crew_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    crew.forEach { member ->
                        val role = member.role?.role ?: unknownText
                        val name = member.astronaut?.name ?: unknownText
                        Text(
                            text = stringResource(id = R.string.details_crew_member, name, role),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (!launch.vidUrls.isNullOrEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = stringResource(id = R.string.details_video_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    EmbeddedYouTubePlayer(videoUrl = launch.vidUrls.first().url)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
@Composable
fun DetailsCountdownTimer(netTime: String) {

    var timerText by remember(netTime) { mutableStateOf("") }

    LaunchedEffect(netTime) {
        while (true) {
            timerText = calculateTimeRemaining(netTime)
            delay(1000)
        }
    }

    if (timerText.isNotBlank() && timerText != "TBD") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timerText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
        }
    }
}
@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EmbeddedYouTubePlayer(videoUrl: String) {
    val uriHandler = LocalUriHandler.current
    val videoId = if (videoUrl.contains("v=")) {
        videoUrl.substringAfter("v=").substringBefore("&")
    } else if (videoUrl.contains("youtu.be/")) {
        videoUrl.substringAfter("youtu.be/").substringBefore("?")
    } else {
        null
    }

    if (videoId != null) {
        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { uriHandler.openUri(videoUrl) },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.details_video_tap_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

    } else {
        Text(
            text = stringResource(id = R.string.details_video_external_link),
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { uriHandler.openUri(videoUrl) }
                .padding(vertical = 8.dp)
        )
    }
}