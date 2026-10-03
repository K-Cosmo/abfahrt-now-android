package now.abfahrt.transit.data.api

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import now.abfahrt.transit.data.preferences.UserPreferencesRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds the x-api-key header to every request using the key decrypted from the
 * Keystore-protected DataStore value. Reads the key synchronously (runBlocking is acceptable inside an Interceptor
 * because the OkHttp dispatcher already runs on a background thread).
 */
@Singleton
class ApiKeyInterceptor @Inject constructor(
    private val prefs: UserPreferencesRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = runBlocking { prefs.preferencesFlow.firstOrNull()?.apiKey?.trim().orEmpty() }
        val builder = chain.request().newBuilder()
        if (apiKey.isNotBlank()) {
            builder.addHeader("x-api-key", apiKey)
        }
        return chain.proceed(builder.build())
    }
}
