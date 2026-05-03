package com.example.game_vol1.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game_vol1.HuntButton
import com.example.game_vol1.HuntColors
import com.example.game_vol1.HuntPanel
import com.example.game_vol1.HuntScaffold
import com.example.game_vol1.HuntTheme
import com.example.game_vol1.HuntTitle

@Composable
fun AdminScrollScreen(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    HuntTheme {
        HuntScaffold { modifier ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (onBack != null) {
                    HuntButton("Back", onBack, color = HuntColors.SlateLight)
                }
                HuntTitle(title, subtitle)
                content()
            }
        }
    }
}

@Composable
fun AdminMetricRow(vararg metrics: Pair<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.forEach { (label, value) ->
            HuntPanel(modifier = Modifier.weight(1f), accent = HuntColors.Blue) {
                Text(value, color = HuntColors.Text, fontWeight = FontWeight.Black, fontSize = 22.sp, maxLines = 1)
                Text(label, color = HuntColors.Muted, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}
