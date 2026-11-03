package com.aodstudio

import android.os.Bundle
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
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
import androidx.compose.ui.platform.LocalContext

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

data class SavedDesign(
    val name: String,
    val styleIndex: Int,
    val colorIndex: Int,
    val clockSize: Float,
    val fontChoice: String,
    val alignment: String,
    val format: String,
    val showDate: Boolean,
    val showDay: Boolean,
    val showBattery: Boolean,
    val backgroundUri: String?
)

private const val PREFS_NAME = "aod_studio_designs"
private const val DESIGNS_KEY = "saved_designs"

private fun readSavedDesigns(context: Context): List<SavedDesign> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(DESIGNS_KEY, "[]") ?: "[]"
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            SavedDesign(
                name = item.optString("name", "My Design"),
                styleIndex = item.optInt("styleIndex", 0).coerceIn(0, styles.lastIndex),
                colorIndex = item.optInt("colorIndex", 0).coerceIn(0, palette.lastIndex),
                clockSize = item.optDouble("clockSize", 56.0).toFloat().coerceIn(34f, 76f),
                fontChoice = item.optString("fontChoice", "Sans"),
                alignment = item.optString("alignment", "Center"),
                format = item.optString("format", "24-hour"),
                showDate = item.optBoolean("showDate", true),
                showDay = item.optBoolean("showDay", true),
                showBattery = item.optBoolean("showBattery", true),
                backgroundUri = item.optString("backgroundUri", "").takeIf { it.isNotBlank() }
            )
        }
    } catch (_: Exception) { emptyList() }
}

private fun writeSavedDesigns(context: Context, designs: List<SavedDesign>) {
    val array = JSONArray()
    designs.forEach { design ->
        array.put(JSONObject().apply {
            put("name", design.name)
            put("styleIndex", design.styleIndex)
            put("colorIndex", design.colorIndex)
            put("clockSize", design.clockSize.toDouble())
            put("fontChoice", design.fontChoice)
            put("alignment", design.alignment)
            put("format", design.format)
            put("showDate", design.showDate)
            put("showDay", design.showDay)
            put("showBattery", design.showBattery)
            put("backgroundUri", design.backgroundUri ?: "")
        })
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(DESIGNS_KEY, array.toString()).apply()
}

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
    var backgroundUri by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            backgroundUri = uri.toString()
        }
    }
    val context = LocalContext.current
    val savedDesigns = remember { mutableStateListOf<SavedDesign>().apply { addAll(readSavedDesigns(context)) } }
    var showSaveDialog by remember { mutableStateOf(false) }
    var designName by remember { mutableStateOf("") }

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
                            fontFamily, alignment, showDate, showDay, showBattery, backgroundUri)
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

                        EditorSection("Background image", "Use a photo from your phone") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Image, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Choose photo")
                                }
                                if (backgroundUri != null) {
                                    OutlinedButton(onClick = { backgroundUri = null }) {
                                        Icon(Icons.Default.Close, null)
                                        Text("Remove")
                                    }
                                }
                            }
                            Text(
                                if (backgroundUri == null) "No custom background selected"
                                else "Custom background selected",
                                color = Color.Gray, fontSize = 11.sp
                            )
                        }

                        EditorSection("Display elements", "Choose what appears below the clock") {
                            EditorSwitch("Show date", showDate) { showDate = it }
                            EditorSwitch("Show day", showDay) { showDay = it }
                            EditorSwitch("Show battery", showBattery) { showBattery = it }
                        }

                        Button(
                            onClick = { designName = "My Design"; showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save this design")
                        }

                        if (savedDesigns.isNotEmpty()) {
                            EditorSection("My saved designs", "${savedDesigns.size} design(s) stored on this phone") {
                                savedDesigns.forEachIndexed { index, design ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Panel),
                                        shape = RoundedCornerShape(13.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text(design.name, fontWeight = FontWeight.SemiBold)
                                                Text("${styles[design.styleIndex].title} • ${design.fontChoice} • ${design.clockSize.toInt()} sp",
                                                    color = Color.Gray, fontSize = 11.sp)
                                            }
                                            TextButton(onClick = {
                                                selected = design.styleIndex
                                                customColor = design.colorIndex
                                                clockSize = design.clockSize
                                                fontChoice = design.fontChoice
                                                alignment = design.alignment
                                                format = design.format
                                                showDate = design.showDate
                                                showDay = design.showDay
                                                showBattery = design.showBattery,
                                                backgroundUri = design.backgroundUri
                                            }) { Text("Apply") }
                                            IconButton(onClick = {
                                                savedDesigns.removeAt(index)
                                                writeSavedDesigns(context, savedDesigns)
                                            }) {
                                                Icon(Icons.Default.DeleteOutline, "Delete design", tint = Color(0xFFFF8A8A))
                                            }
                                        }
                                    }
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

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save your design") },
            text = {
                OutlinedTextField(
                    value = designName,
                    onValueChange = { designName = it.take(30) },
                    label = { Text("Design name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val saved = SavedDesign(
                        name = designName.trim().ifBlank { "My Design" },
                        styleIndex = selected,
                        colorIndex = customColor,
                        clockSize = clockSize,
                        fontChoice = fontChoice,
                        alignment = alignment,
                        format = format,
                        showDate = showDate,
                        showDay = showDay,
                        showBattery = showBattery,
                        backgroundUri = backgroundUri
                    )
                    savedDesigns.add(saved)
                    writeSavedDesigns(context, savedDesigns)
                    showSaveDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            },
            containerColor = Panel
        )
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
    showBattery: Boolean,
    backgroundUri: String?
) {
    val context = LocalContext.current
    val backgroundBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, backgroundUri) {
        value = if (backgroundUri == null) null else withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(Uri.parse(backgroundUri))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) { null }
        }
    }
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
        Box(Modifier.fillMaxSize()) {
            if (backgroundBitmap != null) {
                Image(
                    bitmap = backgroundBitmap!!.asImageBitmap(),
                    contentDescription = "Custom AOD background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
