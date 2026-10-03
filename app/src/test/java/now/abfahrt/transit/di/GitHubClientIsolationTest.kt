package now.abfahrt.transit.di

import now.abfahrt.transit.data.api.ApiKeyInterceptor
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubClientIsolationTest {

    @Test
    fun `GitHub client never installs abfahrt API key interceptor`() {
        val client = NetworkModule.provideGitHubOkHttpClient()

        assertTrue(client.interceptors.none { it is ApiKeyInterceptor })
    }
}
