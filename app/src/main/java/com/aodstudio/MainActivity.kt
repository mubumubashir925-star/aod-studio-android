package com.aodstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var customColor by remember { mutableStateOf(0) }
    val colors = listOf(Color.White, Color(0xFFB7A8FF), Color(0xFF73D8E8), Color(0xFFFFC66D), Color(0xFF71F2C3))
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent, onBackground = Color.White,
        onSurface = Color.White
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
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("AOD STUDIO", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(when(tab) { "Editor" -> "Create your look"; "Settings" -> "Display settings"; else -> "Your screen, your style" },
                            fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(20.dp))
                when(tab) {
                    "Designs" -> {
                        Text("LIVE PREVIEW", color = Color.LightGray, fontSize = 11.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(10.dp))
                        AodPreview(styles[selected], colors[customColor])
                        Spacer(Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ready-made designs", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("${styles.size} styles", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(styles) { style ->
                                Card(colors = CardDefaults.cardColors(containerColor = Panel),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.clickable { selected = styles.indexOf(style) }) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(style.time, color = style.color, fontSize = 25.sp, fontWeight = FontWeight.Light)
                                        Text(style.title, color = Color.LightGray, fontSize = 12.sp)
                                        if (styles.indexOf(style) == selected) Text("SELECTED", color = Accent, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                    "Editor" -> {
                        Text("CUSTOMIZE YOUR CLOCK", color = Color.LightGray, fontSize = 11.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(14.dp))
                        AodPreview(styles[selected], colors[customColor])
                        Spacer(Modifier.height(22.dp))
                        Text("Clock colour", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            colors.forEachIndexed { i, color ->
                                Box(Modifier.size(38.dp).background(color, RoundedCornerShape(50))
                                    .clickable { customColor = i },
                                    contentAlignment = Alignment.Center) {
                                    if (customColor == i) Icon(Icons.Default.Check, null, tint = Ink)
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Text("Clock format", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        var format by remember { mutableStateOf("24-hour") }
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            listOf("12-hour", "24-hour").forEachIndexed { i, s ->
                                SegmentedButton(selected = format == s, onClick = { format = s },
                                    shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(s) }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("More editor controls (font, size, position, widgets and image background) are planned for the next build.",
                            color = Color.Gray, fontSize = 12.sp)
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
            }
        }
    }
}

@Composable
fun AodPreview(style: ClockStyle, override: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(190.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(style.date, color = Color.Gray, fontSize = 10.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(style.time, color = override, fontSize = 55.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text("MON  •  72%  •  BATTERY", color = Color.DarkGray, fontSize = 10.sp, letterSpacing = 1.sp)
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
