package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.GeneratedImage
import com.example.ui.presets.StylePreset
import com.example.ui.presets.StylePresets
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtGeneratorScreen(
    viewModel: ArtViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dialog for active full preview image details
    var selectedPreviewImage by remember { mutableStateOf<GeneratedImage?>(null) }

    // Display Error Snackbar/Dialog
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Palette logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Imagine AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu icon",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (uiState.historyList.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.clearAllHistory()
                                Toast.makeText(context, "Gallery history cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Elegant Navigation Tabs
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.activeTab == ArtTab.CREATE,
                    onClick = { viewModel.setTab(ArtTab.CREATE) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = uiState.activeTab == ArtTab.EDIT,
                    onClick = { viewModel.setTab(ArtTab.EDIT) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Preset", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Screen Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // If there's no history yet, display hero banner for onboarding polish
                if (uiState.currentArt == null && uiState.historyList.isEmpty()) {
                    OnboardingBannerDetails()
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Core Form (Depends on Creator vs Editor mode)
                when (uiState.activeTab) {
                    ArtTab.CREATE -> {
                        CreateArtTabPanel(
                            uiState = uiState,
                            viewModel = viewModel,
                            onCloseKeyboard = { keyboardController?.hide() }
                        )
                    }
                    ArtTab.EDIT -> {
                        EditArtTabPanel(
                            uiState = uiState,
                            viewModel = viewModel,
                            onCloseKeyboard = { keyboardController?.hide() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Render Active Generated Art Canvas
                ActiveOutputCanvasArea(
                    uiState = uiState,
                    viewModel = viewModel,
                    onEditBaseImage = { image ->
                        viewModel.selectBaseImage(image)
                        viewModel.setTab(ArtTab.EDIT)
                        Toast.makeText(context, "Loaded in to Edit canvas", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Gallery History Grid
                ArtHistoryGallerySection(
                    uiState = uiState,
                    onImageClick = { selectedPreviewImage = it },
                    onDeleteImage = { image ->
                        viewModel.deleteHistoryItem(image)
                        Toast.makeText(context, "Removed from history", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal Details Preview Dialog
    selectedPreviewImage?.let { image ->
        ArtDetailsDialog(
            image = image,
            onDismiss = { selectedPreviewImage = null },
            onDelete = {
                viewModel.deleteHistoryItem(image)
                selectedPreviewImage = null
                Toast.makeText(context, "Art canvas deleted", Toast.LENGTH_SHORT).show()
            },
            onUseAsBase = {
                viewModel.selectBaseImage(image)
                viewModel.setTab(ArtTab.EDIT)
                selectedPreviewImage = null
                Toast.makeText(context, "Base image set for editing", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun OnboardingBannerDetails() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(4.dp, Color.White),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GeoGradientStart.copy(alpha = 0.25f),
                            GeoGradientEnd.copy(alpha = 0.25f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ready to create.",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter a prompt below. Direct Gemini's neural brushstrokes using curated presets.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CreateArtTabPanel(
    uiState: ArtUiState,
    viewModel: ArtViewModel,
    onCloseKeyboard: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Enter Art Prompt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.prompt,
                onValueChange = { viewModel.updatePrompt(it) },
                placeholder = {
                    Text(
                        "Describe what you want to generate. E.g., 'A mystical glowing white owl landing on a branch under a brilliant supernova...'",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .testTag("prompt_input"),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onCloseKeyboard()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Select Style Preset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally Scrollable Preset Selectors
            PresetHorizontalSelector(
                selectedId = uiState.selectedPresetId,
                onPresetSelected = { viewModel.selectPreset(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Aspect Ratio Setup
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aspect Ratio",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ConfigSelector(
                        selectedOption = uiState.aspectRatio,
                        options = listOf("1:1", "16:9", "4:3", "9:16"),
                        onOptionSelected = { viewModel.selectAspectRatio(it) }
                    )
                }

                // Image Size Setup
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Output Quality",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ConfigSelector(
                        selectedOption = uiState.imageSize,
                        options = listOf("1K", "2K", "4K"),
                        onOptionSelected = { viewModel.selectImageSize(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA Button styled fully rounded bg-[#6750a4] text-white as in HTML design spec
            Button(
                onClick = {
                    onCloseKeyboard()
                    viewModel.generateArt()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(27.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Cyclone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Spark Art Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditArtTabPanel(
    uiState: ArtUiState,
    viewModel: ArtViewModel,
    onCloseKeyboard: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Active Editing Base",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.selectedBaseImage == null) {
                // Empty state warning for selecting base
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please touch any image in the history gallery below and tap 'Edit Base' to load it here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Show loaded edit base target details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(uiState.selectedBaseImage.localFilePath))
                            .build(),
                        contentDescription = "Editing Base image",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Based on: \"${uiState.selectedBaseImage.prompt}\"",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Preset style: ${uiState.selectedBaseImage.selectedPreset}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.selectBaseImage(null) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Remove base image",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Describe Editing instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.editPrompt,
                onValueChange = { viewModel.updateEditPrompt(it) },
                placeholder = {
                    Text(
                        "Enter edit instructions. E.g., 'Add a cute neon cyberpunk helmet over the cat and make eyes glow neon pink.'",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .testTag("edit_input"),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onCloseKeyboard()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 3,
                enabled = uiState.selectedBaseImage != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ratio & Quality defaults inside edit mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aspect Ratio",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ConfigSelector(
                        selectedOption = uiState.aspectRatio,
                        options = listOf("1:1", "16:9", "4:3", "9:16"),
                        onOptionSelected = { viewModel.selectAspectRatio(it) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Output Quality",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ConfigSelector(
                        selectedOption = uiState.imageSize,
                        options = listOf("1K", "2K", "4K"),
                        onOptionSelected = { viewModel.selectImageSize(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA Edit Button
            Button(
                onClick = {
                    onCloseKeyboard()
                    viewModel.editArt()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("edit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                enabled = !uiState.isLoading && uiState.selectedBaseImage != null,
                shape = RoundedCornerShape(27.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Execute Model Edit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetHorizontalSelector(
    selectedId: String,
    onPresetSelected: (String) -> Unit
) {
    Column {
        ScrollableTabRow(
            selectedTabIndex = StylePresets.presets.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            StylePresets.presets.forEach { preset ->
                val isSelected = preset.id == selectedId

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp, 
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onPresetSelected(preset.id) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = preset.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
        
        // Active description
        val currentPreset = StylePresets.getById(selectedId)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = currentPreset.description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ConfigSelector(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun ActiveOutputCanvasArea(
    uiState: ArtUiState,
    viewModel: ArtViewModel,
    onEditBaseImage: (GeneratedImage) -> Unit
) {
    val context = LocalContext.current
    AnimatedVisibility(
        visible = uiState.isLoading || uiState.currentArt != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isLoading) "Neural brush busy..." else "Artistic Creation Unleashed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isLoading) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                    
                    if (!uiState.isLoading) {
                        IconButton(onClick = { viewModel.clearResultArt() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Canvas",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoading) {
                    // Processing / Loading State with animated infinite transitions
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "canvas")
                        val rotationSpin by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spin"
                        )

                        val scalePulse by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .drawBehind {
                                        drawCircle(
                                            Brush.sweepGradient(
                                                listOf(
                                                    GeoPrimary, 
                                                    GeoSunsetTertiary, 
                                                    GeoGradientStart, 
                                                    GeoPrimary
                                                )
                                            ),
                                            alpha = 0.8f
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cyclone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = uiState.loadingMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = "Models processing takes up to 10 seconds...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (uiState.currentArt != null) {
                    // Render Image loaded safely from cached local file path
                    val absolutePath = uiState.currentArt.localFilePath
                    val fileExists = File(absolutePath).exists()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                when (uiState.currentArt.aspectRatio) {
                                    "16:9" -> 1.77f
                                    "4:3" -> 1.33f
                                    "9:16" -> 0.56f
                                    else -> 1f
                                }
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (fileExists) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(File(absolutePath))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Latest Generated Art",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // File not found fallback
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Artwork file loading context lost.", color = MaterialTheme.colorScheme.tertiary)
                            }
                        }

                        // Floating badges for quick tags
                        Row(
                            Modifier
                                .padding(8.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(uiState.currentArt.selectedPreset) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.Black.copy(alpha = 0.7f),
                                    labelColor = GeoGradientStart
                                )
                            )
                            
                            if (uiState.currentArt.isEdited) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("EDITED") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = Color.Black.copy(alpha = 0.7f),
                                        labelColor = GeoSunsetTertiary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Artwork specifications metadata
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Prompt: \"${uiState.currentArt.prompt}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (uiState.currentArt.isEdited && uiState.currentArt.editPrompt != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Applied edits: \"${uiState.currentArt.editPrompt}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ratio: ${uiState.currentArt.aspectRatio} | Size: ${uiState.currentArt.imageSize}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Saved Offline locally",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons of quick manipulation on the output
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onEditBaseImage(uiState.currentArt)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit as base", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Saved image details stored securely offline.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify Save Offline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtHistoryGallerySection(
    uiState: ArtUiState,
    onImageClick: (GeneratedImage) -> Unit,
    onDeleteImage: (GeneratedImage) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Neural Artwork Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "${uiState.historyList.size} Saved Items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "History gallery is currently empty",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spark the engine above to paint your first masterpiece.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Adaptive Grid layout
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 1200.dp), // Set standard bounds for nesting in Column scroll
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false // Nest scroll context safely
            ) {
                items(uiState.historyList, key = { it.id }) { image ->
                    ArtGalleryItemCard(
                        image = image,
                        onImageClick = { onImageClick(image) },
                        onDeleteImage = { onDeleteImage(image) }
                    )
                }
            }
        }
    }
}

@Composable
fun ArtGalleryItemCard(
    image: GeneratedImage,
    onImageClick: () -> Unit,
    onDeleteImage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onImageClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(image.localFilePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = image.prompt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = image.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = image.selectedPreset,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = if (image.isEdited) Icons.Default.AutoFixHigh else Icons.Default.Collections,
                            contentDescription = null,
                            tint = if (image.isEdited) GeoSunsetTertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Quick Delete Button floating overlay
            IconButton(
                onClick = { onDeleteImage() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.Red,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ArtDetailsDialog(
    image: GeneratedImage,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUseAsBase: () -> Unit
) {
    val file = File(image.localFilePath)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Canvas Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (file.exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(file)
                                .build(),
                            contentDescription = image.prompt,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Cached file no longer present", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata list details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Original Prompt: \"${image.prompt}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (image.isEdited && image.editPrompt != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Modifications applied: \"${image.editPrompt}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Preset style: ${image.selectedPreset}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Aspect: ${image.aspectRatio} | Quality: ${image.imageSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.15f),
                            contentColor = Color.Red
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onUseAsBase,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Set as base Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
