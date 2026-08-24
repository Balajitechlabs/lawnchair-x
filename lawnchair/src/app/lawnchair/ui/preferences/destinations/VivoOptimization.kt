/*
 * Copyright 2026, BalajiTechLabs & Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.ui.preferences.destinations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.controls.WarningPreference
import app.lawnchair.ui.preferences.components.isNotificationServiceEnabled
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceGroupHeading
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.util.isDefaultLauncher
import com.google.android.msdl.data.model.MSDLToken

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VivoOptimization(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService<ClipboardManager>()

    var refreshKey by remember { mutableIntStateOf(0) }

    // Status checks
    val isBubblesEnabled = remember(refreshKey) {
        try {
            Settings.Secure.getInt(context.contentResolver, "notification_bubbles", 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    val isHomeDefault = remember(refreshKey) {
        context.isDefaultLauncher()
    }

    val powerManager = context.getSystemService<PowerManager>()
    val isBatteryOptimized = remember(refreshKey) {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    val isNotificationListenerEnabled = remember(refreshKey) {
        isNotificationServiceEnabled(context)
    }

    val isOverlayEnabled = remember(refreshKey) {
        Settings.canDrawOverlays(context)
    }

    PreferenceLayout(
        label = "Vivo & OriginOS Hub",
        modifier = modifier,
        backArrowVisible = !LocalIsExpandedScreen.current,
    ) {
        WarningPreference(
            text = "Personal Custom Build for Vivo V60e (OriginOS / Funtouch OS) by BalajiTechLabs. Disclaimer: Exclusively for personal use — not an official release. Configure these settings to unlock Google Bubbles, bypass OriginOS battery kills, and enjoy 120Hz fluid Pixel smoothness.",
            modifier = Modifier.padding(horizontal = 16.dp),
            standalone = true,
            colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Section 1: Google Notification Bubbles
        PreferenceGroupHeading(heading = "Google Notification Bubbles")
        PreferenceGroup {
            ClickablePreference(
                label = if (isBubblesEnabled) "Notification Bubbles: Enabled ✓" else "Enable Google Notification Bubbles",
                subtitle = if (isBubblesEnabled) {
                    "AOSP Notification Bubbles are active on your Vivo device!"
                } else {
                    "OriginOS hides this setting. Tap to copy the 1-line ADB command or activate via Shizuku."
                },
                hapticToken = MSDLToken.TAP_MEDIUM_EMPHASIS,
                onClick = {
                    val adbCmd = "adb shell settings put secure notification_bubbles 1"
                    val clip = ClipData.newPlainText("ADB Command", adbCmd)
                    clipboardManager?.setPrimaryClip(clip)
                    Toast.makeText(context, "Command copied to clipboard!\n$adbCmd", Toast.LENGTH_LONG).show()
                    refreshKey++
                },
            )

            ClickablePreference(
                label = "Copy Shizuku / Shell Script",
                subtitle = "settings put secure notification_bubbles 1",
                hapticToken = MSDLToken.TAP_LOW_EMPHASIS,
                onClick = {
                    val cmd = "settings put secure notification_bubbles 1"
                    val clip = ClipData.newPlainText("Shell Command", cmd)
                    clipboardManager?.setPrimaryClip(clip)
                    Toast.makeText(context, "Shell command copied: $cmd", Toast.LENGTH_SHORT).show()
                },
            )
        }

        // Section 2: Core System Permissions
        PreferenceGroupHeading(heading = "Essential System Roles")
        PreferenceGroup {
            ClickablePreference(
                label = if (isHomeDefault) "Default Launcher: Active ✓" else "Set as Default Home App",
                subtitle = if (isHomeDefault) "Lawnchair is handling your home screen" else "Tap to choose Lawnchair as default launcher in OriginOS",
                hapticToken = MSDLToken.TAP_MEDIUM_EMPHASIS,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    refreshKey++
                },
            )

            ClickablePreference(
                label = if (isBatteryOptimized) "Background Battery: Unrestricted ✓" else "Allow Unrestricted Background Power",
                subtitle = if (isBatteryOptimized) "Protected from OriginOS aggressive background kill" else "Prevents OriginOS from restarting or sleeping the launcher",
                hapticToken = MSDLToken.TAP_MEDIUM_EMPHASIS,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    refreshKey++
                },
            )

            ClickablePreference(
                label = if (isNotificationListenerEnabled) "Notification Badges: Active ✓" else "Notification Listener Permission",
                subtitle = "Required for unread badges and Google notification pill syncing",
                hapticToken = MSDLToken.TAP_LOW_EMPHASIS,
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    refreshKey++
                },
            )

            ClickablePreference(
                label = if (isOverlayEnabled) "Floating Windows & QuickDash: Granted ✓" else "Display Over Other Apps",
                subtitle = "Allows floating bubbles, overlays, and QuickDash companion hub",
                hapticToken = MSDLToken.TAP_LOW_EMPHASIS,
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    refreshKey++
                },
            )

            ClickablePreference(
                label = "Accessibility Service",
                subtitle = "Enables instant double-tap screen lock without PIN prompt requirement",
                hapticToken = MSDLToken.TAP_LOW_EMPHASIS,
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    refreshKey++
                },
            )
        }

        // Section 3: Vivo Display & Motion Calibration
        PreferenceGroupHeading(heading = "Vivo 120Hz & Display Calibration")
        PreferenceGroup {
            ClickablePreference(
                label = "Display Refresh Rate (120Hz)",
                subtitle = "Ensure Vivo Display settings are set to '120Hz' or 'High' for maximum fluid spring physics",
                hapticToken = MSDLToken.TAP_LOW_EMPHASIS,
                onClick = {
                    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                },
            )
        }
    }
}
