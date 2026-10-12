/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O﻿ute﻿rTu﻿ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.BuildConfig
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ENABLE_FFMETADATAEX
import com.dd3boh.outertune.constants.LYRIC_FETCH_TIMEOUT
import com.dd3boh.outertune.constants.MAX_LM_SCANNER_JOBS
import com.dd3boh.outertune.constants.OOBE_VERSION
import com.dd3boh.outertune.constants.SNACKBAR_VERY_SHORT
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.ui.component.ColumnWithContentPadding
import com.dd3boh.outertune.ui.component.ContributorCard
import com.dd3boh.outertune.ui.component.ContributorInfo
import com.dd3boh.outertune.ui.component.ContributorType.CUSTOM
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SettingsClickToReveal
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.component.button.IconLabelButton
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.scanners.FFmpegScanner
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.FfmpegLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** The JixOne release channel — APKs are published here by CI. */
private const val JIXONE_REPO = "jixa11/JixOne-Android"

private data class JixUpdateInfo(
    val currentVersion: String,
    val latestVersion: String?,
    val apkUrl: String?,
    val releaseUrl: String?,
)

/** '1.10.0' > '1.9.2' → true ; tolerates a leading v */
private fun versionNewer(latest: String, current: String): Boolean {
    val norm = { s: String -> s.trim().trimStart('v', 'V') }
    val a = norm(latest).split('.').map { it.toIntOrNull() ?: 0 }
    val b = norm(current).split('.').map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(a.size, b.size)) {
        val d = (a.getOrNull(i) ?: 0) - (b.getOrNull(i) ?: 0)
        if (d != 0) return d > 0
    }
    return false
}

private fun fetchLatestRelease(): JixUpdateInfo? = try {
    val conn = URL("https://api.github.com/repos/$JIXONE_REPO/releases/latest").openConnection() as HttpURLConnection
    conn.connectTimeout = 10000
    conn.readTimeout = 10000
    conn.setRequestProperty("Accept", "application/vnd.github+json")
    conn.setRequestProperty("User-Agent", "JixOne-UpdateCheck")
    if (conn.responseCode != 200) return null
    val obj = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
    val tag = obj.optString("tag_name", "")
    if (tag.isEmpty()) return null
    var apk: String? = null
    val assets = obj.optJSONArray("assets")
    if (assets != null) {
        for (i in 0 until assets.length()) {
            val a = assets.getJSONObject(i)
            if (a.optString("name", "").endsWith(".apk", true) && a.optString("state") == "uploaded") {
                apk = a.optString("browser_download_url"); break
            }
        }
    }
    JixUpdateInfo(BuildConfig.VERSION_NAME, tag, apk, obj.optString("html_url", ""))
} catch (e: Exception) {
    null
}

