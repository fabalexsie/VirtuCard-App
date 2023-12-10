package de.siebes.fabian.virtucard.ui
// ~ https://github.com/android/codelab-android-datastore/blob/preferences_datastore/app/src/main/java/com/codelab/android/datastore/ui/TasksViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import de.siebes.fabian.virtucard.data.UserPreferencesRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class UserPrefsUiState(
    val shareUrl: String,
)

class ShareUrlViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val initialSetupEvent = liveData {
        emit(userPreferencesRepository.fetchInitialPreferences())
    }

    // Keep the user preferences as a stream of changes
    private val userPrefsUiStateFlow = userPreferencesRepository.userPreferencesFlow.map {
        UserPrefsUiState(
            shareUrl = it.shareUrl,
        )
    }

    val userPrefsUiStateLiveData = userPrefsUiStateFlow.asLiveData()

    fun updateShareUrl(shareUrl: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateShareUrl(shareUrl = shareUrl)
        }
    }
}

class ShareUrlViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareUrlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShareUrlViewModel(userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}