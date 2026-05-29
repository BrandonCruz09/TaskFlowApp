package com.ll.taskflowv3.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextDecoration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateTask: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showAdminDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentContext = LocalContext.current

    LaunchedEffect(Unit) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<com.ll.taskflowv3.data.sync.SyncTasksWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(currentContext).enqueue(syncRequest)
    }

    // 1. AGREGAMOS LA NUEVA PESTAÑA AL ARREGLO
    val categories = listOf("Escuela", "Personal", "Trabajo", "Completados")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // 2. LA NUEVA LÓGICA DE FILTRADO MAESTRA
    val filteredTasks = if (selectedCategory == "Completados") {
        // Si estamos en "Completados", mostramos todas las terminadas
        state.tasks.filter { it.status == TaskStatus.COMPLETED }
    } else {
        // Si estamos en las otras, mostramos solo las de esa categoría que NO estén terminadas
        state.tasks.filter { it.category == selectedCategory && it.status != TaskStatus.COMPLETED }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showAdminDialog = true }) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Admin Login")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                categories.forEach { category ->
                    NavigationBarItem(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        icon = {
                            // 3. ASIGNAMOS EL ÍCONO A LA NUEVA PESTAÑA
                            val icon = when (category) {
                                "Escuela" -> Icons.Default.Star
                                "Personal" -> Icons.Default.Person
                                "Completados" -> Icons.Default.CheckCircle
                                else -> Icons.Default.Menu
                            }
                            Icon(imageVector = icon, contentDescription = category)
                        },
                        label = {
                            // Ocultamos el texto si la pantalla es pequeña para que no se amontone
                            Text(category, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            // Opcional: Ocultamos el botón de agregar si estamos en el historial de completados
            if (selectedCategory != "Completados") {
                FloatingActionButton(
                    onClick = onNavigateToCreateTask,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Tarea", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (filteredTasks.isEmpty() && !state.isLoading) {
                val emptyMessage = if (selectedCategory == "Completados") {
                    "Aún no has completado ninguna tarea.\n¡A trabajar!"
                } else {
                    "No tienes tareas activas en $selectedCategory.\n¡Todo limpio!"
                }

                Text(
                    text = emptyMessage,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskItem(
                            task = task,
                            onCheckedChange = { viewModel.toggleTaskStatus(task) },
                            onDeleteClick = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
            }

            if (state.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                ) {
                    Text(
                        text = state.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showAdminDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminDialog = false },
            onLoginSuccess = {
                showAdminDialog = false
                onNavigateToAdmin()
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300),
        label = "colorFondo"
    )

    val textColor = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onCheckedChange() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )

                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    textDecoration = textDecoration
                )

                if (!task.isSynced) {
                    Text(
                        text = "☁️ Sin sincronizar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                IconButton(onClick = { onDeleteClick() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar Tarea",
                        tint = if (isCompleted) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    textDecoration = textDecoration,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }

            if (task.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateString = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(task.dueDate))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 48.dp)) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha límite",
                        modifier = Modifier.size(16.dp),
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                        textDecoration = textDecoration
                    )
                }
            }

            // Agregamos una pequeña etiqueta visual para saber de qué categoría era la tarea completada
            if (isCompleted) {
                Text(
                    text = "Categoría original: ${task.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.padding(start = 48.dp, top = 8.dp)
                )
            }
        }
    }
}