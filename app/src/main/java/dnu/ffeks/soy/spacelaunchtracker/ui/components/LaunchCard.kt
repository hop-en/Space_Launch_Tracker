package dnu.ffeks.soy.spacelaunchtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dnu.ffeks.soy.spacelaunchtracker.R
import dnu.ffeks.soy.spacelaunchtracker.data.network.SpaceLaunch

@Composable
fun LaunchCard(
    launch: SpaceLaunch,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(100.dp)) {

            AsyncImage(
                model = launch.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp),
                placeholder = painterResource(id = R.drawable.illustrationrocketship),
                error = painterResource(id = R.drawable.illustrationrocketship),
                fallback = painterResource(id = R.drawable.illustrationrocketship)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = launch.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = launch.net.substringBefore("T"),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(
                onClick = onFollowClick,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = if (isFollowed) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Follow",
                    tint = if (isFollowed) Color.Red else Color.Gray
                )
            }
        }
    }
}