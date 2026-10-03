package now.abfahrt.transit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import now.abfahrt.transit.BuildConfig
import now.abfahrt.transit.data.api.AbfahrtApiService
import now.abfahrt.transit.util.AppVersionInfo
import now.abfahrt.transit.data.api.ApiKeyInterceptor
import now.abfahrt.transit.data.api.OpenRouteServiceApi
import now.abfahrt.transit.data.api.PhotonApiService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.abfahrt.now/"
    private const val PHOTON_BASE_URL = "https://photon.komoot.io/"
    private const val ORS_BASE_URL = "https://api.heigit.org/openrouteservice/"

    private fun networkLogLevel(): HttpLoggingInterceptor.Level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE

    @Provides
    @Singleton
    @Named("abfahrtClient")
    fun provideAbfahrtOkHttpClient(apiKeyInterceptor: ApiKeyInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = networkLogLevel()
                }
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("photonClient")
    fun providePhotonOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .header("User-Agent", AppVersionInfo.userAgent)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = networkLogLevel()
                }
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("orsClient")
    fun provideOrsOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                                        .header("User-Agent", AppVersionInfo.userAgent)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = networkLogLevel()
                }
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("abfahrtRetrofit")
    fun provideAbfahrtRetrofit(@Named("abfahrtClient") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("photonRetrofit")
    fun providePhotonRetrofit(@Named("photonClient") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(PHOTON_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("orsRetrofit")
    fun provideOrsRetrofit(@Named("orsClient") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ORS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(@Named("abfahrtRetrofit") retrofit: Retrofit): AbfahrtApiService =
        retrofit.create(AbfahrtApiService::class.java)

    @Provides
    @Singleton
    fun providePhotonApiService(@Named("photonRetrofit") retrofit: Retrofit): PhotonApiService =
        retrofit.create(PhotonApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenRouteServiceApi(@Named("orsRetrofit") retrofit: Retrofit): OpenRouteServiceApi =
        retrofit.create(OpenRouteServiceApi::class.java)
}
