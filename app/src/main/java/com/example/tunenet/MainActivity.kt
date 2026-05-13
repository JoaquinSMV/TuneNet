package com.example.tunenet

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        // Splash Screen (Mantenemos tu lógica de animación)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { false } // Se maneja por tiempo o carga si fuera necesario
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

        // Inicialización de dependencias (Manual DI para simplicidad en la práctica)
        val database = TuneDatabase.getDatabase(this)
        val apiService = DeezerApiService.create()
        val repository = MusicRepository(apiService, database.tuneDao())
        val userPreferences = UserPreferences(this)
        val factory = MainViewModelFactory(repository, userPreferences)

        setContent {
            val viewModel: MainViewModel = viewModel(factory = factory)
            val themeSelection by viewModel.themeSelection.collectAsState()
            
            val darkTheme = when (themeSelection) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = darkTheme) {
                val windowSize = calculateWindowSizeClass(this)
                val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val context = LocalContext.current

                // Manejo de Toasts desde el ViewModel
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
                                        Text("TuneNet 3.0", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                                        // Navegación con argumentos complejos simplificada por ID
                                        // Para una app real, pasaríamos el objeto o usaríamos un SharedViewModel
                                        navController.navigate(Screen.Detail.createRoute(track.id))
                                    },
                                    onFavoriteClick = { track -> viewModel.addFavorite(track) }
                                )
                            }
                            composable(
                                route = Screen.Detail.route,
                                arguments = listOf(navArgument("trackId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val trackId = backStackEntry.arguments?.getLong("trackId")
                                val tracks by viewModel.searchResults.collectAsState()
                                val track = tracks.find { it.id == trackId }
                                track?.let { DetailScreen(it, viewModel) }
                            }
                            composable(Screen.Favorites.route) {
                                FavoritesScreen(viewModel) { favorite ->
                                    // Re-mapear FavoriteEntity a DeezerTrack para DetailScreen
                                    val track = DeezerTrack(
                                        id = favorite.id,
                                        title = favorite.title,
                                        artist = DeezerArtist(favorite.artistName),
                                        album = DeezerAlbum(favorite.albumTitle, favorite.albumCover),
                                        preview = favorite.preview,
                                        duration = favorite.duration
                                    )
                                    // Usamos una ruta temporal o la misma DetailScreen
                                    // Para este ejercicio, mostramos el detalle igual
                                    DetailScreen(track, viewModel)
                                }
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
