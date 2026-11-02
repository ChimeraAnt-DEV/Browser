package com.browseglass

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.browseglass.engine.BrowserEngine
import com.browseglass.ui.BastionScreen
import com.browseglass.ui.theme.BastionTheme

/**
 * Single-activity browser. Launches in singleTask, edge-to-edge, all UI is
 * Compose on top of the GeckoView SurfaceView. Extends [FragmentActivity] so
 * [com.browseglass.security.BiometricGate] can present the BiometricPrompt.
 */
class MainActivity : FragmentActivity(), BastionScreen {

    private val engine: BrowserEngine get() = (application as BastionApp).engine

    /** Permission launchers used by the glass permission-flow (Phase 5.32). */
    private val cameraPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val micPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val locationPerm = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BastionTheme {
                BastionScreen(host = this)
            }
        }
    }

    override fun requestCamera() {
        cameraPerm.launch(android.Manifest.permission.CAMERA)
    }

    override fun requestMicrophone() {
        micPerm.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    override fun requestLocation() {
        locationPerm.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ))
    }
}