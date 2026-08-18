package com.localaichat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localaichat.app.data.engine.GgufMetadata
import com.localaichat.app.data.model.DefaultPersonas
import com.localaichat.app.data.model.ModelConfig
import com.localaichat.app.ui.theme.Blue600
import com.localaichat.app.ui.theme.Emerald500
import com.localaichat.app.ui.theme.Slate400
import com.localaichat.app.ui.theme.Slate50
import com.localaichat.app.ui.theme.Slate700
import com.localaichat.app.ui.theme.Slate800
import com.localaichat.app.ui.theme.Slate900
import com.localaichat.app.ui.theme.Slate950
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentConfig: ModelConfig,
    modelMetadata: GgufMetadata?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSelectModelClick: () -> Unit,
    onSaveConfig: (ModelConfig) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var temperature by remember { mutableFloatStateOf(currentConfig.temperature) }
    var topP by remember { mutableFloatStateOf(currentConfig.topP) }
    var threadCount by remember { mutableIntStateOf(currentConfig.threadCount) }
    var maxTokens by remember { mutableIntStateOf(currentConfig.maxTokens) }
    var systemPrompt by remember { mutableStateOf(currentConfig.systemPrompt) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Engine Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate50,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate50
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = Slate400
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Slate950
                )
            )
        },
        containerColor = Slate950,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Model Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GGUF Model File",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate50,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Active: ${currentConfig.modelName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )

                    // GGUF Metadata Inspector view
                    if (modelMetadata != null && modelMetadata.isValidGguf) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate950)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Valid GGUF v${modelMetadata.version} Verified", fontSize = 12.sp, color = Emerald500, fontWeight = FontWeight.Bold)
                                }
                                Text("• Architecture: ${modelMetadata.architecture}", fontSize = 12.sp, color = Slate400)
                                Text("• Tensors: ${modelMetadata.tensorCount} layers", fontSize = 12.sp, color = Slate400)
                                if (modelMetadata.fileSizeBytes > 0) {
                                    val sizeMb = modelMetadata.fileSizeBytes / (1024 * 1024)
                                    Text("• File Size: $sizeMb MB", fontSize = 12.sp, color = Slate400)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSelectModelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Choose from Downloads", color = Slate50)
                    }
                }
            }

            // Hardware & Inference Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hardware & Generation Tuning",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate50,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CPU Threads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "CPU Performance Cores", color = Slate50, fontSize = 14.sp)
                        Text(text = "$threadCount Cores", color = Blue600, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = threadCount.toFloat(),
                        onValueChange = { threadCount = it.roundToInt() },
                        valueRange = 1f..8f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = Blue600,
                            activeTrackColor = Blue600,
                            inactiveTrackColor = Slate800
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Temperature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Temperature (Creativity)", color = Slate50, fontSize = 14.sp)
                        Text(text = String.format("%.2f", temperature), color = Blue600, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.1f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Blue600,
                            activeTrackColor = Blue600,
                            inactiveTrackColor = Slate800
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Top-P
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Top-P Sampling", color = Slate50, fontSize = 14.sp)
                        Text(text = String.format("%.2f", topP), color = Blue600, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = topP,
                        onValueChange = { topP = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Blue600,
                            activeTrackColor = Blue600,
                            inactiveTrackColor = Slate800
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Max Tokens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Max Output Tokens", color = Slate50, fontSize = 14.sp)
                        Text(text = "$maxTokens", color = Blue600, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { maxTokens = it.roundToInt() },
                        valueRange = 64f..2048f,
                        colors = SliderDefaults.colors(
                            thumbColor = Blue600,
                            activeTrackColor = Blue600,
                            inactiveTrackColor = Slate800
                        )
                    )
                }
            }

            // System Prompt with quick presets
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom System Prompt",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate50,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DefaultPersonas.list.take(3).forEach { persona ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Slate800)
                                    .clickable { systemPrompt = persona.systemPrompt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "${persona.emoji} ${persona.name.split(" ").first()}", fontSize = 11.sp, color = Slate400)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedTextColor = Slate50,
                            unfocusedTextColor = Slate50,
                            focusedBorderColor = Blue600,
                            unfocusedBorderColor = Slate800
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSaveConfig(
                        currentConfig.copy(
                            temperature = temperature,
                            topP = topP,
                            threadCount = threadCount,
                            maxTokens = maxTokens,
                            systemPrompt = systemPrompt
                        )
                    )
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Save Configuration", color = Slate50, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
