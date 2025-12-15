package com.example.tunenet

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.AsyncImage
import com.example.tunenet.ui.theme.AppTheme

// 1. MODELO DE DATOS

data class MusicItem(
    val id: Int,
    val title: String,
    val artist: String,
    val genre: String,
    val description: String,
    val coverUrl: String,
    var isFavorite: Boolean = false,
    val comments: MutableList<String> = mutableListOf("¡Temazo!", "Increíble producción.")
)

val sampleMusicData = mutableStateListOf(
    MusicItem(1, "Nada que perder", "Robe", "Rock Transgresivo", "La poesía inconfundible de Robe Iniesta.", "https://images.genius.com/e9b60713f3605db65611c24c15c2891c.1000x1000x1.jpg", true),
    MusicItem(2, "A Match Into Water", "Pierce The Veil", "Post-Hardcore", "Energía pura y riffs rápidos.", "https://i.pinimg.com/736x/87/75/47/8775472c70a9bf2069b0e8c07e079950.jpg", false),
    MusicItem(3, "Duality", "Slipknot", "Nu Metal", "El himno del caos controlado.", "https://i.scdn.co/image/ab67616d0000b2736b3463e7160d333ada4b175a", true),
    MusicItem(4, "Jesucristo García", "Extremoduro", "Rock Urbano", "Un clásico absoluto del rock español.", "https://i.ytimg.com/vi/2DpqV3joVOE/hqdefault.jpg", false),
    MusicItem(5, "Be Quiet and Drive", "Deftones", "Alt-Metal", "Atmósfera etérea y guitarras pesadas.", "https://i.pinimg.com/1200x/b7/a3/b8/b7a3b89f982fd4aca139b7de2c1739a2.jpg", true),
    MusicItem(6, "Smells Like Teen Spirit", "Nirvana", "Grunge", "El himno generacional de los 90.", "https://i.pinimg.com/736x/74/e4/4e/74e44e84d046f8c101fc7eadf4bc5b85.jpg", false)
)

enum class CurrentScreen { LIST, DETAIL, FAV_LIST, FAV_DETAIL, PROFILE, ABOUT }

