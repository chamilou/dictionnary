package com.example.dictionnary.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionnary.data.repository.DictionaryRepository
import com.example.dictionnary.domain.model.AppLanguage
import com.example.dictionnary.domain.model.DictionaryEntryResult
import com.example.dictionnary.domain.model.RecentSearch
import com.example.dictionnary.presentation.ui.AppThemeManager
import com.example.dictionnary.presentation.ui.AppThemeMode
import com.example.dictionnary.presentation.ui.UiLanguageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TrainingWordSource {
    CHOSEN,
    RANDOM
}

enum class DirectionCoverageSupport {
    SUPPORTED,
    DRAFT_BRIDGE,
    COMING_SOON
}

data class DirectionWordCount(
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val count: Int,
    val support: DirectionCoverageSupport = DirectionCoverageSupport.SUPPORTED
)

data class DictionaryUiState(
    val query: String = "",
    val sourceLanguageCode: String = AppLanguage.AV.code,
    val targetLanguageCode: String = AppLanguage.RU.code,
    val showAllTranslations: Boolean = true,
    val showDraftTranslations: Boolean = true,
    val searchResults: List<DictionaryEntryResult> = emptyList(),
    val favorites: List<DictionaryEntryResult> = emptyList(),
    val recentSearches: List<RecentSearch> = emptyList(),
    val canLoadMoreSearchResults: Boolean = false,
    val nextSearchOffset: Int = 0,
    val selectedEntryId: Long? = null,
    val isLoadingMoreResults: Boolean = false,
    val isLoading: Boolean = false,
    val trainingWordSource: TrainingWordSource = TrainingWordSource.RANDOM,
    val trainingPrompt: String = "",
    val trainingSuggestions: List<DictionaryEntryResult> = emptyList(),
    val selectedTrainingEntry: DictionaryEntryResult? = null,
    val isTrainingAnswerVisible: Boolean = false,
    val isTrainingLoading: Boolean = false,
    val uiLanguageCode: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val totalEntryCount: Int = 0,
    val directionWordCounts: List<DirectionWordCount> = emptyList(),
    val isSettingsInfoLoading: Boolean = false
)

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DictionaryRepository(application)
    private var refreshJob: Job? = null
    private var trainingSearchJob: Job? = null

    var uiState by mutableStateOf(
        DictionaryUiState(
            uiLanguageCode = UiLanguageManager.getSavedUiLanguageCode(application),
            themeMode = AppThemeManager.getSavedThemeMode(application)
        )
    )
        private set

    init {
        refresh()
        loadSettingsInfo()
    }

    fun updateQuery(query: String) {
        uiState = uiState.copy(query = query)
        refresh()
    }

    fun updateSourceLanguage(languageCode: String) {
        uiState = uiState.copy(sourceLanguageCode = languageCode).resetTrainingState()
        refresh()
    }

    fun updateTargetLanguage(languageCode: String) {
        uiState = uiState.copy(targetLanguageCode = languageCode).resetTrainingState()
        refresh()
    }

    fun updateLanguageDirection(sourceLanguageCode: String, targetLanguageCode: String) {
        uiState = uiState.copy(
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode
        ).resetTrainingState()
        refresh()
    }

    fun swapLanguages() {
        if (uiState.targetLanguageCode == AppLanguage.ALL.code) {
            return
        }

        uiState = uiState.copy(
            sourceLanguageCode = uiState.targetLanguageCode,
            targetLanguageCode = uiState.sourceLanguageCode
        ).resetTrainingState()
        refresh()
    }

    fun setShowAllTranslations(enabled: Boolean) {
        uiState = uiState.copy(showAllTranslations = enabled).resetTrainingState()
        refresh()
    }

    fun setShowDraftTranslations(enabled: Boolean) {
        uiState = uiState.copy(showDraftTranslations = enabled).resetTrainingState()
        refresh()
    }

    fun updateUiLanguage(languageCode: String?) {
        if (uiState.uiLanguageCode == languageCode) {
            return
        }

        UiLanguageManager.saveUiLanguageCode(getApplication(), languageCode)
        uiState = uiState.copy(uiLanguageCode = languageCode)
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        if (uiState.themeMode == themeMode) {
            return
        }

        AppThemeManager.saveThemeMode(getApplication(), themeMode)
        uiState = uiState.copy(themeMode = themeMode)
    }

    fun loadSettingsInfo(forceReload: Boolean = false) {
        val snapshot = uiState
        if (snapshot.isSettingsInfoLoading) {
            return
        }
        if (!forceReload && snapshot.directionWordCounts.isNotEmpty()) {
            return
        }

        uiState = uiState.copy(isSettingsInfoLoading = true)

        viewModelScope.launch {
            val totalEntryCount = withContext(Dispatchers.IO) {
                repository.getEntryCount()
            }
            val directionWordCounts = withContext(Dispatchers.IO) {
                coverageDirections.map { direction ->
                    direction.copy(
                        count = repository.countDirectionWords(
                            sourceLanguageCode = direction.sourceLanguageCode,
                            targetLanguageCode = direction.targetLanguageCode,
                            includeDraftTranslations = true
                        )
                    )
                }
            }

            if (!isActive) {
                return@launch
            }

            uiState = uiState.copy(
                totalEntryCount = totalEntryCount,
                directionWordCounts = directionWordCounts,
                isSettingsInfoLoading = false
            )
        }
    }

    fun toggleFavorite(entryId: Long) {
        uiState = uiState.copy(
            selectedTrainingEntry = uiState.selectedTrainingEntry?.let { entry ->
                if (entry.entryId == entryId) {
                    entry.copy(isFavorite = !entry.isFavorite)
                } else {
                    entry
                }
            }
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.toggleFavorite(entryId)
            }
            refresh()
        }
    }

    fun updateTranslationCheckedStatus(
        entryId: Long,
        languageCode: String,
        checkedStatus: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateTranslationCheckedStatus(
                    entryId = entryId,
                    languageCode = languageCode,
                    checkedStatus = checkedStatus
                )
            }
            refresh()
        }
    }

    fun openEntry(entryId: Long) {
        uiState = uiState.copy(selectedEntryId = entryId)
    }

    fun closeEntry() {
        uiState = uiState.copy(selectedEntryId = null)
    }

    fun applyRecentSearch(recentSearch: RecentSearch) {
        uiState = uiState.copy(
            query = recentSearch.query,
            sourceLanguageCode = recentSearch.sourceLanguageCode,
            targetLanguageCode = recentSearch.targetLanguageCode
        ).resetTrainingState()
        refresh()
    }

    fun setTrainingWordSource(source: TrainingWordSource) {
        if (uiState.trainingWordSource == source) {
            return
        }

        trainingSearchJob?.cancel()
        uiState = uiState.copy(trainingWordSource = source).resetTrainingState()
    }

    fun updateTrainingPrompt(prompt: String) {
        trainingSearchJob?.cancel()
        uiState = uiState.copy(
            trainingPrompt = prompt,
            selectedTrainingEntry = null,
            isTrainingAnswerVisible = false,
            isTrainingLoading = false
        )

        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isBlank()) {
            uiState = uiState.copy(trainingSuggestions = emptyList())
            return
        }

        val snapshot = uiState
        trainingSearchJob = viewModelScope.launch {
            delay(120)

            val suggestions = withContext(Dispatchers.IO) {
                repository.search(
                    query = trimmedPrompt,
                    sourceLanguageCode = snapshot.sourceLanguageCode,
                    targetLanguageCode = snapshot.targetLanguageCode,
                    showAllTranslations = snapshot.showAllTranslations,
                    includeDraftTranslations = snapshot.showDraftTranslations,
                    limit = 8,
                    offset = 0,
                    recordRecentSearch = false
                ).results
            }

            if (!isActive) {
                return@launch
            }

            uiState = uiState.copy(trainingSuggestions = suggestions)
        }
    }

    fun selectTrainingEntry(entry: DictionaryEntryResult) {
        val sourceText = trainingTextFor(entry, uiState.sourceLanguageCode)
        uiState = uiState.copy(
            trainingWordSource = TrainingWordSource.CHOSEN,
            trainingPrompt = sourceText ?: uiState.trainingPrompt,
            trainingSuggestions = emptyList(),
            selectedTrainingEntry = entry,
            isTrainingAnswerVisible = false,
            isTrainingLoading = false
        )
    }

    fun loadRandomTrainingEntry() {
        if (uiState.isTrainingLoading) {
            return
        }

        trainingSearchJob?.cancel()
        val snapshot = uiState
        uiState = uiState.copy(
            trainingWordSource = TrainingWordSource.RANDOM,
            isTrainingLoading = true,
            isTrainingAnswerVisible = false,
            trainingSuggestions = emptyList()
        )

        viewModelScope.launch {
            val entry = withContext(Dispatchers.IO) {
                repository.getRandomTrainingEntry(
                    sourceLanguageCode = snapshot.sourceLanguageCode,
                    targetLanguageCode = snapshot.targetLanguageCode,
                    showAllTranslations = snapshot.showAllTranslations,
                    includeDraftTranslations = snapshot.showDraftTranslations
                )
            }

            if (!isActive) {
                return@launch
            }

            uiState = uiState.copy(
                selectedTrainingEntry = entry,
                isTrainingAnswerVisible = false,
                isTrainingLoading = false
            )
        }
    }

    fun revealTrainingAnswer() {
        if (uiState.selectedTrainingEntry == null) {
            return
        }
        uiState = uiState.copy(isTrainingAnswerVisible = true)
    }

    fun hideTrainingAnswer() {
        uiState = uiState.copy(isTrainingAnswerVisible = false)
    }

    fun loadMoreSearchResults() {
        val snapshot = uiState
        if (
            snapshot.isLoading ||
            snapshot.isLoadingMoreResults ||
            !snapshot.canLoadMoreSearchResults
        ) {
            return
        }

        uiState = uiState.copy(isLoadingMoreResults = true)

        viewModelScope.launch {
            val searchPage = withContext(Dispatchers.IO) {
                repository.search(
                    query = snapshot.query,
                    sourceLanguageCode = snapshot.sourceLanguageCode,
                    targetLanguageCode = snapshot.targetLanguageCode,
                    showAllTranslations = snapshot.showAllTranslations,
                    includeDraftTranslations = snapshot.showDraftTranslations,
                    limit = DictionaryRepository.SEARCH_PAGE_SIZE,
                    offset = snapshot.nextSearchOffset,
                    recordRecentSearch = false
                )
            }

            if (!isActive) {
                return@launch
            }

            uiState = uiState.copy(
                searchResults = uiState.searchResults + searchPage.results,
                canLoadMoreSearchResults = searchPage.hasMore,
                nextSearchOffset = searchPage.nextOffset,
                isLoadingMoreResults = false
            )
        }
    }

    private fun refresh() {
        refreshJob?.cancel()
        val snapshot = uiState

        refreshJob = viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                isLoadingMoreResults = false,
                canLoadMoreSearchResults = false,
                nextSearchOffset = 0
            )
            delay(180)

            val searchPage = withContext(Dispatchers.IO) {
                repository.search(
                    query = snapshot.query,
                    sourceLanguageCode = snapshot.sourceLanguageCode,
                    targetLanguageCode = snapshot.targetLanguageCode,
                    showAllTranslations = snapshot.showAllTranslations,
                    includeDraftTranslations = snapshot.showDraftTranslations
                )
            }
            val favorites = withContext(Dispatchers.IO) {
                repository.getFavorites(
                    sourceLanguageCode = snapshot.sourceLanguageCode,
                    targetLanguageCode = snapshot.targetLanguageCode,
                    showAllTranslations = snapshot.showAllTranslations,
                    includeDraftTranslations = snapshot.showDraftTranslations
                )
            }
            val recentSearches = withContext(Dispatchers.IO) {
                repository.getRecentSearches()
            }

            if (!isActive) {
                return@launch
            }

            uiState = uiState.copy(
                searchResults = searchPage.results,
                favorites = favorites,
                recentSearches = recentSearches,
                canLoadMoreSearchResults = searchPage.hasMore,
                nextSearchOffset = searchPage.nextOffset,
                isLoading = false
            )
        }
    }

    private fun DictionaryUiState.resetTrainingState(): DictionaryUiState {
        trainingSearchJob?.cancel()
        return copy(
            trainingPrompt = "",
            trainingSuggestions = emptyList(),
            selectedTrainingEntry = null,
            isTrainingAnswerVisible = false,
            isTrainingLoading = false
        )
    }

    private fun trainingTextFor(
        entry: DictionaryEntryResult,
        languageCode: String
    ): String? {
        entry.avarText
            ?.takeIf { languageCode == AppLanguage.AV.code && it.isNotBlank() }
            ?.let { return it }

        return entry.translations
            .firstOrNull { it.languageCode == languageCode }
            ?.text
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {
        val coverageDirections = listOf(
            DirectionWordCount(AppLanguage.AV.code, AppLanguage.RU.code, count = 0),
            DirectionWordCount(AppLanguage.RU.code, AppLanguage.AV.code, count = 0),
            DirectionWordCount(AppLanguage.AV.code, AppLanguage.EN.code, count = 0),
            DirectionWordCount(AppLanguage.EN.code, AppLanguage.AV.code, count = 0)
        )
    }
}
