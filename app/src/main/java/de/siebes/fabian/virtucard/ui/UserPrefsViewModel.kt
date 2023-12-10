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
    val userId: String,
    val userPw: String,
)

class UserPrefsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val initialSetupEvent = liveData {
        emit(userPreferencesRepository.fetchInitialPreferences())
    }

    // Keep the user preferences as a stream of changes
    private val userPrefsUiStateFlow = userPreferencesRepository.userPreferencesFlow.map {
        UserPrefsUiState(
            userId = it.userId,
            userPw = it.userPw,
        )
    }

    val userPrefsUiStateLiveData = userPrefsUiStateFlow.asLiveData()

    fun updateUserId(userId: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserId(userId = userId)
        }
    }
    fun updateUserPw(userPw: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserPw(userPw = userPw)
        }
    }
}

class UserPrefsViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserPrefsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserPrefsViewModel(userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}