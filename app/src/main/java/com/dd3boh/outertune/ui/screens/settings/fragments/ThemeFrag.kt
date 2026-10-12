/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings.fragments

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DEFAULT_PLAYER_BACKGROUND
import com.dd3boh.outertune.constants.DarkMode
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.DynamicThemeKey
import com.dd3boh.outertune.constants.HighContrastKey
import com.dd3boh.outertune.constants.JixOneThemeKey
import com.dd3boh.outertune.constants.PlayerBackgroundStyle
import com.dd3boh.outertune.constants.PlayerBackgroundStyleKey
import com.dd3boh.outertune.constants.PureBlackKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.theme.JixOneTheme
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference

@Composable
private fun JixOneThemeSwatches(theme: JixOneTheme) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(start = 2.dp)
    ) {
        theme.swatch.forEach { argb ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(argb.toInt()))
            )
        }
    }
}

@Composable
fun ColumnScope.JixOneThemePicker() {
    val (jixThemeId, onJixThemeChange) = rememberPreference(JixOneThemeKey, defaultValue = JixOneTheme.DEFAULT.id)

    JixOneTheme.entries.forEach { theme ->
        val selected = jixThemeId == theme.id
        PreferenceEntry(
            title = {
                Text(
                    text = when (theme) {
                        JixOneTheme.DEFAULT -> stringResource(R.string.jix_theme_default)
                        JixOneTheme.CARPET -> stringResource(R.string.jix_theme_carpet)
                        JixOneTheme.TILE -> stringResource(R.string.jix_theme_tile)
                        JixOneTheme.DRAFSH -> stringResource(R.string.jix_theme_derafsh)
                        JixOneTheme.CYRUS -> stringResource(R.string.jix_theme_cyrus)
                    }
                )
            },
            description = when (theme) {
                JixOneTheme.DEFAULT -> stringResource(R.string.jix_theme_default_desc)
                JixOneTheme.CARPET -> stringResource(R.string.jix_theme_carpet_desc)
                JixOneTheme.TILE -> stringResource(R.string.jix_theme_tile_desc)
                JixOneTheme.DRAFSH -> stringResource(R.string.jix_theme_derafsh_desc)
                JixOneTheme.CYRUS -> stringResource(R.string.jix_theme_cyrus_desc)
            },
            icon = { JixOneThemeSwatches(theme) },
            trailingContent = {
                if (selected) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            onClick = { onJixThemeChange(theme.id) }
        )
    }
}

@Composable
fun ColumnScope.ThemeAppFrag() {
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (highContrastCompat, onHccChange) = rememberPreference(HighContrastKey, defaultValue = false)

    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)

    // ---- JixOne heritage themes (فرش، کاشی، درفش، کوروش) ----
    Text(
        text = stringResource(R.string.jix_theme_section),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
    JixOneThemePicker()
    Spacer(Modifier.height(8.dp))

    SwitchPreference(
        title = { Text(stringResource(R.string.enable_dynamic_theme)) },
        icon = { Icon(Icons.Rounded.Palette, null) },
        checked = dynamicTheme,
        onCheckedChange = onDynamicThemeChange
    )
    AnimatedVisibility(!dynamicTheme) {
        SwitchPreference(
            title = { Text(stringResource(R.string.high_contrast)) },
            description = stringResource(R.string.high_contrast_description),
            icon = { Icon(Icons.Rounded.Contrast, null) },
            checked = highContrastCompat,
            onCheckedChange = onHccChange
        )
    }
    EnumListPreference(
        title = { Text(stringResource(R.string.dark_theme)) },
        icon = { Icon(Icons.Rounded.DarkMode, null) },
        selectedValue = darkMode,
        onValueSelected = onDarkModeChange,
        valueText = {
            when (it) {
                DarkMode.ON -> stringResource(R.string.dark_theme_on)
                DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
            }
        }
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.pure_black)) },
        icon = { Icon(Icons.Rounded.Contrast, null) },
        checked = pureBlack,
        onCheckedChange = onPureBlackChange
    )
}


@Composable
fun ColumnScope.ThemePlayerFrag() {
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = DEFAULT_PLAYER_BACKGROUND
    )
    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    EnumListPreference(
        title = { Text(stringResource(R.string.player_background_style)) },
        icon = { Icon(Icons.Rounded.BlurOn, null) },
        selectedValue = playerBackground,
        onValueSelected = onPlayerBackgroundChange,
        valueText = {
            when (it) {
                PlayerBackgroundStyle.FOLLOW_THEME -> stringResource(R.string.player_background_default)
                PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.player_background_gradient)
                PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
            }
        },
        values = availableBackgroundStyles
    )
}

