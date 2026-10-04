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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import now.abfahrt.transit.ui.screens.AppNavHost
import now.abfahrt.transit.ui.theme.AbfahrtTheme
import now.abfahrt.transit.ui.viewmodel.UpdateViewModel
import now.abfahrt.transit.util.StartupTrace

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val updateViewModel: UpdateViewModel by viewModels()
    private lateinit var startupSession: StartupTrace.ActivitySession

    override fun onCreate(savedInstanceState: Bundle?) {
        startupSession = StartupTrace.beginActivitySession()

        // Install the splash screen before super.onCreate so the system
        // replaces it with our activity smoothly.
        val splashStarted = StartupTrace.nowUptimeMs()
        StartupTrace.section("Abfahrt.Splash.install") {
            installSplashScreen()
        }
        StartupTrace.duration(
            event = "splash_install",
            startedUptimeMs = splashStarted,
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )

        val superStarted = StartupTrace.nowUptimeMs()
        super.onCreate(savedInstanceState)
        StartupTrace.duration(
            event = "activity_super_onCreate",
            startedUptimeMs = superStarted,
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )

        val edgeToEdgeStarted = StartupTrace.nowUptimeMs()
        enableEdgeToEdge()
        StartupTrace.duration(
            event = "edge_to_edge",
            startedUptimeMs = edgeToEdgeStarted,
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )

        // AppCompat restores persisted per-app locales automatically via
        // AppLocalesMetadataHolderService (see AndroidManifest.xml).
        // Do not override that here with a separate SharedPreferences source,
        // otherwise the locale can be reset back to an outdated value.
        val setContentStarted = StartupTrace.nowUptimeMs()
        setContent {
            AbfahrtTheme {
                LaunchedEffect(Unit) {
                    StartupTrace.mark("compose_root_committed")
                    withFrameNanos { }
                    StartupTrace.mark("compose_first_frame")
                }

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
        StartupTrace.duration(
            event = "set_content_return",
            startedUptimeMs = setContentStarted,
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )
        StartupTrace.mark(
            event = "activity_onCreate_exit",
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )
    }

    override fun onStart() {
        super.onStart()
        StartupTrace.mark(
            event = "activity_onStart",
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )
    }

    override fun onResume() {
        super.onResume()
        StartupTrace.mark(
            event = "activity_onResume",
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )
    }

    override fun onStop() {
        StartupTrace.mark(
            event = "activity_onStop",
            sessionId = startupSession.id,
            startKind = startupSession.startKind
        )
        super.onStop()
    }

    private fun openUpdateRelease(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
