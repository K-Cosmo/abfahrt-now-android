package now.abfahrt.transit.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import now.abfahrt.transit.data.model.AppLanguage
import now.abfahrt.transit.data.model.AppPreferences
import now.abfahrt.transit.data.model.OrsTravelMode
import now.abfahrt.transit.data.model.SavedPlace
import now.abfahrt.transit.data.model.SavedPlaceType
import now.abfahrt.transit.data.model.TransportMode
import now.abfahrt.transit.data.security.ApiKeyCipher
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "abfahrt_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiKeyCipher: ApiKeyCipher
) {
    companion object {
        private const val TAG = "AbfahrtSecret"
    }

    private object Keys {
        val API_KEY          = stringPreferencesKey("api_key")
        val ORS_API_KEY      = stringPreferencesKey("ors_api_key")
        val ONBOARDING_DONE  = booleanPreferencesKey("onboarding_done")
        val RADIUS           = intPreferencesKey("radius")
        val WINDOW_START     = intPreferencesKey("window_start")
        val WINDOW_END       = intPreferencesKey("window_end")
        val REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        val MODES            = stringSetPreferencesKey("selected_modes")
        val QUICK_SLOTS      = stringPreferencesKey("quick_slots")
        val LANGUAGE         = stringPreferencesKey("language")
        val MAX_PER_DIR      = intPreferencesKey("max_per_direction")
        val ORS_TRAVEL_MODE  = stringPreferencesKey("ors_travel_mode")
        val HIDE_UNREACHABLE = booleanPreferencesKey("hide_unreachable_departures")
        val HOME_TITLE       = stringPreferencesKey("saved_home_title")
        val HOME_SUBTITLE    = stringPreferencesKey("saved_home_subtitle")
        val HOME_LAT         = doublePreferencesKey("saved_home_lat")
        val HOME_LON         = doublePreferencesKey("saved_home_lon")
        val WORK_TITLE       = stringPreferencesKey("saved_work_title")
        val WORK_SUBTITLE    = stringPreferencesKey("saved_work_subtitle")
        val WORK_LAT         = doublePreferencesKey("saved_work_lat")
        val WORK_LON         = doublePreferencesKey("saved_work_lon")
    }

    private val migrationMutex = Mutex()
    @Volatile private var migrationComplete = false

    private val secretCacheLock = Any()
    private var cachedApiStored: String? = null
    private var cachedApiPlain: String = ""
    private var cachedOrsStored: String? = null
    private var cachedOrsPlain: String = ""

    val preferencesFlow: Flow<AppPreferences> = flow {
        ensureApiKeysEncrypted()
        emitAll(
            context.dataStore.data
                .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
                .map { prefs ->
                    val modeValues = prefs[Keys.MODES] ?: TransportMode.entries.map { it.apiValue }.toSet()
                    val start = prefs[Keys.WINDOW_START] ?: 0
                    val end   = prefs[Keys.WINDOW_END]   ?: 30
                    AppPreferences(
                        apiKey                 = decodeStoredSecret(prefs[Keys.API_KEY].orEmpty(), SecretKind.ABFAHRT),
                        orsApiKey              = decodeStoredSecret(prefs[Keys.ORS_API_KEY].orEmpty(), SecretKind.ORS),
                        onboardingCompleted    = prefs[Keys.ONBOARDING_DONE]  ?: false,
                        radius                 = prefs[Keys.RADIUS]           ?: 800,
                        windowStartMinutes     = start.coerceIn(0, 115),
                        windowEndMinutes       = end.coerceIn(start + 5, 120),
                        refreshIntervalMinutes = prefs[Keys.REFRESH_INTERVAL] ?: 1,
                        selectedModes          = modeValues
                            .mapNotNull { TransportMode.fromApiValue(it) }
                            .toSet()
                            .ifEmpty { TransportMode.entries.toSet() },
                        maxPerDirection        = prefs[Keys.MAX_PER_DIR] ?: 1,
                        language               = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: "system"),
                        orsTravelMode          = OrsTravelMode.fromCode(prefs[Keys.ORS_TRAVEL_MODE]),
                        hideUnreachableDepartures = prefs[Keys.HIDE_UNREACHABLE] ?: false,
                        homePlace               = readSavedPlace(prefs, SavedPlaceType.HOME),
                        workPlace               = readSavedPlace(prefs, SavedPlaceType.WORK),
                        quickFilterSlots       = (prefs[Keys.QUICK_SLOTS]
                            ?.split(",")
                            ?.mapNotNull { TransportMode.fromApiValue(it.trim()) }
                            ?.take(4)
                            ?.takeIf { it.size == 4 })
                            ?: listOf(
                                TransportMode.SUBWAY, TransportMode.SUBURBAN,
                                TransportMode.TRAM,   TransportMode.BUS
                            )
                    )
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun updateApiKey(key: String): Boolean {
        val normalized = key.trim()
        if (normalized.isEmpty()) {
            Log.w(TAG, "🔐 refusing to remove required abfahrt.now API key")
            return false
        }
        return updateSecret(Keys.API_KEY, normalized, SecretKind.ABFAHRT)
    }

    suspend fun updateOrsApiKey(key: String): Boolean =
        updateSecret(Keys.ORS_API_KEY, key, SecretKind.ORS)

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }

    suspend fun updateRadius(r: Int) {
        context.dataStore.edit { it[Keys.RADIUS] = r.coerceIn(100, 2000) }
    }

    suspend fun updateRefreshInterval(m: Int) {
        context.dataStore.edit { it[Keys.REFRESH_INTERVAL] = m.coerceIn(0, 5) }
    }

    suspend fun updateWindowRange(startMin: Int, endMin: Int) {
        val s = startMin.coerceIn(0, 115)
        val e = endMin.coerceIn(s + 5, 120)
        context.dataStore.edit {
            it[Keys.WINDOW_START] = s
            it[Keys.WINDOW_END]   = e
        }
    }

    suspend fun updateLanguage(lang: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang.code }
    }

    suspend fun updateMaxPerDirection(n: Int) {
        context.dataStore.edit { it[Keys.MAX_PER_DIR] = n.coerceIn(1, 3) }
    }

    suspend fun updateQuickSlots(slots: List<TransportMode>) {
        require(slots.size == 4) { "quickFilterSlots must have exactly 4 entries" }
        context.dataStore.edit { it[Keys.QUICK_SLOTS] = slots.joinToString(",") { m -> m.apiValue } }
    }

    suspend fun updateSelectedModes(modes: Set<TransportMode>) {
        val safe = modes.ifEmpty { TransportMode.entries.toSet() }
        context.dataStore.edit { it[Keys.MODES] = safe.map { m -> m.apiValue }.toSet() }
    }

    suspend fun updateOrsTravelMode(mode: OrsTravelMode) {
        context.dataStore.edit { it[Keys.ORS_TRAVEL_MODE] = mode.name }
    }

    suspend fun updateHideUnreachableDepartures(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIDE_UNREACHABLE] = enabled }
    }

    suspend fun updateSavedPlace(type: SavedPlaceType, place: SavedPlace?) {
        context.dataStore.edit { prefs ->
            val keys = savedPlaceKeys(type)
            if (place == null) {
                prefs.remove(keys.title)
                prefs.remove(keys.subtitle)
                prefs.remove(keys.lat)
                prefs.remove(keys.lon)
            } else {
                prefs[keys.title] = place.title.trim()
                prefs[keys.subtitle] = place.subtitle.trim()
                prefs[keys.lat] = place.lat
                prefs[keys.lon] = place.lon
            }
        }
    }

    private fun readSavedPlace(prefs: Preferences, type: SavedPlaceType): SavedPlace? {
        val keys = savedPlaceKeys(type)
        val title = prefs[keys.title]?.trim().orEmpty()
        val lat = prefs[keys.lat]
        val lon = prefs[keys.lon]
        if (title.isBlank() || lat == null || lon == null) return null
        return SavedPlace(
            title = title,
            subtitle = prefs[keys.subtitle]?.trim().orEmpty(),
            lat = lat,
            lon = lon
        )
    }

    private fun savedPlaceKeys(type: SavedPlaceType): SavedPlaceKeys = when (type) {
        SavedPlaceType.HOME -> SavedPlaceKeys(Keys.HOME_TITLE, Keys.HOME_SUBTITLE, Keys.HOME_LAT, Keys.HOME_LON)
        SavedPlaceType.WORK -> SavedPlaceKeys(Keys.WORK_TITLE, Keys.WORK_SUBTITLE, Keys.WORK_LAT, Keys.WORK_LON)
    }

    private data class SavedPlaceKeys(
        val title: Preferences.Key<String>,
        val subtitle: Preferences.Key<String>,
        val lat: Preferences.Key<Double>,
        val lon: Preferences.Key<Double>
    )

    /**
     * One-time in-place migration of pre-Build-125 plaintext keys. The old value is
     * only replaced after encryption succeeded. A failed migration therefore never
     * destroys a working key and is retried by a later collector.
     */
    private suspend fun ensureApiKeysEncrypted() {
        if (migrationComplete) return

        migrationMutex.withLock {
            if (migrationComplete) return

            val snapshot = try {
                context.dataStore.data.first()
            } catch (e: IOException) {
                Log.w(TAG, "🔐 API-key migration deferred: DataStore read failed")
                return
            }

            val legacyApi = snapshot[Keys.API_KEY]
                ?.takeIf { it.isNotBlank() && !apiKeyCipher.isEncrypted(it) }
            val legacyOrs = snapshot[Keys.ORS_API_KEY]
                ?.takeIf { it.isNotBlank() && !apiKeyCipher.isEncrypted(it) }

            if (legacyApi == null && legacyOrs == null) {
                migrationComplete = true
                return
            }

            val encryptedApi = legacyApi?.let { encryptSafely(it, "abfahrt.now migration") }
            val encryptedOrs = legacyOrs?.let { encryptSafely(it, "ORS migration") }
            val migrationSucceeded = (legacyApi == null || encryptedApi != null) &&
                (legacyOrs == null || encryptedOrs != null)

            if (!migrationSucceeded) {
                Log.w(TAG, "🔐 API-key migration deferred; legacy value kept intact")
                return
            }

            context.dataStore.edit { current ->
                if (legacyApi != null && encryptedApi != null && current[Keys.API_KEY] == legacyApi) {
                    current[Keys.API_KEY] = encryptedApi
                }
                if (legacyOrs != null && encryptedOrs != null && current[Keys.ORS_API_KEY] == legacyOrs) {
                    current[Keys.ORS_API_KEY] = encryptedOrs
                }
            }

            clearSecretCache()
            migrationComplete = true
            Log.i(
                TAG,
                "🔐 migrated legacy API-key storage abfahrt=${legacyApi != null} ors=${legacyOrs != null}"
            )
        }
    }

    private suspend fun updateSecret(
        preferenceKey: Preferences.Key<String>,
        key: String,
        kind: SecretKind
    ): Boolean {
        val normalized = key.trim()
        if (normalized.isEmpty()) {
            context.dataStore.edit { it.remove(preferenceKey) }
            updateSecretCache(kind, stored = "", plain = "")
            return true
        }

        val encrypted = encryptSafely(normalized, "${kind.logName} save") ?: return false

        context.dataStore.edit { it[preferenceKey] = encrypted }
        updateSecretCache(kind, stored = encrypted, plain = normalized)
        Log.i(TAG, "🔐 ${kind.logName} API key stored encrypted")
        return true
    }

    private suspend fun encryptSafely(value: String, purpose: String): String? =
        withContext(Dispatchers.IO) {
            runCatching { apiKeyCipher.encrypt(value) }
                .onFailure { Log.e(TAG, "🔐 $purpose failed: ${it.javaClass.simpleName}") }
                .getOrNull()
        }

    private fun decodeStoredSecret(stored: String, kind: SecretKind): String {
        if (stored.isBlank()) {
            updateSecretCache(kind, stored = "", plain = "")
            return ""
        }

        cachedSecret(kind, stored)?.let { return it }

        if (!apiKeyCipher.isEncrypted(stored)) {
            // Legacy fallback if first-run migration was temporarily unavailable.
            updateSecretCache(kind, stored, stored)
            return stored
        }

        val decrypted = runCatching { apiKeyCipher.decrypt(stored) }
            .onFailure {
                Log.e(TAG, "🔐 ${kind.logName} API key decrypt failed: ${it.javaClass.simpleName}")
            }
            .getOrNull()
            ?: return ""

        // Cache only successful decryptions; a transient failure must remain retryable.
        updateSecretCache(kind, stored, decrypted)
        return decrypted
    }

    private fun cachedSecret(kind: SecretKind, stored: String): String? = synchronized(secretCacheLock) {
        when (kind) {
            SecretKind.ABFAHRT -> cachedApiPlain.takeIf { cachedApiStored == stored }
            SecretKind.ORS -> cachedOrsPlain.takeIf { cachedOrsStored == stored }
        }
    }

    private fun updateSecretCache(kind: SecretKind, stored: String, plain: String) {
        synchronized(secretCacheLock) {
            when (kind) {
                SecretKind.ABFAHRT -> {
                    cachedApiStored = stored
                    cachedApiPlain = plain
                }
                SecretKind.ORS -> {
                    cachedOrsStored = stored
                    cachedOrsPlain = plain
                }
            }
        }
    }

    private fun clearSecretCache() {
        synchronized(secretCacheLock) {
            cachedApiStored = null
            cachedApiPlain = ""
            cachedOrsStored = null
            cachedOrsPlain = ""
        }
    }

    private enum class SecretKind(val logName: String) {
        ABFAHRT("abfahrt.now"),
        ORS("ORS")
    }
}
