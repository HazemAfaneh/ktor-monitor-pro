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
internal fun MethodBadge(method: String) {
    val color = when (method.uppercase()) {
        "GET"    -> Color(0xFF4CAF50)
        "POST"   -> Color(0xFF2196F3)
        "PUT"    -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        "PATCH"  -> Color(0xFF9C27B0)
        else     -> Color(0xFF607D8B)
    }
    Text(
        text = method.uppercase(),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
