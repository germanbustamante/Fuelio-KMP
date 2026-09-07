package com.germandebustamante.fuelio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.germandebustamante.fuelio.core.navigation.deeplink.ExternalUriHandler
import com.germandebustamante.fuelio.feature.common.permission.location.AndroidLocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.PermissionResultBridge

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> PermissionResultBridge.deliverResult(isGranted) }

    private val locationPermissionController: LocationPermissionController by lazy {
        AndroidLocationPermissionController(this, locationPermissionLauncher)
    }

    /**
     * The launch `Intent` is "sticky": Android redelivers it to every new Activity instance,
     * including ones created by a configuration change or a recreation after process death.
     * Reading `intent.data` unconditionally in `onCreate` would reapply the link — rotating the
     * device after opening `fuelio://station/x/detail` rebuilt the stack and sent the user back to
     * the detail screen from wherever they were. This flag remembers the URI was already delivered
     * to [ExternalUriHandler] and travels in the state `Bundle`, so it survives both rotation and
     * process death.
     */
    private var deepLinkConsumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        deepLinkConsumed = savedInstanceState?.getBoolean(KEY_DEEP_LINK_CONSUMED) == true
        consumeDeepLinkIfNeeded(intent)

        setContent {
            App(locationPermissionController)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // A new intent is by definition unconsumed: with android:launchMode="singleTop" every
        // subsequent link arrives here instead of spawning a second Activity.
        deepLinkConsumed = false
        consumeDeepLinkIfNeeded(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_DEEP_LINK_CONSUMED, deepLinkConsumed)
    }

    private fun consumeDeepLinkIfNeeded(intent: Intent?) {
        if (deepLinkConsumed) return
        val uri = intent?.data?.toString() ?: return
        deepLinkConsumed = true
        ExternalUriHandler.onNewUri(uri)
    }

    private companion object {
        const val KEY_DEEP_LINK_CONSUMED = "deep_link_consumed"
    }
}