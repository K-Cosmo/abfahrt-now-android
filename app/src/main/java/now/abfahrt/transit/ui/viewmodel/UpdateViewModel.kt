package now.abfahrt.transit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import now.abfahrt.transit.BuildConfig
import now.abfahrt.transit.data.api.GitHubReleaseApi
import now.abfahrt.transit.util.AppVersionInfo
import now.abfahrt.transit.util.AvailableUpdate
import now.abfahrt.transit.util.StartupTrace
import now.abfahrt.transit.util.UpdateReleasePolicy
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val githubReleaseApi: GitHubReleaseApi
) : ViewModel() {

    private val _availableUpdate = MutableStateFlow<AvailableUpdate?>(null)
    val availableUpdate: StateFlow<AvailableUpdate?> = _availableUpdate.asStateFlow()

    init {
        StartupTrace.mark("update_vm_created")
        checkLatestRelease()
    }

    private fun checkLatestRelease() {
        viewModelScope.launch {
            val started = StartupTrace.nowUptimeMs()
            StartupTrace.mark("update_check_start")
            try {
                val release = githubReleaseApi.latestRelease()
                _availableUpdate.value = UpdateReleasePolicy.newerThanCurrent(
                    tagName = release.tagName,
                    currentVersionCode = AppVersionInfo.versionCode
                )
                StartupTrace.duration(
                    event = "update_check_complete",
                    startedUptimeMs = started,
                    details = "result=success"
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                StartupTrace.duration(
                    event = "update_check_complete",
                    startedUptimeMs = started,
                    details = "result=unavailable type=${error::class.java.simpleName}"
                )
                // Update availability is non-critical. Network/GitHub failures must never
                // block startup or replace normal app error handling.
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Release check unavailable (${error::class.java.simpleName})")
                }
            }
        }
    }

    fun dismissUpdate() {
        _availableUpdate.value = null
    }

    private companion object {
        const val TAG = "AbfahrtUpdate"
    }
}
