package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BatchInputCard
import com.example.ui.components.ExportDialog
import com.example.ui.components.LearningRecalibrationCard
import com.example.ui.components.LiveProcessingLogView
import com.example.ui.components.MatchCard
import com.example.ui.components.MatchDetailBottomSheet
import com.example.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AnalyticsViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val rawBatchInput by viewModel.rawBatchInput.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val currentProgress by viewModel.currentProgress.collectAsState()
    val currentStatusText by viewModel.currentStatusText.collectAsState()
    val logs by viewModel.processingLogs.collectAsState()
    val filteredMatches by viewModel.filteredMatches.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val selectedDetailMatch by viewModel.selectedMatchForDetail.collectAsState()
    val recalibrationState by viewModel.recalibrationState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showFiltersPanel by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FootyAnalytics",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "1,000+ Leagues • 50,000 Monte Carlo Runs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFiltersPanel = !showFiltersPanel },
                        modifier = Modifier.testTag("toggle_filters_panel_button")
                    ) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("open_export_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Export")
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.Nightlight,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Hero Banner Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1784746133774),
                    contentDescription = "Hero banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Global Multi-Market Analytics Engine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Independent statistical modeling for Fouls, Offsides, Throw-ins, Goal Kicks & Shots",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Navigation Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Match Analytics (${filteredMatches.size})") },
                    icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Live Console Log") },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Recalibration Engine") },
                    icon = { Icon(Icons.Default.ModelTraining, contentDescription = null) }
                )
            }

            // Tab Contents
            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            BatchInputCard(
                                rawText = rawBatchInput,
                                onTextChange = { viewModel.updateBatchInput(it) },
                                onLoadSampleClick = { viewModel.loadSampleFixtures() },
                                onRunAnalyticsClick = { viewModel.processBatchFixtures() },
                                isProcessing = isProcessing
                            )
                        }

                        // Collapsible Filter Panel
                        if (showFiltersPanel) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                FilterPanel(
                                    filterState = filterState,
                                    onSearchQueryChange = { q ->
                                        viewModel.filterState.value = filterState.copy(searchQuery = q)
                                    },
                                    onConfidenceFilterSelect = { c ->
                                        viewModel.filterState.value = filterState.copy(selectedConfidenceFilter = c)
                                    },
                                    onTrapScoreChange = { t ->
                                        viewModel.filterState.value = filterState.copy(maxTrapScore = t)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyzed Match Predictions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (filteredMatches.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No matches match the current filter criteria or batch input.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredMatches, key = { it.id }) { match ->
                                MatchCard(
                                    match = match,
                                    onInspectDetailClick = { m ->
                                        viewModel.selectedMatchForDetail.value = m
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                1 -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LiveProcessingLogView(
                            isProcessing = isProcessing,
                            progress = currentProgress,
                            statusText = currentStatusText,
                            logs = logs
                        )
                    }
                }

                2 -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LearningRecalibrationCard(
                            weights = recalibrationState,
                            onRecalibrateClick = { viewModel.triggerRecalibration() }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for deep detail
    if (selectedDetailMatch != null) {
        MatchDetailBottomSheet(
            match = selectedDetailMatch!!,
            onDismissRequest = { viewModel.selectedMatchForDetail.value = null },
            sheetState = sheetState
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            csvText = viewModel.generateCSVExport(),
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun FilterPanel(
    filterState: com.example.ui.viewmodel.FilterState,
    onSearchQueryChange: (String) -> Unit,
    onConfidenceFilterSelect: (String) -> Unit,
    onTrapScoreChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("filter_search_textfield"),
                placeholder = { Text("Search by team or league...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Filter by Confidence Grade", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val options = listOf("All", "Elite", "Strong+", "Qualified Only", "Auto-Rejected")
                items(options) { option ->
                    FilterChip(
                        selected = filterState.selectedConfidenceFilter == option,
                        onClick = { onConfidenceFilterSelect(option) },
                        label = { Text(option) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Max Trap Score: ${filterState.maxTrapScore}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Slider(
                value = filterState.maxTrapScore.toFloat(),
                onValueChange = { onTrapScoreChange(it.toInt()) },
                valueRange = 0f..100f
            )
        }
    }
}
