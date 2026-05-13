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
    // ID de prueba que no suele tener restricciones
    val videoId = "u1zgFlCw8Aw"

    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubePlayerView(context).apply {
                // Importante: Desactivamos la inicialización automática para usar nuestras opciones
                enableAutomaticInitialization = false

                lifecycleOwner.lifecycle.addObserver(this)

                // AQUÍ VA EL BLOQUE DE OPCIONES
                val options = IFramePlayerOptions.Builder()
                    .controls(1)
                    .origin("https://www.youtube.com")
                    .build()

                // Y AQUÍ LA INICIALIZACIÓN MANUAL
                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // Usamos loadVideo para forzar la carga
                        youTubePlayer.loadVideo(videoId, 0f)
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