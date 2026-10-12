/*
 * JixOne native themes — Persian heritage palettes ported from the JixOne web app
 * (github.com/jixa11/JixOne). Each theme builds a full Material 3 scheme via
 * material-color-utilities, with hand-tuned tonal palettes so the surfaces keep
 * the same deep tints as the web original (carpet indigo, tile lapis, drafsh
 * plum, cyrus papyrus).
 *
 * JixOne is GPL-3 licensed like its OuterTune base.
 */

package com.dd3boh.outertune.ui.theme

import com.google.material.color.dynamiccolor.ColorSpec.SpecVersion
import com.google.material.color.dynamiccolor.DynamicScheme
import com.google.material.color.dynamiccolor.Variant
import com.google.material.color.hct.Hct
import com.google.material.color.palettes.TonalPalette
import java.util.Optional

/**
 * The JixOne theme selector. `designedDark` mirrors the web app: carpet/tile/derafsh
 * are night themes by design, cyrus is a papyrus-light theme, DEFAULT keeps
 * following the app dark-mode setting (and dynamic artwork colors).
 */
enum class JixOneTheme(
    val id: String,
    /** null → follow the dark-mode setting; true/false → the theme's designed mode wins */
    val designedDark: Boolean?,
    /** [bg, primary, secondary, tertiary] preview swatches, matching the web app */
    val swatch: List<Long>,
) {
    DEFAULT("default", null, listOf(0xFF141414, 0xFFED5564, 0xFF7A7A7A, 0xFF9E9E9E)),
    CARPET("persian-carpet", true, listOf(0xFF081828, 0xFFD9A441, 0xFFC8763A, 0xFFE8D9B0)),
    TILE("persian-tile", true, listOf(0xFF081226, 0xFF33C5D6, 0xFF4F7FE6, 0xFFD8AA50)),
    DRAFSH("derafsh", true, listOf(0xFF1B0A2A, 0xFFE0B24A, 0xFF9A5BE0, 0xFFC83250)),
    CYRUS("cyrus", false, listOf(0xFFF5EEDE, 0xFFB08A2E, 0xFF2A5D9F, 0xFFC46240));

    companion object {
        fun fromId(id: String?): JixOneTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * A Material 3 scheme assembled from fixed seeds and a tinted neutral axis —
 * the same construction as SchemeTonalSpot, but with the neutral palettes
 * pinned to each theme's background hue instead of a desaturated gray.
 */
class SchemeJixOne(
    primarySeed: Int,
    secondarySeed: Int,
    tertiarySeed: Int,
    neutralHue: Double,
    neutralChroma: Double,
    neutralVariantChroma: Double,
    isDark: Boolean,
) : DynamicScheme(
    /* sourceColorHct */ Hct.fromInt(primarySeed),
    /* variant */ Variant.TONAL_SPOT,
    /* isDark */ isDark,
    /* contrastLevel */ 0.0,
    /* platform */ Platform.PHONE,
    /* specVersion */ SpecVersion.SPEC_2021,
    /* primaryPalette */ TonalPalette.fromInt(primarySeed),
    /* secondaryPalette */ TonalPalette.fromInt(secondarySeed),
    /* tertiaryPalette */ TonalPalette.fromInt(tertiarySeed),
    /* neutralPalette */ TonalPalette.fromHueAndChroma(neutralHue, neutralChroma),
    /* neutralVariantPalette */ TonalPalette.fromHueAndChroma(neutralHue, neutralVariantChroma),
    /* errorPalette */ Optional.of(TonalPalette.fromHueAndChroma(25.0, 84.0)),
)

/** Build the scheme for a JixOne theme, or null for DEFAULT (existing behavior). */
fun jixOneScheme(theme: JixOneTheme, isDark: Boolean): DynamicScheme? = when (theme) {
    JixOneTheme.DEFAULT -> null

    // فرش ایرانی — saffron gold on the deep indigo of a Kashan carpet field
    JixOneTheme.CARPET -> SchemeJixOne(
        primarySeed = 0xFFD9A441.toInt(),   // saffron gold
        secondarySeed = 0xFFC8763A.toInt(), // copper madder
        tertiarySeed = 0xFFE8D9B0.toInt(),  // ivory silk
        neutralHue = 219.0,                 // carpet-field indigo
        neutralChroma = 7.0,
        neutralVariantChroma = 12.0,
        isDark = isDark,
    )

    // کاشی اصفهان — Isfahan tile turquoise over lapis night
    JixOneTheme.TILE -> SchemeJixOne(
        primarySeed = 0xFF33C5D6.toInt(),   // tile turquoise
        secondarySeed = 0xFF4F7FE6.toInt(), // lapis blue
        tertiarySeed = 0xFFD8AA50.toInt(),  // gilded border
        neutralHue = 227.0,                 // lapis night
        neutralChroma = 8.0,
        neutralVariantChroma = 13.0,
        isDark = isDark,
    )

    // درفش کاویانی — the Kaviani banner: royal gold, violet and crimson
    JixOneTheme.DRAFSH -> SchemeJixOne(
        primarySeed = 0xFFE0B24A.toInt(),   // banner gold
        secondarySeed = 0xFF9A5BE0.toInt(), // royal violet
        tertiarySeed = 0xFFC83250.toInt(),  // banner crimson
        neutralHue = 281.0,                 // plum dusk
        neutralChroma = 9.0,
        neutralVariantChroma = 14.0,
        isDark = isDark,
    )

    // کوروش هخامنشی — Achaemenid gold and lapis on papyrus
    JixOneTheme.CYRUS -> SchemeJixOne(
        primarySeed = 0xFFB08A2E.toInt(),   // persepolis gold
        secondarySeed = 0xFF2A5D9F.toInt(), // lapis lazuli
        tertiarySeed = 0xFFC46240.toInt(),  // susiana terracotta
        neutralHue = 45.0,                  // papyrus cream
        neutralChroma = 6.0,
        neutralVariantChroma = 10.0,
        isDark = isDark,
    )
}
