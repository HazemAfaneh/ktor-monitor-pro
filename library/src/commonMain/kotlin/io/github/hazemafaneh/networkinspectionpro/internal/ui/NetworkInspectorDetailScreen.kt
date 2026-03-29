package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hazemafaneh.networkinspectionpro.NetworkLogEntry
import io.github.hazemafaneh.networkinspectionpro.NetworkLogStore
import io.github.hazemafaneh.networkinspectionpro.internal.ui.components.MethodBadge
import io.github.hazemafaneh.networkinspectionpro.internal.utils.formatTimestamp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import androidx.compose.runtime.CompositionLocalProvider

// ── JSON Syntax Colors ──────────────────────────────────────────────────────

private val JsonKeyColor     = Color(0xFF9CDCFE)
private val JsonStringColor  = Color(0xFFCE9178)
private val JsonNumberColor  = Color(0xFFB5CEA8)
private val JsonBoolColor    = Color(0xFF569CD6)
private val JsonNullColor    = Color(0xFF808080)
private val JsonBracketColor = Color(0xFFD4D4D4)
private val JsonMetaColor    = Color(0xFF6A9955)

// ── Flat JSON tree model ─────────────────────────────────────────────────────

private enum class FlatRowType { LEAF, CONTAINER_OPEN, CONTAINER_CLOSE }

private data class FlatJsonRow(
    val path: String,
    val ancestorPaths: List<String>,
    val key: String?,
    val element: JsonElement,
    val depth: Int,
    val type: FlatRowType,
    val isExpanded: Boolean = false
)

private fun flattenJson(
    element: JsonElement,
    path: String,
    key: String?,
    depth: Int,
    expandedPaths: Set<String>,
    ancestors: List<String>,
    result: MutableList<FlatJsonRow>
) {
    when (element) {
        is JsonPrimitive -> {
            result.add(FlatJsonRow(path, ancestors, key, element, depth, FlatRowType.LEAF))
        }
        is JsonObject, is JsonArray -> {
            val isExpanded = path in expandedPaths
            result.add(FlatJsonRow(path, ancestors, key, element, depth, FlatRowType.CONTAINER_OPEN, isExpanded))
            if (isExpanded) {
                val newAncestors = ancestors + path
                when (element) {
                    is JsonObject -> element.entries.forEach { (k, v) ->
                        flattenJson(v, "$path.$k", k, depth + 1, expandedPaths, newAncestors, result)
                    }
                    is JsonArray -> element.forEachIndexed { i, v ->
                        flattenJson(v, "$path[$i]", null, depth + 1, expandedPaths, newAncestors, result)
                    }
                    else -> {}
                }
                result.add(FlatJsonRow("$path#close", ancestors, null, element, depth, FlatRowType.CONTAINER_CLOSE))
            }
        }
        JsonNull -> {
            result.add(FlatJsonRow(path, ancestors, key, element, depth, FlatRowType.LEAF))
        }
    }
}

private fun flattenAll(element: JsonElement): List<FlatJsonRow> {
    val allPaths = mutableSetOf<String>()
    collectAllPaths(element, "root", allPaths)
    val result = mutableListOf<FlatJsonRow>()
    flattenJson(element, "root", null, 0, allPaths, emptyList(), result)
    return result
}

private fun collectAllPaths(element: JsonElement, path: String, result: MutableSet<String>) {
    when (element) {
        is JsonObject -> {
            result.add(path)
            element.entries.forEach { (k, v) -> collectAllPaths(v, "$path.$k", result) }
        }
        is JsonArray -> {
            result.add(path)
            element.forEachIndexed { i, v -> collectAllPaths(v, "$path[$i]", result) }
        }
        else -> {}
    }
}

