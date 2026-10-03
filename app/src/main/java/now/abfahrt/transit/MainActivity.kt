package now.abfahrt.transit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import now.abfahrt.transit.ui.screens.AppNavHost
import now.abfahrt.transit.ui.theme.AbfahrtTheme
import now.abfahrt.transit.ui.viewmodel.UpdateViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val updateViewModel: UpdateViewModel by viewModels()

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
                val availableUpdate by updateViewModel.availableUpdate.collectAsState()

                AppNavHost()

                availableUpdate?.let { update ->
                    AlertDialog(
                        onDismissRequest = updateViewModel::dismissUpdate,
                        title = { Text(text = stringResource(R.string.app_name)) },
                        text = {
                            Text(
                                text = stringResource(
                                    R.string.update_available_message,
                                    update.displayVersion
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    openUpdateRelease(update.releaseUrl)
                                    updateViewModel.dismissUpdate()
                                }
                            ) {
                                Text(text = stringResource(R.string.update_action))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = updateViewModel::dismissUpdate) {
                                Text(text = stringResource(R.string.close))
                            }
                        }
                    )
                }
            }
        }
    }

    private fun openUpdateRelease(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
