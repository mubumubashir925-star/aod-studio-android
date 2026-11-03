package com.aodstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF080B12)
private val Panel = Color(0xFF151B27)
private val Accent = Color(0xFF8B7CFF)

data class ClockStyle(val title: String, val time: String, val date: String, val color: Color)

private val styles = listOf(
    ClockStyle("Minimal", "10:45", "MONDAY, 03 NOV", Color(0xFFEDEDED)),
    ClockStyle("Violet Glow", "10:45", "MONDAY  •  NOV 03", Color(0xFFB7A8FF)),
    ClockStyle("Ocean", "10:45", "MON 03 NOVEMBER", Color(0xFF73D8E8)),
    ClockStyle("Amber", "10:45", "MONDAY, NOVEMBER 03", Color(0xFFFFC66D)),
    ClockStyle("Clean White", "10:45", "03 NOVEMBER 2026", Color(0xFFFFFFFF)),
    ClockStyle("Cyber", "10:45", "SYSTEM ONLINE  •  03 NOV", Color(0xFF71F2C3))
)

private val palette = listOf(
    Color.White, Color(0xFFB7A8FF), Color(0xFF73D8E8),
    Color(0xFFFFC66D), Color(0xFF71F2C3), Color(0xFFFF8FA3),
    Color(0xFF8DB4FF), Color(0xFFB8C0CC)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AodStudioApp() }
    }
}

