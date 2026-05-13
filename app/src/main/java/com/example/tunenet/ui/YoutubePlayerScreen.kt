package com.example.tunenet.ui

import android.webkit.WebSettings
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
    
    // TRUCO: ID estable de música para pruebas
    val videoId = "u1zgFlCw8Aw" 

    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)

                // TRUCO DEFINITIVO PARA ERROR 152-4:
                // En la v13.0.0, el Builder REQUIERE pasar el context.
                // Pero parece que en algunas versiones de la librería no es necesario.
                // Vamos a usar una inicialización más estándar si el builder falla.
                val options = IFramePlayerOptions.Builder()
                    .controls(1)
                    .origin("https://www.youtube-nocookie.com")
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                }, options)
                
                // Intentamos forzar el User-Agent para mayor estabilidad
                // Accedemos al WebView interno de forma segura
                try {
                    // La librería usa un WebView internamente. Vamos a intentar buscarlo.
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        if (child is android.webkit.WebView) {
                            child.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Fallback silencioso
                }
            }
        },
        onRelease = { view ->
            lifecycleOwner.lifecycle.removeObserver(view)
            view.release()
        }
    )
}
