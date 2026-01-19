package com.zachvlat.footballscores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zachvlat.footballscores.data.repository.LiveScoresRepository
import com.zachvlat.footballscores.ui.components.*
import com.zachvlat.footballscores.ui.theme.FootballscoresTheme
import com.zachvlat.footballscores.ui.viewmodel.LiveScoresUiState
import com.zachvlat.footballscores.ui.viewmodel.LiveScoresViewModel
import com.zachvlat.footballscores.ui.viewmodel.LiveScoresViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FootballscoresTheme {
                LiveScoresScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScoresScreen() {
    val repository = remember { LiveScoresRepository() }
    val viewModel: LiveScoresViewModel = viewModel(
        factory = LiveScoresViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedDate by remember { mutableStateOf(viewModel.getTodayDateString()) }
    var showDateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Soccer Scores",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatDateForDisplay(selectedDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is LiveScoresUiState.Loading -> {
                    LoadingIndicator()
                }
                
                is LiveScoresUiState.Success -> {
                    if (state.response.Stages.isEmpty()) {
                        EmptyState(
                            message = "No matches found for selected date",
                            onRefresh = { viewModel.refresh() }
                        )
                    } else {
                        MatchList(stages = state.response.Stages)
                    }
                }
                
                is LiveScoresUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRefresh = { viewModel.refresh() }
                    )
                }
            }
        }
    }
    
    if (showDateDialog) {
        DateSelectionDialog(
            onDateSelected = { date ->
                selectedDate = date
                viewModel.loadScoresForDate(date)
                showDateDialog = false
            },
            onDismiss = { showDateDialog = false }
        )
    }
}

@Composable
fun MatchList(stages: List<com.zachvlat.footballscores.data.model.Stage>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        stages.forEach { stage ->
            if (stage.Events.isNotEmpty()) {
                // Competition Header
                item {
                    CompetitionHeader(stage = stage)
                }
                
                // Matches for this competition
                items(stage.Events) { event ->
                    MatchCard(event = event)
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
fun DateSelectionDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel = viewModel<LiveScoresViewModel>()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                TextButton(onClick = {
                    onDateSelected(viewModel.getTodayDateString())
                }) {
                    Text("Today")
                }
                TextButton(onClick = {
                    onDateSelected(viewModel.getYesterdayDateString())
                }) {
                    Text("Yesterday")
                }
                TextButton(onClick = {
                    onDateSelected(viewModel.getTomorrowDateString())
                }) {
                    Text("Tomorrow")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDateForDisplay(dateString: String): String {
    return when {
        dateString.length == 8 -> {
            val year = dateString.substring(0, 4)
            val month = dateString.substring(4, 6)
            val day = dateString.substring(6, 8)
            "$year-$month-$day"
        }
        else -> dateString
    }
}