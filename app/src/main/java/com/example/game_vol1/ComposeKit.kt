package com.example.game_vol1

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat

object HuntColors {
    val Night = Color(0xFF07111F)
    val Night2 = Color(0xFF0C1B2A)
    val Slate = Color(0xFF162235)
    val SlateLight = Color(0xFF24344D)
    val Surface = Color(0xFF132033)
    val Text = Color(0xFFF8FAFC)
    val Muted = Color(0xFFB7C2D4)
    val Green = Color(0xFF22C55E)
    val Blue = Color(0xFF2563EB)
    val BlueSoft = Color(0xFF93C5FD)
    val Gold = Color(0xFFF59E0B)
    val Rose = Color(0xFFE11D48)
}

@Composable
fun HuntTheme(content: @Composable () -> Unit) {
    HuntSystemBars()
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = HuntColors.Night,
            surface = HuntColors.Surface,
            primary = HuntColors.Green,
            secondary = HuntColors.Blue,
            tertiary = HuntColors.Gold,
            onBackground = HuntColors.Text,
            onSurface = HuntColors.Text,
            onPrimary = Color.White,
        ),
        content = content,
    )
}

@Composable
@Suppress("DEPRECATION")
private fun HuntSystemBars() {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return
    SideEffect {
        window.statusBarColor = HuntColors.Night.toArgb()
        window.navigationBarColor = HuntColors.Night.toArgb()
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}

@Composable
fun HuntScaffold(
    activity: Activity? = null,
    selected: String? = null,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = HuntColors.Night,
        bottomBar = {
            if (activity != null && selected != null) {
                HuntBottomNav(activity, selected)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(HuntColors.Night, HuntColors.Night2, HuntColors.Slate)))
                .padding(padding),
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun HuntPanel(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HuntColors.Surface),
    ) {
        Column(
            modifier = Modifier
                .then(if (accent != null) Modifier.background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.24f), Color.Transparent))) else Modifier)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
fun HuntTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = HuntColors.Text, fontWeight = FontWeight.Black, fontSize = 28.sp, lineHeight = 32.sp)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, color = HuntColors.Muted, fontSize = 14.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun HuntButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = HuntColors.Green) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HuntTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Text(text, color = HuntColors.BlueSoft, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HuntField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
    )
}

@Composable
fun HuntMetric(title: String, value: String, modifier: Modifier = Modifier, color: Color = HuntColors.Gold) {
    HuntPanel(modifier = modifier) {
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 22.sp, maxLines = 1)
        Text(title, color = HuntColors.Muted, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun HuntAction(title: String, subtitle: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    HuntPanel(
        modifier = modifier.clickable(onClick = onClick),
        accent = color,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HuntDot(title.take(1), color)
            Column {
                Text(title, color = HuntColors.Text, fontWeight = FontWeight.Black, fontSize = 17.sp, maxLines = 1)
                Text(subtitle, color = HuntColors.Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun HuntDot(text: String, color: Color) {
    Box(
        modifier = Modifier.size(30.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
fun HuntBottomNav(activity: Activity, selected: String) {
    NavigationBar(containerColor = HuntColors.Surface, contentColor = HuntColors.Text) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            navItem(activity, selected, "map", "Map", "M", HuntColors.Blue, ExplorerMapActivity::class.java, Modifier.weight(1f))
            navItem(activity, selected, "history", "History", "H", HuntColors.Rose, DiscoveriesActivity::class.java, Modifier.weight(1f))
            navItem(activity, selected, "daily", "Daily", "D", HuntColors.Gold, GoalsActivity::class.java, Modifier.weight(1f))
            navItem(activity, selected, "profile", "Base", "B", HuntColors.Green, GeoMenuActivity::class.java, Modifier.weight(1f))
        }
    }
}

@Composable
private fun navItem(
    activity: Activity,
    selected: String,
    key: String,
    label: String,
    dot: String,
    color: Color,
    target: Class<out Activity>,
    modifier: Modifier = Modifier,
) {
    val isSelected = selected == key
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent)
            .clickable {
            if (selected != key) {
                activity.startActivity(Intent(activity, target))
                activity.finish()
            }
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HuntDot(dot, color)
        Text(label, color = if (isSelected) HuntColors.Text else HuntColors.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun VerticalSpacer(height: Int = 12) {
    Spacer(Modifier.height(height.dp))
}