private fun rowPlainText(row: FlatJsonRow): String {
    val sb = StringBuilder()
    if (row.key != null) sb.append("\"${row.key}\": ")
    when (row.type) {
        FlatRowType.CONTAINER_CLOSE -> {
            sb.append(if (row.element is JsonArray) "]" else "}")
        }
        FlatRowType.CONTAINER_OPEN -> {
            if (row.isExpanded) {
                sb.append(if (row.element is JsonArray) "[" else "{")
            } else {
                val count = when (row.element) {
                    is JsonArray  -> row.element.size
                    is JsonObject -> row.element.size
                    else          -> 0
                }
                val (o, c) = if (row.element is JsonArray) "[" to "]" else "{" to "}"
                sb.append("$o $count items $c")
            }
        }
        FlatRowType.LEAF -> {
            val prim = row.element as JsonPrimitive
            if (prim.isString) sb.append("\"${prim.content}\"") else sb.append(prim.content)
        }
    }
    return sb.toString()
}

private fun buildRowAnnotatedString(row: FlatJsonRow): AnnotatedString = buildAnnotatedString {
    if (row.key != null) {
        withStyle(SpanStyle(color = JsonKeyColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) {
            append("\"${row.key}\"")
        }
        withStyle(SpanStyle(color = JsonBracketColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) {
            append(": ")
        }
    }
    when (row.type) {
        FlatRowType.CONTAINER_CLOSE -> {
            withStyle(SpanStyle(color = JsonBracketColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) {
                append(if (row.element is JsonArray) "]" else "}")
            }
        }
        FlatRowType.CONTAINER_OPEN -> {
            if (row.isExpanded) {
                withStyle(SpanStyle(color = JsonBracketColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) {
                    append(if (row.element is JsonArray) "[" else "{")
                }
            } else {
                val count = when (row.element) {
                    is JsonArray  -> row.element.size
                    is JsonObject -> row.element.size
                    else          -> 0
                }
                val (o, c) = if (row.element is JsonArray) "[" to "]" else "{" to "}"
                withStyle(SpanStyle(color = JsonBracketColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) { append(o) }
                withStyle(SpanStyle(color = JsonMetaColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) { append(" $count items ") }
                withStyle(SpanStyle(color = JsonBracketColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)) { append(c) }
            }
        }
        FlatRowType.LEAF -> {
            val prim = row.element as? JsonPrimitive ?: return@buildAnnotatedString
            val style = when {
                prim.isString                   -> SpanStyle(color = JsonStringColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                prim.content == "null"          -> SpanStyle(color = JsonNullColor,   fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                prim.content == "true"
                || prim.content == "false"      -> SpanStyle(color = JsonBoolColor,   fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                else                            -> SpanStyle(color = JsonNumberColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
            withStyle(style) {
                if (prim.isString) append("\"${prim.content}\"") else append(prim.content)
            }
        }
    }
}

private fun applySearchHighlights(
    base: AnnotatedString,
    plainText: String,
    query: String,
    matchRanges: List<IntRange>,
    isCurrentMatchRow: Boolean,
    currentMatchStart: Int
): AnnotatedString {
    if (query.isBlank() || matchRanges.isEmpty()) return base
    return buildAnnotatedString {
        append(base)
        for (range in matchRanges) {
            val isCurrent = isCurrentMatchRow && range.first == currentMatchStart
            addStyle(
                SpanStyle(
                    background = if (isCurrent) Color(0xFFFF8C00) else Color(0xFFFFFF00),
                    color = Color.Black
                ),
                range.first, range.last + 1
            )
        }
    }
}

private fun findOccurrences(text: String, query: String): List<IntRange> {
    if (query.isBlank()) return emptyList()
    val result = mutableListOf<IntRange>()
    var index = text.indexOf(query, ignoreCase = true)
    while (index != -1) {
        result.add(index until index + query.length)
        index = text.indexOf(query, index + 1, ignoreCase = true)
    }
    return result
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun extractPath(url: String): String {
    return try {
        val withoutProtocol = if ("://" in url) url.substringAfter("://") else url
        val afterHost = withoutProtocol.substringAfter("/", "")
        "/" + afterHost.split("?").first()
    } catch (_: Exception) { url }
}

private fun extractQueryParams(url: String): List<Pair<String, String>> {
    val queryString = url.substringAfter("?", "")
    if (queryString.isBlank()) return emptyList()
    return queryString.split("&").mapNotNull { param ->
        val eq = param.indexOf('=')
        if (eq == -1) null else param.substring(0, eq) to param.substring(eq + 1)
    }
}

private fun generateCurl(entry: NetworkLogEntry): String {
    val sb = StringBuilder("curl -X ${entry.method}")
    entry.requestHeaders.forEach { (k, v) ->
        sb.append(" \\\n  -H '${k}: ${v}'")
    }
    if (!entry.requestBody.isNullOrBlank()) {
        sb.append(" \\\n  -d '${entry.requestBody}'")
    }
    sb.append(" \\\n  '${entry.url}'")
    return sb.toString()
}

private fun parseJsonSafe(text: String): JsonElement? = try {
    Json.parseToJsonElement(text)
} catch (_: Exception) { null }

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkInspectorDetailScreen(
    logEntryId: String,
    onBack: () -> Unit
) {
    val allEntries by NetworkLogStore.entries.collectAsState()
    val entry = remember(allEntries, logEntryId) { allEntries.firstOrNull { it.id == logEntryId } }
    val clipboard = LocalClipboardManager.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        if (entry != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MethodBadge(method = entry.method)
                                Text(
                                    text = extractPath(entry.url),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text("Network Detail", color = Color.White)
                        }
                    },
                    actions = {
                        if (entry != null) {
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString(generateCurl(entry)))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Copy cURL", tint = Color.White)
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (entry == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Log entry not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                DetailContent(
                    entry = entry,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

// ── DetailContent ─────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(entry: NetworkLogEntry, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Response", "Request", "Headers")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTab) {
            0 -> ResponseBodyTab(body = entry.responseBody)
            1 -> BodyTab(entry = entry)
            2 -> HeadersTab(entry = entry)
        }
    }
}

// ── Tab 0: Response Body ──────────────────────────────────────────────────────

@Composable
private fun ResponseBodyTab(body: String?) {
    val clipboard = LocalClipboardManager.current
    var query by remember { mutableStateOf("") }

    val parsedJson = remember(body) { if (body.isNullOrBlank()) null else parseJsonSafe(body) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                placeholder = { Text("Search…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }}
                } else null,
                textStyle = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = { clipboard.setText(AnnotatedString(body ?: "")) }) {
                Icon(Icons.Default.Share, contentDescription = "Copy body", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Content
        if (body.isNullOrBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No body", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (parsedJson != null) {
            JsonTreeView(rootElement = parsedJson, searchQuery = query)
        } else {
            val plainOccurrences = remember(body, query) { findOccurrences(body, query) }
            val annotated = remember(body, query, plainOccurrences) {
                buildAnnotatedString {
                    append(body)
                    for (range in plainOccurrences) {
                        addStyle(SpanStyle(background = Color(0xFFFFFF00), color = Color.Black), range.first, range.last + 1)
                    }
                }
            }
            val scroll = rememberScrollState()
            Text(
                text = annotated,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(12.dp)
            )
        }
    }
}

// ── JsonTreeView ──────────────────────────────────────────────────────────────

@Composable
private fun JsonTreeView(rootElement: JsonElement, searchQuery: String) {
    var userExpanded by remember { mutableStateOf(setOf("root")) }
    var currentMatchIndex by remember(searchQuery) { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Compute all rows fully expanded (for search)
    val allRows = remember(rootElement) { flattenAll(rootElement) }

    // Compute which rows match the query across the full tree
    val allMatchRows = remember(allRows, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allRows.filter { row ->
            rowPlainText(row).contains(searchQuery, ignoreCase = true)
        }
    }

    // Auto-expand ancestors of all matching rows
    val searchExpandedPaths = remember(allMatchRows) {
        allMatchRows.flatMap { it.ancestorPaths }.toSet()
    }

    // Merged expanded paths
    val expandedPaths by remember(userExpanded, searchExpandedPaths) {
        derivedStateOf { userExpanded + searchExpandedPaths }
    }

    // Flat visible rows based on expanded state
    val visibleRows = remember(rootElement, expandedPaths) {
        mutableListOf<FlatJsonRow>().also { result ->
            flattenJson(rootElement, "root", null, 0, expandedPaths, emptyList(), result)
        }
    }

    // Compute match info per visible row
    data class RowMatchInfo(val ranges: List<IntRange>)
    val matchInfoMap = remember(visibleRows, searchQuery) {
        if (searchQuery.isBlank()) emptyMap()
        else visibleRows.mapIndexed { idx, row ->
            val text = rowPlainText(row)
            val ranges = findOccurrences(text, searchQuery)
            idx to RowMatchInfo(ranges)
        }.filter { it.second.ranges.isNotEmpty() }.toMap()
    }

    // Global match list: list of (visibleRowIndex, rangeIndex)
    val globalMatches = remember(matchInfoMap) {
        matchInfoMap.entries
            .sortedBy { it.key }
            .flatMap { (rowIdx, info) -> info.ranges.mapIndexed { ri, _ -> rowIdx to ri } }
    }

    val totalMatches = globalMatches.size

    // Clamp currentMatchIndex when search changes
    LaunchedEffect(searchQuery) {
        currentMatchIndex = 0
    }

    // Search bar row (counter + prev/next)
    Column(modifier = Modifier.fillMaxSize()) {
        if (searchQuery.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (totalMatches > 0) "${currentMatchIndex + 1}/$totalMatches" else "0/0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = {
                    if (totalMatches > 0) currentMatchIndex = (currentMatchIndex - 1 + totalMatches) % totalMatches
                }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
                }
                IconButton(onClick = {
                    if (totalMatches > 0) currentMatchIndex = (currentMatchIndex + 1) % totalMatches
                }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
                }
            }
        }

        // Scroll to current match row
        LaunchedEffect(currentMatchIndex, globalMatches) {
            if (globalMatches.isNotEmpty()) {
                val targetRow = globalMatches[currentMatchIndex.coerceIn(0, globalMatches.lastIndex)].first
                scope.launch { listState.animateScrollToItem(targetRow) }
            }
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(visibleRows, key = { _, row -> row.path }) { rowIdx, row ->
                val matchInfo = matchInfoMap[rowIdx]
                val isCurrentMatchRow = globalMatches.isNotEmpty() &&
                        globalMatches[currentMatchIndex.coerceIn(0, globalMatches.lastIndex)].first == rowIdx
                val currentMatchRangeIdx = if (isCurrentMatchRow)
                    globalMatches[currentMatchIndex.coerceIn(0, globalMatches.lastIndex)].second else -1
                val currentMatchStart = if (currentMatchRangeIdx >= 0 && matchInfo != null)
                    matchInfo.ranges[currentMatchRangeIdx].first else -1

                JsonRowItem(
                    row = row,
                    searchQuery = searchQuery,
                    matchRanges = matchInfo?.ranges ?: emptyList(),
                    isCurrentMatchRow = isCurrentMatchRow,
                    currentMatchStart = currentMatchStart,
                    onToggle = {
                        if (row.type == FlatRowType.CONTAINER_OPEN) {
                            userExpanded = if (row.path in userExpanded)
                                userExpanded - row.path
                            else
                                userExpanded + row.path
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun JsonRowItem(
    row: FlatJsonRow,
    searchQuery: String,
    matchRanges: List<IntRange>,
    isCurrentMatchRow: Boolean,
    currentMatchStart: Int,
    onToggle: () -> Unit
) {
    val isContainer = row.type == FlatRowType.CONTAINER_OPEN
    val rowBg = when {
        isCurrentMatchRow && matchRanges.isNotEmpty() -> Color(0x33FF8C00)
        matchRanges.isNotEmpty() -> Color(0x22FFFF00)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .then(if (isContainer) Modifier.clickable(onClick = onToggle) else Modifier)
            .padding(start = (row.depth * 12 + 6).dp, top = 2.dp, bottom = 2.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isContainer) {
            Icon(
                imageVector = if (row.isExpanded) Icons.Default.KeyboardArrowDown
                              else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = JsonBracketColor
            )
            Spacer(Modifier.width(2.dp))
        } else {
            Spacer(Modifier.width(16.dp))
        }

        val base = buildRowAnnotatedString(row)
        val plainText = rowPlainText(row)
        val annotated = applySearchHighlights(base, plainText, searchQuery, matchRanges, isCurrentMatchRow, currentMatchStart)
        Text(text = annotated, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

// ── Tab 1: Request Body ───────────────────────────────────────────────────────

@Composable
private fun BodyTab(entry: NetworkLogEntry) {
    val clipboard = LocalClipboardManager.current
    var query by remember { mutableStateOf("") }

    val queryParams = remember(entry.url) { extractQueryParams(entry.url) }
    val bodyText = remember(entry.requestBody, queryParams) {
        buildString {
            if (queryParams.isNotEmpty()) {
                append("// Query Parameters\n")
                queryParams.forEach { (k, v) -> append("$k = $v\n") }
                append("\n")
            }
            if (!entry.requestBody.isNullOrBlank()) {
                append("// Request Body\n")
                val pretty = try {
                    val elem = Json.parseToJsonElement(entry.requestBody)
                    Json { prettyPrint = true }.encodeToString(JsonElement.serializer(), elem)
                } catch (_: Exception) { entry.requestBody }
                append(pretty)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                placeholder = { Text("Search…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }}
                } else null,
                textStyle = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = { clipboard.setText(AnnotatedString(bodyText)) }) {
                Icon(Icons.Default.Share, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (bodyText.isBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No body", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val occurrences = remember(bodyText, query) { findOccurrences(bodyText, query) }
            val annotated = remember(bodyText, query, occurrences) {
                buildAnnotatedString {
                    append(bodyText)
                    for (range in occurrences) {
                        addStyle(SpanStyle(background = Color.Yellow, color = Color.Black), range.first, range.last + 1)
                    }
                }
            }
            val scroll = rememberScrollState()
            Text(
                text = annotated,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(12.dp)
            )
        }
    }
}

// ── Tab 2: Headers ────────────────────────────────────────────────────────────

@Composable
private fun HeadersTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExpandableSection(title = "Overview", initiallyExpanded = true) {
            KeyValueRow(label = "Status", value = entry.responseCode?.toString() ?: "—")
            KeyValueRow(label = "Method", value = entry.method)
            KeyValueRow(label = "URL", value = entry.url)
            KeyValueRow(label = "Duration", value = entry.durationMs?.let { "${it}ms" } ?: "—")
            KeyValueRow(label = "Timestamp", value = formatTimestamp(entry.timestampMs))
        }

        ExpandableSection(
            title = "Request Headers (${entry.requestHeaders.size})",
            initiallyExpanded = true
        ) {
            entry.requestHeaders.entries.forEachIndexed { i, (k, v) ->
                KeyValueRow(label = k, value = v, copyText = "$k: $v")
                if (i < entry.requestHeaders.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            if (entry.requestHeaders.isEmpty()) {
                Text("No headers", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        ExpandableSection(
            title = "Response Headers (${entry.responseHeaders.size})",
            initiallyExpanded = false
        ) {
            entry.responseHeaders.entries.forEachIndexed { i, (k, v) ->
                KeyValueRow(label = k, value = v, copyText = "$k: $v")
                if (i < entry.responseHeaders.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            if (entry.responseHeaders.isEmpty()) {
                Text("No headers", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: String, copyText: String? = null) {
    val clipboard = LocalClipboardManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
        if (copyText != null) {
            IconButton(onClick = { clipboard.setText(AnnotatedString(copyText)) }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
