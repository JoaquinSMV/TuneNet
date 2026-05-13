package com.example.tunenet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tunenet.data.model.DeezerTrack

@Composable
fun TrackListScreen(
    tracks: List<DeezerTrack>,
    isTablet: Boolean,
    onTrackClick: (DeezerTrack) -> Unit,
    onFavoriteClick: (DeezerTrack) -> Unit
) {
    if (isTablet) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tracks) { track ->
                TrackGridItem(track, onTrackClick, onFavoriteClick)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tracks) { track ->
                TrackListItem(track, onTrackClick, onFavoriteClick)
            }
        }
    }
}

@Composable
fun TrackListItem(track: DeezerTrack, onClick: (DeezerTrack) -> Unit, onFav: (DeezerTrack) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(track) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = track.album.coverMedium,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = track.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = track.artist.name, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onFav(track) }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorito", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun TrackGridItem(track: DeezerTrack, onClick: (DeezerTrack) -> Unit, onFav: (DeezerTrack) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(track) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = track.album.coverMedium,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = track.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = track.artist.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = { onFav(track) }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorito", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
