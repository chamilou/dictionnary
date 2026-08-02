package com.avardiction.app.data.repository

import android.content.Context
import com.avardiction.app.data.local.CorrectionEntity
import com.avardiction.app.data.local.CsvDictionaryImporter
import com.avardiction.app.data.local.DictionaryDatabase
import com.avardiction.app.data.local.EntryEntity
import com.avardiction.app.data.local.FavoriteEntity
import com.avardiction.app.data.local.RecentSearchEntity
import com.avardiction.app.data.local.SearchNormalizer
import com.avardiction.app.data.local.TranslationEntity
import com.avardiction.app.domain.model.AppLanguage
import com.avardiction.app.domain.model.DictionaryEntryResult
import com.avardiction.app.domain.model.EntryTranslation
import com.avardiction.app.domain.model.RecentSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DictionaryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database = DictionaryDatabase.getInstance(appContext)
    private val dao = database.dictionaryDao()
    private val importer = CsvDictionaryImporter(appContext)
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val seedAssetFingerprint by lazy { importer.assetFingerprint(SEED_ASSET_NAME) }
    private val seedMutex = Mutex()
    private val _seedStatus = MutableStateFlow<SeedStatus>(SeedStatus.Idle)
    val seedStatus: StateFlow<SeedStatus> = _seedStatus.asStateFlow()

    suspend fun search(
        query: String,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean,
        limit: Int = SEARCH_PAGE_SIZE,
        offset: Int = 0,
        recordRecentSearch: Boolean = true
    ): SearchPage {
        seedIfNeeded()

        val normalizedQuery = SearchNormalizer.normalize(query, sourceLanguageCode)
        val entryIds = dao.searchEntryIds(
            normalizedQuery = normalizedQuery,
            sourceLanguage = sourceLanguageCode,
            targetLanguage = targetLanguageCode,
            showAllTranslations = showAllTranslations,
            includeDraft = includeDraftTranslations,
            limit = limit + 1,
            offset = offset
        )

        val hasMore = entryIds.size > limit
        val pageEntryIds = if (hasMore) entryIds.take(limit) else entryIds
        val bridgeEntryIds = if (
            sourceLanguageCode == AppLanguage.EN.code &&
            query.isNotBlank() &&
            pageEntryIds.size < limit
        ) {
            loadEnglishBridgeEntryIds(
                primaryEntryIds = pageEntryIds,
                includeDraftTranslations = includeDraftTranslations,
                remainingLimit = limit - pageEntryIds.size
            )
        } else {
            emptyList()
        }
        val resultEntryIds = pageEntryIds + bridgeEntryIds

        if (recordRecentSearch && query.isNotBlank()) {
            recordRecentSearch(query, normalizedQuery, sourceLanguageCode, targetLanguageCode)
        }

        return SearchPage(
            results = buildEntryResults(
                entryIds = resultEntryIds,
                sourceLanguageCode = sourceLanguageCode,
                targetLanguageCode = targetLanguageCode,
                showAllTranslations = showAllTranslations,
                includeDraftTranslations = includeDraftTranslations,
                searchMatchLanguageCode = sourceLanguageCode,
                bridgeEntryIds = bridgeEntryIds.toSet()
            ),
            nextOffset = offset + pageEntryIds.size,
            hasMore = hasMore
        )
    }

    suspend fun getFavorites(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean
    ): List<DictionaryEntryResult> {
        seedIfNeeded()
        val favoriteIds = dao.getFavoriteEntryIds()
        if (favoriteIds.isEmpty()) {
            return emptyList()
        }

        return buildEntryResults(
            entryIds = favoriteIds,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            showAllTranslations = showAllTranslations,
            includeDraftTranslations = includeDraftTranslations
        )
    }

    suspend fun getBrowseFirstLetters(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean
    ): List<String> {
        seedIfNeeded()
        return SearchNormalizer.sortBrowseLetters(
            letters = dao.getBrowseFirstLetters(
                sourceLanguage = sourceLanguageCode,
                targetLanguage = targetLanguageCode,
                showAllTranslations = showAllTranslations,
                includeDraft = includeDraftTranslations
            ),
            languageCode = sourceLanguageCode
        )
    }

    suspend fun getBrowseEntriesForLetter(
        letter: String,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean
    ): List<DictionaryEntryResult> {
        seedIfNeeded()

        val browseKey = SearchNormalizer.extractBrowseLetter(letter, sourceLanguageCode)
            ?: return emptyList()

        val entryIds = dao.getBrowseEntryIdsByBrowseKey(
            browseKey = browseKey,
            sourceLanguage = sourceLanguageCode,
            targetLanguage = targetLanguageCode,
            showAllTranslations = showAllTranslations,
            includeDraft = includeDraftTranslations
        )

        return buildEntryResults(
            entryIds = entryIds,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            showAllTranslations = showAllTranslations,
            includeDraftTranslations = includeDraftTranslations
        )
    }

    suspend fun getRandomTrainingEntry(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean
    ): DictionaryEntryResult? {
        seedIfNeeded()

        val entryId = dao.getRandomTrainingEntryId(
            sourceLanguage = sourceLanguageCode,
            targetLanguage = targetLanguageCode,
            showAllTranslations = showAllTranslations,
            includeDraft = includeDraftTranslations
        ) ?: return null

        return buildEntryResults(
            entryIds = listOf(entryId),
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            showAllTranslations = showAllTranslations,
            includeDraftTranslations = includeDraftTranslations
        ).firstOrNull()
    }

    suspend fun getEntryCount(): Int {
        seedIfNeeded()
        return dao.countEntries()
    }

    suspend fun countLanguageWords(
        languageCode: String,
        includeDraftTranslations: Boolean
    ): Int {
        seedIfNeeded()
        return dao.countEntriesForLanguage(
            languageCode = languageCode,
            includeDraft = includeDraftTranslations
        )
    }

    suspend fun countDirectionWords(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        includeDraftTranslations: Boolean
    ): Int {
        seedIfNeeded()
        return dao.countDirectionEntries(
            sourceLanguage = sourceLanguageCode,
            targetLanguage = targetLanguageCode,
            includeDraft = includeDraftTranslations
        )
    }

    suspend fun toggleFavorite(entryId: Long) {
        seedIfNeeded()
        if (dao.isFavorite(entryId)) {
            dao.deleteFavorite(entryId)
        } else {
            dao.insertFavorite(
                FavoriteEntity(
                    entryId = entryId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getRecentSearches(): List<RecentSearch> {
        seedIfNeeded()
        return dao.getRecentSearches(limit = 10).map {
            RecentSearch(
                id = it.id,
                query = it.query,
                sourceLanguageCode = it.sourceLanguageCode,
                targetLanguageCode = it.targetLanguageCode,
                createdAt = it.createdAt
            )
        }
    }

    suspend fun addCorrection(
        entryId: Long,
        languageCode: String,
        oldText: String,
        suggestedText: String,
        comment: String
    ) {
        dao.insertCorrection(
            CorrectionEntity(
                entryId = entryId,
                languageCode = languageCode,
                oldText = oldText,
                suggestedText = suggestedText,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTranslationCheckedStatus(
        entryId: Long,
        languageCode: String,
        checkedStatus: String
    ) {
        seedIfNeeded()
        dao.updateTranslationCheckedStatus(
            entryId = entryId,
            languageCode = languageCode,
            checkedStatus = checkedStatus
        )
    }

    private suspend fun buildEntryResults(
        entryIds: List<Long>,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean,
        searchMatchLanguageCode: String? = null,
        bridgeEntryIds: Set<Long> = emptySet()
    ): List<DictionaryEntryResult> {
        if (entryIds.isEmpty()) {
            return emptyList()
        }

        val entriesById = dao.getEntriesByIds(entryIds).associateBy { it.id }
        val favoriteIds = dao.getFavoriteEntryIds().toSet()
        val translationsByEntryId = dao.getTranslationsByEntryIds(entryIds)
            .groupBy { it.entryId }

        return entryIds.mapNotNull { entryId ->
            val entry = entriesById[entryId] ?: return@mapNotNull null
            val allTranslations = translationsByEntryId[entryId].orEmpty()
                .map { it.toDomainTranslation() }
                .sortedBy { AppLanguage.fromCode(it.languageCode).displayPriority }

            val visibleTranslations = filterVisibleTranslations(
                translations = allTranslations,
                sourceLanguageCode = sourceLanguageCode,
                targetLanguageCode = targetLanguageCode,
                showAllTranslations = showAllTranslations,
                includeDraftTranslations = includeDraftTranslations
            )

            val targetExists = showAllTranslations ||
                targetLanguageCode == AppLanguage.ALL.code ||
                visibleTranslations.any { it.languageCode == targetLanguageCode }

            if (visibleTranslations.isEmpty() || !targetExists) {
                return@mapNotNull null
            }

            DictionaryEntryResult(
                entryId = entry.id,
                avarText = allTranslations.firstOrNull { it.languageCode == AppLanguage.AV.code }?.text,
                translations = visibleTranslations,
                category = entry.category,
                type = entry.type,
                notes = entry.notes,
                sourceFile = entry.sourceFile,
                sourcePage = entry.sourcePage,
                isFavorite = entry.id in favoriteIds,
                matchedLanguageCode = when {
                    searchMatchLanguageCode == null -> null
                    entry.id in bridgeEntryIds -> AppLanguage.RU.code
                    else -> searchMatchLanguageCode
                },
                isRussianBridgeResult = entry.id in bridgeEntryIds
            )
        }
    }

    private fun filterVisibleTranslations(
        translations: List<EntryTranslation>,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        showAllTranslations: Boolean,
        includeDraftTranslations: Boolean
    ): List<EntryTranslation> {
        val filtered = translations.filter {
            includeDraftTranslations || !it.isDraftLike || it.languageCode == AppLanguage.AV.code
        }

        if (showAllTranslations || targetLanguageCode == AppLanguage.ALL.code) {
            return filtered
        }

        val preferredCodes = linkedSetOf(AppLanguage.AV.code, sourceLanguageCode, targetLanguageCode)
        if (targetLanguageCode != AppLanguage.RU.code) {
            preferredCodes += AppLanguage.RU.code
        }

        return filtered.filter { it.languageCode in preferredCodes }
    }

    private suspend fun loadEnglishBridgeEntryIds(
        primaryEntryIds: List<Long>,
        includeDraftTranslations: Boolean,
        remainingLimit: Int
    ): List<Long> {
        if (primaryEntryIds.isEmpty() || remainingLimit <= 0) {
            return emptyList()
        }

        val bridgeQueries = dao.getTranslationsByEntryIds(primaryEntryIds)
            .asSequence()
            .filter { it.languageCode == AppLanguage.RU.code }
            .flatMap { translation ->
                russianBridgeQueries(translation.text).asSequence()
            }
            .distinct()
            .take(4)
            .toList()

        if (bridgeQueries.isEmpty()) {
            return emptyList()
        }

        val bridgedEntryIds = mutableListOf<Long>()
        for (bridgeQuery in bridgeQueries) {
            val matchedIds = dao.searchEntryIds(
                normalizedQuery = bridgeQuery,
                sourceLanguage = AppLanguage.RU.code,
                targetLanguage = AppLanguage.ALL.code,
                showAllTranslations = true,
                includeDraft = includeDraftTranslations,
                limit = remainingLimit * 2,
                offset = 0
            )

            matchedIds.forEach { entryId ->
                if (entryId !in primaryEntryIds && entryId !in bridgedEntryIds) {
                    bridgedEntryIds += entryId
                }
            }

            if (bridgedEntryIds.size >= remainingLimit) {
                break
            }
        }

        return bridgedEntryIds.take(remainingLimit)
    }

    private fun russianBridgeQueries(text: String): List<String> {
        return SearchNormalizer.normalize(text, AppLanguage.RU.code)
            .split(Regex("[^\\p{L}\\p{Nd}]+"))
            .asSequence()
            .filter { it.length >= 4 }
            .map { it.take(4) }
            .distinct()
            .toList()
    }

    private suspend fun recordRecentSearch(
        query: String,
        normalizedQuery: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ) {
        dao.deleteDuplicateRecentSearch(
            normalizedQuery = normalizedQuery,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode
        )
        dao.insertRecentSearch(
            RecentSearchEntity(
                query = query.trim(),
                normalizedQuery = normalizedQuery,
                sourceLanguageCode = sourceLanguageCode,
                targetLanguageCode = targetLanguageCode,
                createdAt = System.currentTimeMillis()
            )
        )
        dao.trimRecentSearches(keepLimit = 12)
    }

    private suspend fun seedIfNeeded() {
        seedMutex.withLock {
            val hasEntries = dao.countEntries() > 0
            val indexedSeedFingerprint = preferences.getString(KEY_INDEXED_SEED_FINGERPRINT, null)
            val indexedSeedVersion = preferences.getInt(KEY_INDEXED_SEED_VERSION, 0)

            if (
                hasEntries &&
                indexedSeedFingerprint == seedAssetFingerprint &&
                indexedSeedVersion == SEED_IMPORT_VERSION
            ) {
                _seedStatus.value = SeedStatus.Idle
                return
            }

            try {
                _seedStatus.value = SeedStatus.InProgress(SeedPhase.PREPARING)

                if (hasEntries) {
                    database.clearAllTables()
                }

                val imported = importer.import(SEED_ASSET_NAME) { progress ->
                    _seedStatus.value = SeedStatus.InProgress(
                        phase = SeedPhase.READING_ROWS,
                        processed = progress.processedRows,
                        total = 0
                    )
                }

                val entryChunks = imported.entries.chunked(500)
                entryChunks.forEachIndexed { index, chunk ->
                    dao.insertEntries(chunk)
                    _seedStatus.value = SeedStatus.InProgress(
                        phase = SeedPhase.INSERTING_ENTRIES,
                        processed = index + 1,
                        total = entryChunks.size
                    )
                }

                val translationChunks = imported.translations.chunked(1000)
                translationChunks.forEachIndexed { index, chunk ->
                    dao.insertTranslations(chunk)
                    _seedStatus.value = SeedStatus.InProgress(
                        phase = SeedPhase.INSERTING_TRANSLATIONS,
                        processed = index + 1,
                        total = translationChunks.size
                    )
                }

                _seedStatus.value = SeedStatus.InProgress(SeedPhase.FINALIZING)

                preferences.edit()
                    .putString(KEY_INDEXED_SEED_FINGERPRINT, seedAssetFingerprint)
                    .putInt(KEY_INDEXED_SEED_VERSION, SEED_IMPORT_VERSION)
                    .apply()
            } finally {
                _seedStatus.value = SeedStatus.Idle
            }
        }
    }

    private fun TranslationEntity.toDomainTranslation(): EntryTranslation {
        return EntryTranslation(
            languageCode = languageCode,
            text = text,
            checkedStatus = checkedStatus,
            translationSource = translationSource,
            sourceLanguageCode = sourceLanguageCode,
            isPrimary = isPrimary
        )
    }

    data class SearchPage(
        val results: List<DictionaryEntryResult>,
        val nextOffset: Int,
        val hasMore: Boolean
    )

    sealed interface SeedStatus {
        data object Idle : SeedStatus

        data class InProgress(
            val phase: SeedPhase,
            val processed: Int = 0,
            val total: Int = 0
        ) : SeedStatus
    }

    enum class SeedPhase {
        PREPARING,
        READING_ROWS,
        INSERTING_ENTRIES,
        INSERTING_TRANSLATIONS,
        FINALIZING
    }

    companion object {
        const val SEARCH_PAGE_SIZE = 100
        private const val PREFERENCES_NAME = "dictionary_preferences"
        private const val SEED_ASSET_NAME = "avar_russian_english.csv"
        private const val KEY_INDEXED_SEED_FINGERPRINT = "indexed_seed_fingerprint"
        private const val KEY_INDEXED_SEED_VERSION = "indexed_seed_version"
        private const val SEED_IMPORT_VERSION = 4
    }
}
