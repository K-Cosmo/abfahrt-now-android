package now.abfahrt.transit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import now.abfahrt.transit.data.model.AppPreferences
import now.abfahrt.transit.data.preferences.UserPreferencesRepository
import now.abfahrt.transit.util.StartupTrace
import javax.inject.Inject

/**
 * Provides the app-access decision only after UserPreferencesRepository has emitted
 * a real DataStore-backed value. A nullable initial state deliberately avoids the
 * synthetic AppPreferences() value that previously made onboarding flash briefly
 * for already configured users during cold start.
 */
@HiltViewModel
class AccessGateViewModel @Inject constructor(
    prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private var firstPreferenceEmission = true

    init {
        StartupTrace.mark("access_gate_vm_created")
    }

    val preferences: StateFlow<AppPreferences?> = prefsRepo.preferencesFlow
        .onEach {
            if (firstPreferenceEmission) {
                firstPreferenceEmission = false
                StartupTrace.mark("preferences_first_real_emission")
            }
        }
        .map<AppPreferences, AppPreferences?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
}
