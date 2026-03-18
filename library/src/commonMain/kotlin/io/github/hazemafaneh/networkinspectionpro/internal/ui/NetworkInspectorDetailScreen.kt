package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hazemafaneh.networkinspectionpro.NetworkLogEntry
import io.github.hazemafaneh.networkinspectionpro.internal.utils.formatJson
import io.github.hazemafaneh.networkinspectionpro.internal.ui.components.MethodBadge
import io.github.hazemafaneh.networkinspectionpro.internal.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkInspectorDetailScreen(
    entry: NetworkLogEntry,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Request", "Response")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MethodBadge(method = entry.method)
                        StatusBadge(code = entry.responseCode)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> OverviewTab(entry)
                1 -> RequestTab(entry)
                2 -> ResponseTab(entry)
            }
        }
    }
}

@Composable
private fun OverviewTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow("URL", entry.url)
        InfoRow("Method", entry.method)
        InfoRow("Status", entry.responseCode?.toString() ?: "Pending")
        InfoRow("Duration", entry.durationMs?.let { "${it}ms" } ?: "—")
        InfoRow("Timestamp", entry.timestampMs.toString())
        if (entry.isError) {
            InfoRow("Error", entry.errorMessage ?: "Request failed")
        }
    }
}

@Composable
private fun RequestTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Headers")
        if (entry.requestHeaders.isEmpty()) {
            Text("No headers", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            entry.requestHeaders.forEach { (k, v) -> InfoRow(k, v) }
        }
        HorizontalDivider()
        SectionTitle("Body")
        if (entry.requestBody.isNullOrBlank()) {
            Text("No body", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            CodeBlock(entry.requestBody)
        }
    }
}

@Composable
private fun ResponseTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Headers")
        if (entry.responseHeaders.isEmpty()) {
            Text("No headers", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            entry.responseHeaders.forEach { (k, v) -> InfoRow(k, v) }
        }
        HorizontalDivider()
        SectionTitle("Body")
        if (entry.responseBody.isNullOrBlank()) {
            Text("No body", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val formatted = try { formatJson(entry.responseBody) } catch (_: Exception) { entry.responseBody }
            CodeBlock(formatted)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CodeBlock(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        modifier = Modifier.horizontalScroll(rememberScrollState())
    )
}
