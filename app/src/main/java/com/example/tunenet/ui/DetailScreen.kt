package com.example.tunenet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tunenet.data.model.DeezerTrack
import com.example.tunenet.ui.viewmodel.MainViewModel

@Composable
fun DetailScreen(track: DeezerTrack, viewModel: MainViewModel) {
    val comments by viewModel.getComments(track.id).collectAsState()
    val username by viewModel.username.collectAsState()
    var newComment by remember { mutableStateOf("") }

    // Estado del reproductor
    val isPlaying = viewModel.isPlaying && viewModel.currentPlayingTrack?.id == track.id
    val progress = if (viewModel.currentPlayingTrack?.id == track.id) viewModel.playbackProgress else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            AsyncImage(
                model = track.album.coverMedium,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = track.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = track.artist.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(16.dp))

            // CONTROLES DE REPRODUCCIÓN
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Slider(
                        value = progress,
                        onValueChange = { viewModel.seekTo(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.playTrack(track) }, modifier = Modifier.size(64.dp)) {
                            Icon(
                                if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow, // Corregido el icono
                                contentDescription = if (isPlaying) "Pausa" else "Reproducir",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = if (isPlaying) "Reproduciendo preview..." else "Escuchar preview (30s)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Información", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "Álbum: ${track.album.title}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Duración: ${track.duration} segundos", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Comentarios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    label = { Text("Añadir comentario...") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newComment.isNotBlank()) {
                        viewModel.addComment(track.id, username, newComment)
                        newComment = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        items(comments) { comment ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = comment.author, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = comment.content)
                }
            }
        }
    }
}