@Composable
fun AodStudioApp() {
    var selected by remember { mutableIntStateOf(0) }
    var tab by remember { mutableStateOf("Designs") }
    var customColor by remember { mutableIntStateOf(0) }
    var clockSize by remember { mutableFloatStateOf(56f) }
    var fontChoice by remember { mutableStateOf("Sans") }
    var alignment by remember { mutableStateOf("Center") }
    var format by remember { mutableStateOf("24-hour") }
    var showDate by remember { mutableStateOf(true) }
    var showBattery by remember { mutableStateOf(true) }
    var showDay by remember { mutableStateOf(true) }

    val previewStyle = styles[selected]
    val previewTime = if (format == "12-hour") "10:45 PM" else previewStyle.time
    val fontFamily = when (fontChoice) {
        "Serif" -> FontFamily.Serif
        "Mono" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(containerColor = Ink, bottomBar = {
            NavigationBar(containerColor = Panel) {
                NavigationBarItem(selected = tab == "Designs", onClick = { tab = "Designs" },
                    icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Designs") })
                NavigationBarItem(selected = tab == "Editor", onClick = { tab = "Editor" },
                    icon = { Icon(Icons.Default.Edit, null) }, label = { Text("Editor") })
                NavigationBarItem(selected = tab == "Settings", onClick = { tab = "Settings" },
                    icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
            }
        }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("AOD STUDIO", color = Accent, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(
                            when (tab) {
                                "Editor" -> "Create your look"
                                "Settings" -> "Display settings"
                                else -> "Your screen, your style"
                            },
                            fontSize = 25.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(20.dp))

                when (tab) {
                    "Designs" -> {
                        SectionLabel("LIVE PREVIEW")
                        Spacer(Modifier.height(10.dp))
                        AodPreview(previewStyle, palette[customColor], previewTime, clockSize,
                            fontFamily, alignment, showDate, showDay, showBattery)
                        Spacer(Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ready-made designs", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f))
                            Text("${styles.size} styles", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        styles.forEachIndexed { index, style ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Panel),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)
                                    .clickable { selected = index }
                            ) {
                                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(style.time, color = style.color, fontSize = 25.sp,
                                            fontWeight = FontWeight.Light)
                                        Text(style.title, color = Color.LightGray, fontSize = 12.sp)
                                    }
                                    if (index == selected) Icon(Icons.Default.CheckCircle, null, tint = Accent)
                                    else Text("USE", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    "Editor" -> {
                        SectionLabel("LIVE CUSTOM PREVIEW")
                        Spacer(Modifier.height(10.dp))
                        AodPreview(previewStyle, palette[customColor], previewTime, clockSize,
                            fontFamily, alignment, showDate, showDay, showBattery)
                        Spacer(Modifier.height(22.dp))

                        EditorSection("Clock colour", "Choose a colour for your time") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                palette.forEachIndexed { index, color ->
                                    Box(
                                        Modifier.weight(1f).aspectRatio(1f).background(color, CircleShape)
                                            .clickable { customColor = index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (customColor == index) Icon(Icons.Default.Check, null, tint = Ink)
                                    }
                                }
                            }
                        }

                        EditorSection("Clock size", "${clockSize.toInt()} sp") {
                            Slider(value = clockSize, onValueChange = { clockSize = it },
                                valueRange = 34f..76f, steps = 20)
                        }

                        EditorSection("Font style", "Select the look of your clock") {
                            ChoiceRow(listOf("Sans", "Serif", "Mono"), fontChoice) { fontChoice = it }
                        }

                        EditorSection("Clock format", "Choose how time is displayed") {
                            ChoiceRow(listOf("12-hour", "24-hour"), format) { format = it }
                        }

                        EditorSection("Clock alignment", "Position the clock content") {
                            ChoiceRow(listOf("Left", "Center", "Right"), alignment) { alignment = it }
                        }

                        EditorSection("Display elements", "Choose what appears below the clock") {
                            EditorSwitch("Show date", showDate) { showDate = it }
                            EditorSwitch("Show day", showDay) { showDay = it }
                            EditorSwitch("Show battery", showBattery) { showBattery = it }
                        }

                        Card(colors = CardDefaults.cardColors(containerColor = Panel),
                            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Save, null, tint = Accent)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Design preview only", fontWeight = FontWeight.SemiBold)
                                    Text("Saving designs and background images will be added in the next stage.",
                                        color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                    }

                    else -> {
                        SettingRow("AMOLED black background", "Use a true-black display")
                        SettingRow("Burn-in protection", "Shift clock position periodically")
                        SettingRow("Auto brightness", "Adapt to ambient light")
                        SettingRow("Show battery", "Display battery percentage")
                        SettingRow("Notifications", "Choose which alerts appear")
                        Spacer(Modifier.height(18.dp))
                        Text("AOD activation and lock-screen compatibility will be added after device-permission testing.",
                            color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(label, color = Color.LightGray, fontSize = 11.sp, letterSpacing = 1.5.sp)
}

@Composable
private fun EditorSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 19.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(9.dp))
        content()
    }
}

@Composable
private fun ChoiceRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) { Text(option, fontSize = 11.sp, maxLines = 1) }
        }
    }
}

@Composable
private fun EditorSwitch(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun AodPreview(
    style: ClockStyle,
    override: Color,
    time: String,
    clockSize: Float,
    fontFamily: FontFamily,
    alignment: String,
    showDate: Boolean,
    showDay: Boolean,
    showBattery: Boolean
) {
    val horizontal = when (alignment) {
        "Left" -> Alignment.Start
        "Right" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
    val textAlign = when (alignment) {
        "Left" -> TextAlign.Start
        "Right" -> TextAlign.End
        else -> TextAlign.Center
    }
    Card(colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(205.dp)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp),
            horizontalAlignment = horizontal,
            verticalArrangement = Arrangement.Center
        ) {
            if (showDate) {
                Text(style.date, color = Color.Gray, fontSize = 10.sp,
                    letterSpacing = 1.5.sp, textAlign = textAlign)
                Spacer(Modifier.height(8.dp))
            }
            Text(time, color = override, fontSize = clockSize.sp,
                fontWeight = FontWeight.ExtraLight, letterSpacing = 1.sp,
                fontFamily = fontFamily, textAlign = textAlign, maxLines = 1)
            if (showDay || showBattery) {
                Spacer(Modifier.height(8.dp))
                val details = listOfNotNull(
                    if (showDay) "MON" else null,
                    if (showDay && showBattery) "•" else null,
                    if (showBattery) "72% BATTERY" else null
                ).joinToString("  ")
                Text(details, color = Color.DarkGray, fontSize = 10.sp,
                    letterSpacing = 1.sp, textAlign = textAlign)
            }
        }
    }
}

@Composable
fun SettingRow(title: String, subtitle: String) {
    var enabled by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(checked = enabled, onCheckedChange = { enabled = it })
    }
    HorizontalDivider(color = Panel)
}
