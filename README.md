# TuneNet

TuneNet es una aplicación de descubrimiento musical desarrollada en Kotlin mediante Jetpack Compose. Este proyecto implementa una interfaz de usuario adaptativa (Responsive UI) diseñada para funcionar en dispositivos móviles y tablets, utilizando los componentes de Material Design 3.

## Descripción del Proyecto

El objetivo principal de la aplicación es demostrar la gestión de estados complejos y la adaptación de diseños según el tamaño de pantalla (WindowSizeClass). La arquitectura se basa en una única Activity que gestiona la navegación mediante estados locales, sin dependencias de librerías de navegación externas para este alcance.

## Funcionalidades Implementadas

### Interfaz Adaptativa
La aplicación detecta la configuración del dispositivo y ajusta el layout dinámicamente:
- Dispositivos móviles: Implementación de NavigationBar inferior y visualización de contenido mediante LazyColumn (lista vertical).
- Tablets: Implementación de NavigationRail lateral y visualización mediante LazyVerticalGrid para optimización del espacio en pantalla.

### Gestión de Estado y Búsqueda
El sistema utiliza SnapshotStateList para el manejo de datos dinámicos.
- Buscador en tiempo real: Filtrado simultáneo por título y artista mediante TextField en la TopBar.
- Favoritos: Persistencia en memoria de la selección de favoritos con actualización inmediata de la interfaz.
- Diálogos de confirmación: Implementación de AlertDialog para prevenir el borrado accidental de elementos en la lista de favoritos.

### Sistema de Comentarios
Gestión de listas mutables para la interacción del usuario en la vista de detalles.
- Inserción: Los nuevos comentarios se añaden al índice 0 de la lista para visualización inmediata en la parte superior.
- Eliminación: Cada elemento permite su borrado individual mediante un botón específico alineado a la derecha.

### Diseño y Temas
- Splash Screen: Animación de entrada utilizando interpolación Anticipate.
- Tema personalizado: Definición de paleta de colores propia y soporte básico para modo oscuro.
- Carga de imágenes: Integración de la librería Coil para la carga asíncrona de portadas.
