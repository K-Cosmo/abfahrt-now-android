package now.abfahrt.transit

import android.app.Application
import android.system.Os
import android.system.OsConstants
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

/**
 * Hilt application class.
 * Declared in AndroidManifest.xml via android:name=".AbfahrtApplication".
 */
@HiltAndroidApp
class AbfahrtApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        logMemoryPageSize()
        MapLibre.getInstance(this)
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
