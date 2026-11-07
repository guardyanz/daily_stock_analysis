package id.ihsg.stockanalysis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.ihsg.stockanalysis.data.HistoryItem
import id.ihsg.stockanalysis.data.StockQuote
import id.ihsg.stockanalysis.data.TaskInfo
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: IhsgViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        onBaseUrlChanged = viewModel::onBaseUrlChanged,
        onSaveBaseUrl = viewModel::saveBaseUrl,
        onSymbolChanged = viewModel::onSymbolInputChanged,
        onAddSymbol = viewModel::addSymbol,
        onRemoveSymbol = viewModel::removeSymbol,
        onRefresh = viewModel::refresh,
        onAnalyze = viewModel::analyze,
        onMarketReview = viewModel::marketReview,
        onSelectReport = viewModel::selectReport
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: IhsgUiState,
    onBaseUrlChanged: (String) -> Unit,
    onSaveBaseUrl: () -> Unit,
    onSymbolChanged: (String) -> Unit,
    onAddSymbol: () -> Unit,
    onRemoveSymbol: (String) -> Unit,
    onRefresh: () -> Unit,
    onAnalyze: (String) -> Unit,
    onMarketReview: () -> Unit,
    onSelectReport: (String?) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("IHSG Stock Analysis", fontWeight = FontWeight.SemiBold)
                        Text("IDX-only mobile console", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.busy) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ConnectionPanel(
                        state = state,
                        onBaseUrlChanged = onBaseUrlChanged,
                        onSaveBaseUrl = onSaveBaseUrl
                    )
                }
                item {
                    ActionPanel(
                        state = state,
                        onSymbolChanged = onSymbolChanged,
                        onAddSymbol = onAddSymbol,
                        onMarketReview = onMarketReview
                    )
                }
                item {
                    WatchlistChips(
                        symbols = state.watchlist,
                        onRemoveSymbol = onRemoveSymbol
                    )
                }
                item {
                    SectionHeader("Market Quotes", Icons.AutoMirrored.Filled.ShowChart)
                }
                if (state.quotes.isEmpty()) {
                    item { EmptyState("No quotes yet. Connect the backend and add .JK stocks to your watchlist.") }
                } else {
                    items(state.quotes, key = { it.stockCode }) { quote ->
                        QuoteCard(quote = quote, onAnalyze = onAnalyze)
                    }
                }
                item {
                    SectionHeader("Task Monitor", Icons.Default.CloudSync)
                }
                if (state.tasks.isEmpty()) {
                    item { EmptyState("No active analysis tasks.") }
                } else {
                    items(state.tasks, key = { it.taskId.ifBlank { it.hashCode().toString() } }) { task ->
                        TaskCard(task = task, onSelectReport = onSelectReport)
                    }
                }
                item {
                    SectionHeader("Latest Reports", Icons.AutoMirrored.Filled.Article)
                }
                if (state.history.isEmpty()) {
                    item { EmptyState("No IHSG analysis history yet.") }
                } else {
                    items(state.history, key = { it.queryId }) { item ->
                        HistoryCard(item)
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                }
            }

            if (state.busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                )
            }

            state.selectedReport?.let { report ->
                ReportOverlay(report = report, onClose = { onSelectReport(null) })
            }
        }
    }
}

