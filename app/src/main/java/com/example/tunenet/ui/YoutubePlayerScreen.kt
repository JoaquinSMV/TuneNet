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
                // Usamos youtube-nocookie.com y configuramos el origin
                val options = IFramePlayerOptions.Builder()
                    .controls(1)
                    .origin("https://www.youtube-nocookie.com")
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                }, options)
                
                // TRUCO EXTRA: Forzar el User-Agent para que parezca un navegador de escritorio
                // Esto ayuda a que YouTube no bloquee la petición del WebView interno
                try {
                    val webViewField = YouTubePlayerView::class.java.getDeclaredField("webView")
                    webViewField.isAccessible = true
                    val webView = webViewField.get(this) as android.webkit.WebView
                    webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                } catch (e: Exception) {
                    // Si falla el acceso por reflexión, al menos tenemos el origin configurado
                }
            }
        },
        onRelease = { view ->
            lifecycleOwner.lifecycle.removeObserver(view)
            view.release()
        }
    )
}
