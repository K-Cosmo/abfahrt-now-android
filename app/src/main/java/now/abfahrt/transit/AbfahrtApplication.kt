package now.abfahrt.transit

import android.app.Application
import android.system.Os
import android.system.OsConstants
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import now.abfahrt.transit.util.StartupTrace
import org.maplibre.android.MapLibre

/**
 * Hilt application class.
 * Declared in AndroidManifest.xml via android:name=".AbfahrtApplication".
 */
@HiltAndroidApp
class AbfahrtApplication : Application() {
    override fun onCreate() {
        val applicationStarted = StartupTrace.nowUptimeMs()
        StartupTrace.mark("application_onCreate_enter")

        val superStarted = StartupTrace.nowUptimeMs()
        super.onCreate()
        StartupTrace.duration("application_super_onCreate", superStarted)

        logMemoryPageSize()

        val mapLibreStarted = StartupTrace.nowUptimeMs()
        StartupTrace.section("Abfahrt.MapLibre.init") {
            MapLibre.getInstance(this)
        }
        StartupTrace.duration("maplibre_init", mapLibreStarted)

        StartupTrace.duration("application_onCreate_total", applicationStarted)
        StartupTrace.mark("application_onCreate_exit")
    }

    private fun logMemoryPageSize() {
        runCatching { Os.sysconf(OsConstants._SC_PAGESIZE) }
            .onSuccess { pageSize ->
                Log.i("AbfahrtCompat", "memoryPageSizeBytes=$pageSize")
            }
            .onFailure { error ->
                Log.w("AbfahrtCompat", "memory page-size detection failed: ${error.javaClass.simpleName}")
            }
    }
}
