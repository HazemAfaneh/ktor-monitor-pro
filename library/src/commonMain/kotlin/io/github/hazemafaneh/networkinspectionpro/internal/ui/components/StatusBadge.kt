package io.github.hazemafaneh.networkinspectionpro.internal.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun StatusBadge(statusCode: Int) {
    val (label, color) = when {
        statusCode == 0           -> "ERR" to Color(0xFF9E9E9E)
        statusCode in 200..299    -> statusCode.toString() to Color(0xFF4CAF50)
        statusCode in 300..399    -> statusCode.toString() to Color(0xFF2196F3)
        statusCode in 400..499    -> statusCode.toString() to Color(0xFFFF9800)
        statusCode in 500..599    -> statusCode.toString() to Color(0xFFE53935)
        else                      -> statusCode.toString() to Color(0xFF9E9E9E)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
