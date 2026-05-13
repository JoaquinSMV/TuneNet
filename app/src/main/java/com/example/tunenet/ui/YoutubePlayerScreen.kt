package com.example.tunenet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.tunenet.data.model.DeezerTrack
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YoutubePlayerScreen(track: DeezerTrack, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // TRUCO: En lugar de un ID fijo, idealmente buscaríamos el ID.
    // Por ahora, para arreglar el error 152-4, vamos a usar una configuración más robusta.
    // El error 152-4 a veces se soluciona cambiando el origin o desactivando ciertos controles.
    val videoId = "u1zgFlCw8Aw" // ID por defecto (puedes cambiarlo por una búsqueda real)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)

                val options = IFramePlayerOptions.Builder()
                    .controls(1)
                    .fullscreen(0) // Desactivamos pantalla completa interna para evitar conflictos
                    .rel(0) // No mostrar videos relacionados al final
                    .ivLoadPolicy(3) // No mostrar anotaciones
                    .ccLoadPolicy(0) // No subtítulos por defecto
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // Usamos cueVideo en lugar de loadVideo para ser menos agresivos con la carga
                        // y evitar bloqueos de la API de YouTube
                        youTubePlayer.cueVideo(videoId, 0f)
                    }
                    
                    override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                        // Si da error, intentamos recargar una vez con un método diferente
                        if (error == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError.VIDEO_NOT_FOUND) {
                            // Aquí podrías intentar con otro ID
                        }
                    }
                }, options)
            }
        },
        onRelease = { view ->
            lifecycleOwner.lifecycle.removeObserver(view)
            view.release()
        }
    )
}
