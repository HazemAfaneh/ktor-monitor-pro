package io.github.hazemafaneh.networkinspectionpro.internal.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
internal fun MethodBadge(method: String) {
    val upper = method.uppercase()
    val bgColor = when (upper) {
        "GET"    -> Color(0xFF2196F3)
        "POST"   -> Color(0xFFFF9800)
        "PUT"    -> Color(0xFF9C27B0)
        "DELETE" -> Color(0xFFE53935)
        "PATCH"  -> Color(0xFFFFEB3B)
        else     -> Color(0xFF607D8B)
    }
    val textColor = if (upper == "PATCH") Color.Black else Color.White

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = upper,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