@Composable
private fun UpdateCheckCard(uriHandler: UriHandler) {
    var state by remember { mutableStateOf("checking") } // checking | latest | available | failed
    var info by remember { mutableStateOf<JixUpdateInfo?>(null) }
    val scope = rememberCoroutineScope()

    fun check() {
        state = "checking"
        scope.launch(Dispatchers.IO) {
            val r = fetchLatestRelease()
            info = r
            state = when {
                r == null -> "failed"
                versionNewer(r.latestVersion ?: "", r.currentVersion) -> "available"
                else -> "latest"
            }
        }
    }

    LaunchedEffect(Unit) { check() }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        PreferenceEntry(
            title = { Text(stringResource(R.string.check_for_updates)) },
            description = when (state) {
                "checking" -> stringResource(R.string.checking_for_updates)
                "latest" -> stringResource(R.string.up_to_date)
                "available" -> stringResource(R.string.update_available) +
                        "  (v${BuildConfig.VERSION_NAME} → v${info?.latestVersion?.trimStart('v')})"
                else -> stringResource(R.string.update_check_failed)
            },
            icon = {
                if (state == "checking") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Rounded.CloudDownload, null)
                }
            },
            onClick = { if (state != "checking") check() }
        )
        if (state == "available") {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconLabelButton(
                    text = stringResource(R.string.download_update),
                    icon = Icons.Rounded.Download,
                    onClick = { info?.apkUrl?.let { uriHandler.openUri(it) } ?: uriHandler.openUri("https://github.com/$JIXONE_REPO/releases") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val uriHandler = LocalUriHandler.current

    val showDebugInfo = BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "userdebug"

    ColumnWithContentPadding(
        modifier = Modifier.fillMaxHeight(),
        columnModifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
                .clickable { }
        )

        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "JixOne",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) | ${BuildConfig.FLAVOR}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.width(4.dp))

            if (showDebugInfo) {
                Spacer(Modifier.width(4.dp))

                Text(
                    text = BuildConfig.BUILD_TYPE.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        UpdateCheckCard(uriHandler)

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            IconLabelButton(
                text = "GitHub",
                painter = painterResource(R.drawable.github),
                onClick = { uriHandler.openUri("https://github.com/$JIXONE_REPO") },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconLabelButton(
                text = stringResource(R.string.wiki),
                icon = Icons.Outlined.Info,
                onClick = { uriHandler.openUri("https://github.com/OuterTune/OuterTune/wiki") },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.height(96.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.attribution_title)) },
                    onClick = {
                        navController.navigate("settings/about/attribution")
                    }
                )
                PreferenceEntry(
                    title = { Text(stringResource(R.string.oss_licenses_title)) },
                    onClick = {
                        navController.navigate("settings/about/oss_licenses")
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.help_bug_report_action)) },
                    onClick = {
                        uriHandler.openUri("https://github.com/OuterTune/OuterTune/issues")
                    }
                )
                PreferenceEntry(
                    title = { Text(stringResource(R.string.help_support_forum)) },
                    onClick = {
                        uriHandler.openUri("https://github.com/OuterTune/OuterTune/discussions")
                    }
                )
                PreferenceEntry(
                    title = { Text(stringResource(R.string.help_contact_email_inquiries)) },
                    onClick = {
                        val clipData = ClipData.newPlainText(
                            context.getString(R.string.app_name),
                            AnnotatedString("outertune@protonmail.com")
                        )
                        clipboardManager.nativeClipboard.setPrimaryClip(clipData)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsClickToReveal(stringResource(R.string.app_info_title)) {
                    val info = mutableListOf<String>(
                        "FFMetadataEx: $ENABLE_FFMETADATAEX",
                        "LM scanner concurrency: $MAX_LM_SCANNER_JOBS",
                        "LYRIC_FETCH_TIMEOUT: $LYRIC_FETCH_TIMEOUT",
                        "OOBE_VERSION: $OOBE_VERSION",
                        "LYRIC_FETCH_TIMEOUT: $LYRIC_FETCH_TIMEOUT",
                        "SNACKBAR_VERY_SHORT: $SNACKBAR_VERY_SHORT"
                    )
                    if (ENABLE_FFMETADATAEX) {
                        info.add("FFMetadataEx version: ${FFmpegScanner.VERSION_STRING}")
                        info.add("FFmpeg version: ${FfmpegLibrary.getVersion()}")
                        info.add("FFmpeg isAvailable: ${FfmpegLibrary.isAvailable()}")
                    }

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        info.forEach {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                SettingsClickToReveal(stringResource(R.string.device_info_title)) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val info = mutableListOf<String>(
                            "Device: ${Build.BRAND} ${Build.DEVICE} (${Build.MODEL})",
                            "Manufacturer: ${Build.MANUFACTURER}",
                            "HW: ${Build.BOARD} (${Build.HARDWARE})",
                            "ABIs: ${Build.SUPPORTED_ABIS.joinToString()})",
                            "Android: ${Build.VERSION.SDK_INT} (${Build.ID})",
                            Build.DISPLAY,
                            Build.PRODUCT,
                            Build.FINGERPRINT,
                            Build.VERSION.SECURITY_PATCH
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            info.add("SOC: ${Build.SOC_MODEL} (${Build.SOC_MANUFACTURER})")
                            info.add("SKU: ${Build.SKU} (${Build.ODM_SKU})")
                        }

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            info.forEach {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (ENABLE_FFMETADATAEX) {
                ContributorCard(
                    contributor = ContributorInfo(
                        name = "FFmpeg",
                        description = stringResource(R.string.ffmpeg_lgpl),
                        type = listOf(CUSTOM),
                        url = "https://github.com/OuterTune/ffMetadataEx/blob/main/Modules.md"
                    )
                )
            }
        }

    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )
}
