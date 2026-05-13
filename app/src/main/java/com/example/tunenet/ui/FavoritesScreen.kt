package com.example.tunenet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tunenet.data.local.FavoriteEntity
import com.example.tunenet.ui.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(viewModel: MainViewModel, onTrackClick: (FavoriteEntity) -> Unit) {
    val favorites by viewModel.favorites.collectAsState()
    var favoriteToDelete by remember { mutableStateOf<FavoriteEntity?>(null) }

    if (favoriteToDelete != null) {
        AlertDialog(
            onDismissRequest = { favoriteToDelete = null },
            title = { Text("¿Eliminar de favoritos?") },
            text = { Text("Vas a eliminar '${favoriteToDelete?.title}' de la lista ¿Estás seguro?") },
            confirmButton = {
                TextButton(onClick = {
                    favoriteToDelete?.let { viewModel.removeFavorite(it) }
                    favoriteToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { favoriteToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes favoritos aún", style = MaterialTheme.typography.titleMedium)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites) { favorite ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onTrackClick(favorite) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = favorite.albumCover,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(text = favorite.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = favorite.artistName, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { favoriteToDelete = favorite }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
