package com.example.tunenet.ui

import android.webkit.WebSettings
import androidx.compose.runtime.*
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
    
    // MAPEO DINÁMICO (Simulado para que no siempre sea el mismo video)
    // En una app real, esto vendría de una búsqueda en la API de YouTube
    val videoId = remember(track.id) {
        when {
            track.title.contains("Flowers", ignoreCase = true) -> "G7KNmW9a75Y"
            track.title.contains("As It Was", ignoreCase = true) -> "H5v3kku4y6Q"
            track.title.contains("Blinding Lights", ignoreCase = true) -> "4NRXx6U8ABQ"
            track.artist.name.contains("Bad Bunny", ignoreCase = true) -> "m7B1FkM_hgw"
            track.artist.name.contains("Karol G", ignoreCase = true) -> "9bZkp7q19f0"
            else -> "u1zgFlCw8Aw" // Video por defecto si no hay coincidencia
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)

                val options = IFramePlayerOptions.Builder(context)
                    .controls(1)
                    .origin("https://www.youtube-nocookie.com")
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // Cargamos el video correspondiente a la canción
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                }, options)
                
                try {
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        if (child is android.webkit.WebView) {
                            child.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            break
                        }
                    }
                } catch (e: Exception) {}
            }
        },
        update = { view ->
            // Si el track cambia, podríamos forzar una recarga aquí si fuera necesario
        },
        onRelease = { view ->
            lifecycleOwner.lifecycle.removeObserver(view)
            view.release()
        }
    )
}
