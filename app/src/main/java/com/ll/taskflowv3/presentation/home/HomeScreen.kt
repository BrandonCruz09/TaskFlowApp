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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateTask: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showAdminDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 1. NUESTRA LISTA DE CATEGORÍAS Y EL ESTADO DE LA PESTAÑA ACTUAL
    val categories = listOf("Escuela", "Personal", "Trabajo")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // 2. FILTRAMOS LAS TAREAS SEGÚN LA PESTAÑA SELECCIONADA
    val filteredTasks = state.tasks.filter { it.category == selectedCategory }

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
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Login"
                        )
                    }
                }
            )
        },
        // 3. LA NUEVA BARRA INFERIOR (BOTTOM BAR)
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                categories.forEach { category ->
                    NavigationBarItem(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        icon = {
                            // Asignamos un icono distinto a cada pestaña
                            val icon = when (category) {
                                "Escuela" -> Icons.Default.Star
                                "Personal" -> Icons.Default.Person
                                else -> Icons.Default.Menu // Para el trabajo
                            }
                            Icon(imageVector = icon, contentDescription = category)
                        },
                        label = { Text(category) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarea", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 4. USAMOS LA LISTA FILTRADA EN VEZ DE LA LISTA COMPLETA
            if (filteredTasks.isEmpty() && !state.isLoading) {
                Text(
                    text = "No tienes tareas en $selectedCategory.\n¡Todo limpio!",
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
                println("Acceso concedido al administrador")
            }
        )
    }
}

@Composable
fun TaskItem(
    task: com.ll.taskflowv3.domain.model.Task,
    onCheckedChange: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = task.status == com.ll.taskflowv3.domain.model.TaskStatus.COMPLETED,
                    onCheckedChange = { onCheckedChange() }
                )

                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )

                if (!task.isSynced) {
                    Text(
                        text = "⏳ Pendiente",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                IconButton(onClick = { onDeleteClick() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Borrar Tarea",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }

            if (task.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))

                val dateString = java.text.SimpleDateFormat(
                    "dd MMM yyyy",
                    java.util.Locale.getDefault()
                ).format(java.util.Date(task.dueDate))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 48.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                        contentDescription = "Fecha límite",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}