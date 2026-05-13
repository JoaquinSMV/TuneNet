package com.example.tunenet

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.tunenet.data.local.TuneDatabase
import com.example.tunenet.data.local.UserPreferences
import com.example.tunenet.data.model.DeezerAlbum
import com.example.tunenet.data.model.DeezerArtist
import com.example.tunenet.data.model.DeezerTrack
import com.example.tunenet.data.remote.DeezerApiService
import com.example.tunenet.data.repository.MusicRepository
import com.example.tunenet.ui.*
import com.example.tunenet.ui.theme.AppTheme
import com.example.tunenet.ui.viewmodel.MainViewModel
import com.example.tunenet.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { false }
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(splashScreenView.iconView, View.TRANSLATION_Y, 0f, -splashScreenView.view.height.toFloat())
            slideUp.interpolator = AnticipateInterpolator()
            slideUp.duration = 500L
            val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
            fadeOut.duration = 500L
            slideUp.start()
            fadeOut.start()
            fadeOut.doOnEnd { splashScreenView.remove() }
        }

        enableEdgeToEdge()

        val database = TuneDatabase.getDatabase(this)
        val apiService = DeezerApiService.create()
        val repository = MusicRepository(apiService, database.tuneDao())
        val userPreferences = UserPreferences(this)
        val factory = MainViewModelFactory(application, repository, userPreferences)

        setContent {
            val viewModel: MainViewModel = viewModel(factory = factory)
            val themeSelection by viewModel.themeSelection.collectAsState()
            
            val darkTheme = when (themeSelection) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = darkTheme) {
                TuneNetApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun TuneNetApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val windowSize = calculateWindowSizeClass(context as MainActivity)
    val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Manejo de Toasts
    viewModel.toastMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (isTablet) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Spacer(modifier = Modifier.weight(1f))
                NavigationRailItem(
                    icon = { Icon(Icons.Filled.Home, null) },
                    label = { Text("Inicio") },
                    selected = currentRoute == Screen.List.route,
                    onClick = { navController.navigate(Screen.List.route) }
                )
                NavigationRailItem(
                    icon = { Icon(Icons.Filled.Favorite, null) },
                    label = { Text("Favoritos") },
                    selected = currentRoute == Screen.Favorites.route,
                    onClick = { navController.navigate(Screen.Favorites.route) }
                )
                NavigationRailItem(
                    icon = { Icon(Icons.Filled.Person, null) },
                    label = { Text("Perfil") },
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navController.navigate(Screen.Profile.route) }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (currentRoute == Screen.List.route) {
                            TextField(
                                value = viewModel.searchText,
                                onValueChange = { viewModel.onSearchTextChanged(it) },
                                placeholder = { Text("Buscar en Deezer...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        } else {
                            Text("TuneNet 3.1", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        if (currentRoute != Screen.List.route && !isTablet) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    // MINI PLAYER PERSISTENTE
                    viewModel.currentPlayingTrack?.let { track ->
                        MiniPlayer(
                            track = track,
                            isPlaying = viewModel.isPlaying,
                            progress = viewModel.playbackProgress,
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onSeek = { viewModel.seekTo(it) },
                            onClick = {
                                navController.navigate(Screen.Detail.createRoute(track.id))
                            }
                        )
                    }
                    
                    if (!isTablet) {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, null) },
                                label = { Text("Inicio") },
                                selected = currentRoute == Screen.List.route,
                                onClick = { navController.navigate(Screen.List.route) }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Favorite, null) },
                                label = { Text("Favoritos") },
                                selected = currentRoute == Screen.Favorites.route,
                                onClick = { navController.navigate(Screen.Favorites.route) }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Person, null) },
                                label = { Text("Perfil") },
                                selected = currentRoute == Screen.Profile.route,
                                onClick = { navController.navigate(Screen.Profile.route) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.List.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.List.route) {
                    val tracks by viewModel.searchResults.collectAsState()
                    TrackListScreen(
                        tracks = tracks,
                        isTablet = isTablet,
                        onTrackClick = { track ->
                            navController.navigate(Screen.Detail.createRoute(track.id))
                        },
                        onFavoriteClick = { track -> viewModel.addFavorite(track) },
                        currentTrackId = viewModel.currentPlayingTrack?.id
                    )
                }
                composable(
                    route = Screen.Detail.route,
                    arguments = listOf(navArgument("trackId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackId = backStackEntry.arguments?.getLong("trackId")

                    // Obtenemos ambas listas
                    val searchResults by viewModel.searchResults.collectAsState()
                    val favorites by viewModel.favorites.collectAsState()

                    // Buscamos en resultados de búsqueda
                    val trackFromSearch = searchResults.find { it.id == trackId }

                    // Si no está ahí, buscamos en favoritos y lo convertimos a DeezerTrack
                    val trackFromFavs = favorites.find { it.id == trackId }?.let { fav ->
                        DeezerTrack(
                            id = fav.id,
                            title = fav.title,
                            artist = DeezerArtist(fav.artistName),
                            album = DeezerAlbum(fav.albumTitle, fav.albumCover),
                            preview = fav.preview,
                            duration = fav.duration
                        )
                    }

                    // Usamos el que hayamos encontrado
                    val track = trackFromSearch ?: trackFromFavs
                    track?.let { DetailScreen(it, viewModel) }
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(viewModel) { favorite ->
                        val track = DeezerTrack(
                            id = favorite.id,
                            title = favorite.title,
                            artist = DeezerArtist(favorite.artistName),
                            album = DeezerAlbum(favorite.albumTitle, favorite.albumCover),
                            preview = favorite.preview,
                            duration = favorite.duration
                        )
                        viewModel.playTrack(track) // Esto hace que suene
                        navController.navigate(Screen.Detail.createRoute(track.id)) // ESTO TE LLEVA AL DETALLE
                    }
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(
    track: DeezerTrack,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = track.album.coverMedium,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        // Cambiado Close por Pause (asegúrate de tener el import)
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir"
                    )
                }
            }
        }
    }
}