// 2. MAIN ACTIVITY

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashOnScreen = true
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition { System.currentTimeMillis() - startTime < 2000L }
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(splashScreenView.iconView, View.TRANSLATION_Y, 0f, -splashScreenView.view.height.toFloat())
            slideUp.interpolator = AnticipateInterpolator(); slideUp.duration = 500L
            val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
            fadeOut.duration = 500L; slideUp.start(); fadeOut.start()
            fadeOut.doOnEnd { splashScreenView.remove() }
        }

        enableEdgeToEdge()

        setContent {
            AppTheme {
                val windowSize = calculateWindowSizeClass(this)
                val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact

                var currentScreen by remember { mutableStateOf(CurrentScreen.LIST) }
                var selectedItem by remember { mutableStateOf<MusicItem?>(null) }
                val showFab = currentScreen == CurrentScreen.FAV_DETAIL

                Row(modifier = Modifier.fillMaxSize()) {
                    if (isTablet) {
                        NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            Spacer(modifier = Modifier.weight(1f))
                            NavigationRailItem(
                                icon = { Icon(Icons.Filled.Home, null) },
                                label = { Text("Inicio") },
                                selected = currentScreen == CurrentScreen.LIST || currentScreen == CurrentScreen.DETAIL,
                                onClick = { currentScreen = CurrentScreen.LIST }
                                              )
                            NavigationRailItem(
                                icon = { Icon(Icons.Filled.Favorite, null) },
                                label = { Text("Favoritos") },
                                selected = currentScreen == CurrentScreen.FAV_LIST || currentScreen == CurrentScreen.FAV_DETAIL,
                                onClick = { currentScreen = CurrentScreen.FAV_LIST }
                                              )
                            NavigationRailItem(
                                icon = { Icon(Icons.Filled.Person, null) },
                                label = { Text("Perfil") }, selected = currentScreen == CurrentScreen.PROFILE,
                                onClick = { currentScreen = CurrentScreen.PROFILE }
                                              )

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("TuneNet", fontWeight = FontWeight.Bold) },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                navigationIcon = {
                                    if (currentScreen == CurrentScreen.DETAIL || currentScreen == CurrentScreen.FAV_DETAIL || currentScreen == CurrentScreen.ABOUT) {
                                        IconButton(onClick = { currentScreen = if(currentScreen == CurrentScreen.FAV_DETAIL) CurrentScreen.FAV_LIST else CurrentScreen.LIST }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { currentScreen = CurrentScreen.ABOUT }) { Icon(Icons.Filled.Info, contentDescription = "About") }
                                }
                            )
                        },
                        bottomBar = {
                            if (!isTablet) {
                                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Filled.Home, null) },
                                        label = { Text("Inicio") },
                                        selected = currentScreen == CurrentScreen.LIST || currentScreen == CurrentScreen.DETAIL,
                                        onClick = { currentScreen = CurrentScreen.LIST })
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Filled.Favorite, null) },
                                        label = { Text("Favoritos") },
                                        selected = currentScreen == CurrentScreen.FAV_LIST || currentScreen == CurrentScreen.FAV_DETAIL,
                                        onClick = { currentScreen = CurrentScreen.FAV_LIST })
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Filled.Person, null) },
                                        label = { Text("Perfil") },
                                        selected = currentScreen == CurrentScreen.PROFILE,
                                        onClick = { currentScreen = CurrentScreen.PROFILE })
                                }
                            }
                        },
                        floatingActionButton = {
                            if (showFab) {
                                FloatingActionButton(
                                    onClick = {
                                        selectedItem?.let { item ->
                                            item.comments.add("¡Nuevo comentario!")
                                            val temp = selectedItem; selectedItem = null; selectedItem = temp
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) { Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = Color.White) }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            when (currentScreen) {
                                // PASAMOS 'isTablet' a las pantallas de lista
                                CurrentScreen.LIST -> ElemListScreen(sampleMusicData, isTablet, { selectedItem = it; currentScreen = CurrentScreen.DETAIL }, { toggleFavorite(it); if (selectedItem?.id == it.id) selectedItem = it })
                                CurrentScreen.DETAIL -> selectedItem?.let { DetailItemScreen(it, { selectedItem = toggleFavorite(it) }) }
                                CurrentScreen.FAV_LIST -> FavListScreen(sampleMusicData.filter { it.isFavorite }.toList(), isTablet, { selectedItem = it; currentScreen = CurrentScreen.FAV_DETAIL }, { toggleFavorite(it); if (selectedItem?.id == it.id) selectedItem = it })
                                CurrentScreen.FAV_DETAIL -> selectedItem?.let { DetailFavScreen(it) }
                                CurrentScreen.PROFILE -> ProfileScreen()
                                CurrentScreen.ABOUT -> AboutScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun toggleFavorite(item: MusicItem): MusicItem {
        val index = sampleMusicData.indexOfFirst { it.id == item.id }
        if (index != -1) {
            val updatedItem = sampleMusicData[index].copy(isFavorite = !sampleMusicData[index].isFavorite)
            sampleMusicData[index] = updatedItem
            return updatedItem
        }
        return item
    }
}

// 3. PANTALLAS CON LÓGICA DE GRID VS LISTA

@Composable
fun ElemListScreen(musicItems: List<MusicItem>, isTablet: Boolean, onItemClick: (MusicItem) -> Unit, onFavClick: (MusicItem) -> Unit) {
    if (isTablet) {
        // MODO GRID PARA LA TABLE
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Descubrir Música",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp), // Celdas adaptables
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(musicItems) { item -> TuneGridCard(item, { onItemClick(item) }, onFavClick) }
            }
        }
    } else {
        // MODO LISTA PARA EL MÓVIL
        LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxSize()) {
            item { Text("Descubrir Música",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp)) }
            items(musicItems) { item -> TuneElementCard(item, { onItemClick(item) }, onFavClick) }
        }
    }
}

