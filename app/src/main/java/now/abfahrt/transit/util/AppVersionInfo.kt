package now.abfahrt.transit.util

import now.abfahrt.transit.BuildConfig

object AppVersionInfo {
    val versionName: String = BuildConfig.VERSION_NAME
    val versionCode: Int = BuildConfig.VERSION_CODE

    val displayVersion: String
        get() = "$versionName (Build $versionCode)"

    val userAgent: String
        get() = "AbfahrtApp/$versionName ($versionCode) Android"
}
