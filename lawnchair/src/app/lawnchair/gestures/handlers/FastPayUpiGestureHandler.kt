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
import android.provider.MediaStore
import android.widget.Toast
import app.lawnchair.LawnchairLauncher

class FastPayUpiGestureHandler(context: Context) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: LawnchairLauncher) {
        val upiPackages = listOf(
            "com.google.android.apps.nbu.paisa.user", // Google Pay
            "com.phonepe.app",                       // PhonePe
            "net.one97.paytm",                       // Paytm
            "in.org.npci.upiapp",                    // BHIM
            "com.cred.club",                         // CRED
        )

        for (pkg in upiPackages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    // Try next package
                }
            }
        }

        // Fallback to Camera QR scanner
        try {
            val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(cameraIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No UPI payment or camera app found", Toast.LENGTH_SHORT).show()
        }
    }
}
