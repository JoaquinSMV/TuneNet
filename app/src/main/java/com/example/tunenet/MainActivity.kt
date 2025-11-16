package com.example.tunenet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tunenet.ui.theme.TuneNetTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instalar SplashScreen
        installSplashScreen().apply {

            // Animación de salida: giro del icono
            setOnExitAnimationListener { splashScreenViewProvider ->

                val iconView = splashScreenViewProvider.iconView

                // ANIMACIÓN: rotación completa de 360 grados
                iconView.animate()
                    .rotationBy(360f)
                    .setDuration(500L) // 1 segundo
                    .withEndAction {
                        splashScreenViewProvider.remove()
                    }
                    .start()
            }
        }

        // Duración de la SplashScreen (1 segundo)
        runBlocking {
            delay(1000)
        }

        enableEdgeToEdge()
        setContent {
            TuneNetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AboutScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TuneNet", fontSize = 38.sp)
        Text("Temática: Música y Comunidad", fontSize = 16.sp)
        Text(
            text = "Aplicación con un enfoque de red social como Spotify, utilizando la API de Deezer.",
            fontSize = 14.sp
        )
        Text("Versión: 1.0", fontSize = 14.sp)

        IconButton(onClick = {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("joaquinity@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Información sobre la Aplicación")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Esta es una aplicación de música social que utiliza la API de Deezer para proporcionar una experiencia de descubrimiento de música comunitaria."
                )
            }
            context.startActivity(Intent.createChooser(emailIntent, "Enviar correo..."))
        }) {
            Icon(
                Icons.Default.Email,
                contentDescription = "Correo electrónico",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    TuneNetTheme {
        AboutScreen()
    }
}
