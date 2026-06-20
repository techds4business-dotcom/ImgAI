package com.example.ui.presets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.vector.ImageVector

data class StylePreset(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val promptSuffix: String,
    val description: String
)

object StylePresets {
    val presets = listOf(
        StylePreset(
            id = "none",
            name = "No Preset (Raw)",
            icon = Icons.Default.Casino,
            promptSuffix = "",
            description = "Let your literal text guide the generation without adding stylistic descriptors."
        ),
        StylePreset(
            id = "watercolor",
            name = "Ethereal Watercolor",
            icon = Icons.Default.Brush,
            promptSuffix = "soft pastel colors, artistic dripping ink, textured watercolor paper, delicate color blend, fluid wash, hand-painted aesthetic, highly detailed",
            description = "Soft, fluid pastel pigments running on high-texture paper with bleeding ink effects."
        ),
        StylePreset(
            id = "anime",
            name = "Modern Anime",
            icon = Icons.Default.Layers,
            promptSuffix = "vibrant anime illustration style, precise cel shading, expressive glowing eyes, dynamic light rays, clean line art, high detail, masterpiece",
            description = "Sharp line art and dynamic cel-shaded coloring inspired by modern animated blockbusters."
        ),
        StylePreset(
            id = "synthwave",
            name = "80s Synthwave",
            icon = Icons.Default.ColorLens,
            promptSuffix = "cyberpunk synthwave aesthetic, neon magenta and deep purple glow, retro 1980s grid background, high contrast, laser beams, vaporwave scanlines, atmospheric mist",
            description = "Electrified retro-futuristic vibes with laser grids and a chrome neon twilight palette."
        ),
        StylePreset(
            id = "pixelart",
            name = "16-Bit Pixel Art",
            icon = Icons.Default.Grid3x3,
            promptSuffix = "classic 16-bit retro pixel art, distinct pixel blocks, vibrant limited color palette, clean grid, vintage game console aesthetic, charming video game graphics",
            description = "Charming, grid-aligned vintage game sprites painted in a rich, pixel-precise palette."
        ),
        StylePreset(
            id = "oil_painting",
            name = "Impassioned Oil",
            icon = Icons.Default.Palette,
            promptSuffix = "rich oil painting, thick impasto brush strokes, heavy linen canvas texture, classical Rembrandt chiaroscuro lighting, dramatic shadows, fine oil glaze",
            description = "Heavy textures, rich canvas weave, and dramatic light with realistic, layered paint strokes."
        ),
        StylePreset(
            id = "claymation",
            name = "Playful Claymation",
            icon = Icons.Default.Icecream,
            promptSuffix = "cute 3D claymation stop-motion style, smooth modeled plasticine clay, tactile fingerprint textures, warm studio softlights, hand-sculpted charm",
            description = "Whimsical, hand-molded plasticine models with physical thumbprints and soft studio lighting."
        ),
        StylePreset(
            id = "origami",
            name = "Origami Papercraft",
            icon = Icons.Default.Layers,
            promptSuffix = "low-poly layered paper-cut illustration, textured mulberry paper, sharp geometrical folded origami structures, physically accurate shadows, cast depth",
            description = "Artistic layered papercraft with geometric origami folds and distinct physical depth layers."
        ),
        StylePreset(
            id = "sketch",
            name = "Academic Sketch",
            icon = Icons.Default.Brush,
            promptSuffix = "finely detailed academic graphite pencil sketch, textured cream paper, cross-hatching, charcoal smudging, delicate pencil strokes, hand-drawn realism",
            description = "Intricate graphite shading, pencil grit, and academic cross-hatching on cream card stock."
        )
    )

    fun getById(id: String): StylePreset {
        return presets.find { it.id == id } ?: presets[0]
    }
}