@Composable
private fun ConnectionPanel(
    state: IhsgUiState,
    onBaseUrlChanged: (String) -> Unit,
    onSaveBaseUrl: () -> Unit
) {
    CardBlock {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusDot(isConnected = state.error == null && state.connectionStatus.startsWith("Connected:"))
            Column(Modifier.weight(1f)) {
                Text(state.connectionStatus, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Local backend: use 10.0.2.2 on an emulator or an ADB tunnel on a device.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.baseUrl,
                onValueChange = onBaseUrlChanged,
                singleLine = true,
                label = { Text("API Base URL") }
            )
            IconButton(onClick = onSaveBaseUrl) {
                Icon(Icons.Default.Save, contentDescription = "Save URL")
            }
        }
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActionPanel(
    state: IhsgUiState,
    onSymbolChanged: (String) -> Unit,
    onAddSymbol: () -> Unit,
    onMarketReview: () -> Unit
) {
    CardBlock {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.symbolInput,
                onValueChange = onSymbolChanged,
                singleLine = true,
                label = { Text("IDX symbol") },
                placeholder = { Text("BBCA or BBCA.JK") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
            IconButton(onClick = onAddSymbol, enabled = !state.busy) {
                Icon(Icons.Default.Add, contentDescription = "Add stock")
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onMarketReview,
            enabled = !state.busy
        ) {
            Icon(Icons.Default.CandlestickChart, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Run IHSG Market Review")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatchlistChips(symbols: List<String>, onRemoveSymbol: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        symbols.forEach { symbol ->
            AssistChip(
                onClick = { },
                label = { Text(symbol) },
                trailingIcon = {
                    IconButton(modifier = Modifier.size(28.dp), onClick = { onRemoveSymbol(symbol) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove $symbol", modifier = Modifier.size(16.dp))
                    }
                }
            )
        }
    }
}

@Composable
private fun QuoteCard(quote: StockQuote, onAnalyze: (String) -> Unit) {
    val percent = quote.changePercent ?: 0.0
    val tone = when {
        percent > 0.0 -> Color(0xFF0F7B55)
        percent < 0.0 -> Color(0xFFB3261E)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    CardBlock {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = trendIcon(percent),
                contentDescription = null,
                tint = tone,
                modifier = Modifier.size(32.dp)
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(quote.stockCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(formatPercent(percent), color = tone, fontWeight = FontWeight.SemiBold)
                }
                Text(quote.stockName ?: "IDX equity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text(formatRupiah(quote.currentPrice), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Metric("Open", quote.open?.let(::formatCompact) ?: "-")
                    Metric("High", quote.high?.let(::formatCompact) ?: "-")
                    Metric("Low", quote.low?.let(::formatCompact) ?: "-")
                }
                Spacer(Modifier.height(12.dp))
                ElevatedButton(onClick = { onAnalyze(quote.stockCode) }) {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Analyze")
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskInfo, onSelectReport: (String) -> Unit) {
    CardBlock {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (task.status in listOf("pending", "processing")) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            } else {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                Text(task.stockCode.ifBlank { "IHSG Review" }, fontWeight = FontWeight.SemiBold)
                Text("${task.status.uppercase()} ${task.progress}%", style = MaterialTheme.typography.bodySmall)
                task.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                task.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (task.reportText != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onSelectReport(task.reportText) }) {
                Text("Open market review report")
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem) {
    CardBlock {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.stockCode, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    item.sentimentScore?.let {
                        Text("$it/100", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(item.stockName ?: "IDX equity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                item.operationAdvice?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                item.analysisSummary?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                Text(item.createdAt ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReportOverlay(report: String, onClose: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.24f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("IHSG Market Review", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close report")
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(report, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun EmptyState(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusDot(isConnected: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(if (isConnected) Color(0xFF0F7B55) else Color(0xFFB3261E), RoundedCornerShape(50))
    )
}

private fun trendIcon(percent: Double): ImageVector = when {
    percent > 0.0 -> Icons.AutoMirrored.Filled.TrendingUp
    percent < 0.0 -> Icons.AutoMirrored.Filled.TrendingDown
    else -> Icons.AutoMirrored.Filled.TrendingFlat
}

private val indonesiaLocale: Locale = Locale.forLanguageTag("id-ID")
private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(indonesiaLocale)
private val compactFormat: NumberFormat = NumberFormat.getNumberInstance(indonesiaLocale).apply {
    maximumFractionDigits = 2
}

private fun formatRupiah(value: Double): String = rupiahFormat.format(value)
private fun formatCompact(value: Double): String = compactFormat.format(value)
private fun formatPercent(value: Double): String = "${if (value > 0) "+" else ""}${String.format(Locale.US, "%.2f", value)}%"
