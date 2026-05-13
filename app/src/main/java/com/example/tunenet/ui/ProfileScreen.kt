package com.example.tunenet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tunenet.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val username by viewModel.username.collectAsState()
    val themeSelection by viewModel.themeSelection.collectAsState()
    
    var editName by remember { mutableStateOf(username) }
    
    // Sincronizar editName cuando cambie el username en el DataStore
    LaunchedEffect(username) {
        editName = username
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = { viewModel.saveUsername(editName) },
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Guardar Nombre")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Selección de Tema",
            style = MaterialTheme.typography.titleLarge
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            RadioButton(
                selected = themeSelection == "Light",
                onClick = { viewModel.saveTheme("Light") }
            )
            Text("Claro")
            
            Spacer(modifier = Modifier.width(8.dp))
            
            RadioButton(
                selected = themeSelection == "Dark",
                onClick = { viewModel.saveTheme("Dark") }
            )
            Text("Oscuro")
            
            Spacer(modifier = Modifier.width(8.dp))
            
            RadioButton(
                selected = themeSelection == "System",
                onClick = { viewModel.saveTheme("System") }
            )
            Text("Sistema")
        }
    }
}
