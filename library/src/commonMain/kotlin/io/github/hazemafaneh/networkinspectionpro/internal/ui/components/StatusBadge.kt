package io.github.hazemafaneh.networkinspectionpro.internal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun StatusBadge(code: Int?) {
    val (label, color) = when {
        code == null -> "..." to Color(0xFF9E9E9E)
        code < 300 -> code.toString() to Color(0xFF4CAF50)
        code < 400 -> code.toString() to Color(0xFF2196F3)
        code < 500 -> code.toString() to Color(0xFFFF9800)
        else -> code.toString() to Color(0xFFF44336)
    }
    Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
