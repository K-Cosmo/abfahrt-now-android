package now.abfahrt.transit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import now.abfahrt.transit.ui.screens.AppNavHost
import now.abfahrt.transit.ui.theme.AbfahrtTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen before super.onCreate so the system
        // replaces it with our activity smoothly.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // AppCompat restores persisted per-app locales automatically via
        // AppLocalesMetadataHolderService (see AndroidManifest.xml).
        // Do not override that here with a separate SharedPreferences source,
        // otherwise the locale can be reset back to an outdated value.
        setContent {
            AbfahrtTheme {
                AppNavHost()
            }
        }
    }
}
