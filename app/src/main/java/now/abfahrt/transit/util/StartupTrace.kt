package now.abfahrt.transit.util

import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lightweight startup diagnostics for Build 153.
 *
 * The log intentionally contains no coordinates, API keys, search strings or saved places.
 * `uptimeMillis` shares the same monotonic time base as `Process.getStartUptimeMillis()`.
 */
object StartupTrace {
    const val TAG = "AbfahrtStartup"

    data class ActivitySession(
        val id: Int,
        val startKind: String
    )

    private val processStartUptimeMs = Process.getStartUptimeMillis()
    private val activityCounter = AtomicInteger(0)

    @Volatile
    private var currentSessionId: Int = 0

    @Volatile
    private var currentStartKind: String = "process"

    fun nowUptimeMs(): Long = SystemClock.uptimeMillis()

    fun beginActivitySession(): ActivitySession {
        val id = activityCounter.incrementAndGet()
        val kind = if (id == 1) "cold" else "warm"
        currentSessionId = id
        currentStartKind = kind
        mark(
            event = "activity_onCreate_enter",
            sessionId = id,
            startKind = kind
        )
        return ActivitySession(id = id, startKind = kind)
    }

    fun mark(
        event: String,
        details: String? = null,
        sessionId: Int = currentSessionId,
        startKind: String = currentStartKind
    ) {
        logAt(
            event = event,
            nowUptimeMs = nowUptimeMs(),
            durationMs = null,
            details = details,
            sessionId = sessionId,
            startKind = startKind
        )
    }

    fun duration(
        event: String,
        startedUptimeMs: Long,
        details: String? = null,
        sessionId: Int = currentSessionId,
        startKind: String = currentStartKind
    ) {
        val now = nowUptimeMs()
        logAt(
            event = event,
            nowUptimeMs = now,
            durationMs = (now - startedUptimeMs).coerceAtLeast(0L),
            details = details,
            sessionId = sessionId,
            startKind = startKind
        )
    }

    /** Android system-trace section for short synchronous blocks only. */
    fun <T> section(name: String, block: () -> T): T {
        Trace.beginSection(name.take(127))
        return try {
            block()
        } finally {
            Trace.endSection()
        }
    }

    private fun logAt(
        event: String,
        nowUptimeMs: Long,
        durationMs: Long?,
        details: String?,
        sessionId: Int,
        startKind: String
    ) {
        val sinceProcessMs = (nowUptimeMs - processStartUptimeMs).coerceAtLeast(0L)
        val thread = if (Looper.myLooper() == Looper.getMainLooper()) "main" else "background"
        val durationPart = durationMs?.let { " durationMs=$it" }.orEmpty()
        val detailsPart = details?.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
        Log.i(
            TAG,
            "event=$event sinceProcessMs=$sinceProcessMs$durationPart session=$sessionId startKind=$startKind thread=$thread$detailsPart"
        )
    }
}
