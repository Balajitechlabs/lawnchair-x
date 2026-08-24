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

package app.lawnchair.gestures.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.lawnchair.LawnchairLauncher

class QuickDashGestureHandler(context: Context) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: LawnchairLauncher) {
        val quickDashPackage = "com.balajitechlabs.quickdash"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(quickDashPackage)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(
                context,
                "QuickDash not installed. Opening QuickDash Hub...",
                Toast.LENGTH_SHORT,
            ).show()
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://quickdash.balajitechlab.com"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(webIntent)
            } catch (e: Exception) {
                // Ignore fallback error
            }
        }
    }
}