@Composable
fun FavListScreen(musicItems: List<MusicItem>, isTablet: Boolean, onItemClick: (MusicItem) -> Unit, onRemoveFav: (MusicItem) -> Unit) {
    if (isTablet) {
        // MODO GRID (TABLET)
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mis Favoritos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp))
            if (musicItems.isEmpty())
                Text("No tienes favoritos aún.",
                     style = MaterialTheme.typography.bodyLarge)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(musicItems) { item -> TuneGridCard(item, { onItemClick(item) }, onRemoveFav) }
            }
        }
    } else {
        // MODO LISTA (MÓVIL)
        LazyColumn(contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize())
        {
            item { Text("Mis Favoritos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)) }
            if (musicItems.isEmpty()) item { Text("No tienes favoritos aún.", style = MaterialTheme.typography.bodyLarge) }
            items(musicItems) { item -> TuneElementCard(item, { onItemClick(item) }, onRemoveFav) }
        }
    }
}

// 4. COMPONENTES Y TARJETAS

//Tarjeta Horizontal para Móvil
@Composable
fun TuneElementCard(item: MusicItem, onClick: () -> Unit, onFavClick: (MusicItem) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = item.coverUrl, contentDescription = null, contentScale = ContentScale.Crop,
                alignment = Alignment.TopStart, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(item.artist, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onFavClick(item) }) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = null,
                    tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        }
    }
}

//Tarjeta Vertical para la Tablet
@Composable
fun TuneGridCard(item: MusicItem, onClick: () -> Unit, onFavClick: (MusicItem) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.LightGray)
                      )
            Column(modifier = Modifier.padding(12.dp),
                   horizontalAlignment = Alignment.CenterHorizontally
                  )
            {

                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))

                IconButton(onClick = { onFavClick(item) })
                {
                    Icon(imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DetailItemScreen(item: MusicItem, onFavClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        DetailHeader(item)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onFavClick,
            colors = ButtonDefaults.buttonColors(containerColor = if (item.isFavorite) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = if(item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = null,
                tint = if(item.isFavorite) MaterialTheme.colorScheme.primary else Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if(item.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                color = if (item.isFavorite) MaterialTheme.colorScheme.onSurfaceVariant else Color.White)
        }
    }
}

@Composable
fun DetailFavScreen(item: MusicItem) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        DetailHeader(item)
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Text(
            "Comentarios",
             style = MaterialTheme.typography.titleMedium,
             modifier = Modifier.padding(16.dp),
             color = MaterialTheme.colorScheme.primary
            )
        item.comments.forEach { comment ->
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant))
            {
                Row(modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically)
                {
                    Icon(Icons.Filled.Person,
                         null,
                         modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp)); Text(comment)
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DetailHeader(item: MusicItem) {
    AsyncImage(
        model = item.coverUrl, contentDescription = "Portada", contentScale = ContentScale.Crop, alignment = Alignment.TopStart,
        modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(Color.LightGray)
    )
    Column(modifier = Modifier.padding(24.dp)) {
        Text(item.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(item.artist, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp)); SuggestionChip(onClick = {}, label = { Text(item.genre) })
        Spacer(modifier = Modifier.height(16.dp)); Text(item.description, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ProfileScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Center)
    {
        Icon(
            Icons.Filled.Person,
            null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
            )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Usuario: Joaquín", style = MaterialTheme.typography.headlineSmall)
        Text("joaquinity@gmail.com", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
              )
        {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Center)
    {
        Text("TuneNet", fontSize = 38.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Versión: 2.2", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick =
            {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:");
                putExtra(Intent.EXTRA_EMAIL,
                arrayOf("joaquinity@gmail.com"))
                                                                         }

            try
            { context.startActivity(Intent.createChooser(emailIntent, "Enviar correo...")) }
            catch (e: Exception) {}

            })
        {
            Icon(Icons.Filled.Email,
                 null);
                 Spacer(modifier = Modifier.width(8.dp));
                 Text("Soporte")
        }
    }
}