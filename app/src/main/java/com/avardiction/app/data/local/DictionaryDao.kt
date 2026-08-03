package com.avardiction.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DictionaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<EntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(recentSearch: RecentSearchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: CorrectionEntity)

    @Query("DELETE FROM entries")
    suspend fun deleteAllEntries()

    @Query(
        """
        UPDATE translations
        SET checkedStatus = :checkedStatus
        WHERE entryId = :entryId
          AND languageCode = :languageCode
        """
    )
    suspend fun updateTranslationCheckedStatus(
        entryId: Long,
        languageCode: String,
        checkedStatus: String
    )

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun countEntries(): Int

    @Query(
        """
        SELECT COUNT(DISTINCT entryId)
        FROM translations
        WHERE languageCode = :languageCode
          AND (:includeDraft = 1 OR checkedStatus NOT IN ('draft', 'machine'))
        """
    )
    suspend fun countEntriesForLanguage(
        languageCode: String,
        includeDraft: Boolean
    ): Int

    @Query(
        """
        SELECT COUNT(DISTINCT source.entryId)
        FROM translations AS source
        WHERE source.languageCode = :sourceLanguage
          AND (:includeDraft = 1 OR source.checkedStatus NOT IN ('draft', 'machine'))
          AND EXISTS (
            SELECT 1
            FROM translations AS answer
            WHERE answer.entryId = source.entryId
              AND answer.languageCode = :targetLanguage
              AND (
                :includeDraft = 1 OR
                answer.checkedStatus NOT IN ('draft', 'machine') OR
                answer.languageCode = 'av'
              )
          )
        """
    )
    suspend fun countDirectionEntries(
        sourceLanguage: String,
        targetLanguage: String,
        includeDraft: Boolean
    ): Int

    @Query(
        """
        SELECT DISTINCT entryId
        FROM translations
        WHERE languageCode = :sourceLanguage
          AND (:includeDraft = 1 OR checkedStatus NOT IN ('draft', 'machine'))
          AND EXISTS (
            SELECT 1
            FROM translations AS answer
            WHERE answer.entryId = translations.entryId
              AND answer.languageCode != :sourceLanguage
              AND (
                :includeDraft = 1 OR
                answer.checkedStatus NOT IN ('draft', 'machine') OR
                answer.languageCode = 'av'
              )
              AND (
                :targetLanguage = 'all' OR
                :showAllTranslations = 1 OR
                answer.languageCode = :targetLanguage
              )
          )
          AND (
            :normalizedQuery = '' OR
            normalizedText LIKE '%' || :normalizedQuery || '%'
          )
        ORDER BY
          CASE
            WHEN :normalizedQuery = '' THEN 0
            WHEN normalizedText = :normalizedQuery THEN 1
            WHEN normalizedText LIKE :normalizedQuery || '%' THEN 2
            WHEN normalizedText LIKE '% ' || :normalizedQuery || '%' THEN 3
            ELSE 4
          END,
          LENGTH(normalizedText),
          normalizedText
        LIMIT :limit
        OFFSET :offset
        """
    )
    suspend fun searchEntryIds(
        normalizedQuery: String,
        sourceLanguage: String,
        targetLanguage: String,
        showAllTranslations: Boolean,
        includeDraft: Boolean,
        limit: Int,
        offset: Int
    ): List<Long>

    @Query(
        """
        SELECT DISTINCT source.browseKey
        FROM translations AS source
        WHERE source.languageCode = :sourceLanguage
          AND source.browseKey IS NOT NULL
          AND source.browseKey != ''
          AND (:includeDraft = 1 OR source.checkedStatus NOT IN ('draft', 'machine'))
          AND EXISTS (
            SELECT 1
            FROM translations AS answer
            WHERE answer.entryId = source.entryId
              AND answer.languageCode != :sourceLanguage
              AND (
                :includeDraft = 1 OR
                answer.checkedStatus NOT IN ('draft', 'machine') OR
                answer.languageCode = 'av'
              )
              AND (
                :targetLanguage = 'all' OR
                :showAllTranslations = 1 OR
                answer.languageCode = :targetLanguage
              )
          )
        ORDER BY source.browseKey COLLATE NOCASE
        """
    )
    suspend fun getBrowseFirstLetters(
        sourceLanguage: String,
        targetLanguage: String,
        showAllTranslations: Boolean,
        includeDraft: Boolean
    ): List<String>

    @Query(
        """
        SELECT DISTINCT source.entryId
        FROM translations AS source
        WHERE source.languageCode = :sourceLanguage
          AND source.browseKey = :browseKey
          AND (:includeDraft = 1 OR source.checkedStatus NOT IN ('draft', 'machine'))
          AND EXISTS (
            SELECT 1
            FROM translations AS answer
            WHERE answer.entryId = source.entryId
              AND answer.languageCode != :sourceLanguage
              AND (
                :includeDraft = 1 OR
                answer.checkedStatus NOT IN ('draft', 'machine') OR
                answer.languageCode = 'av'
              )
              AND (
                :targetLanguage = 'all' OR
                :showAllTranslations = 1 OR
                answer.languageCode = :targetLanguage
              )
          )
        ORDER BY source.normalizedText
        """
    )
    suspend fun getBrowseEntryIdsByBrowseKey(
        browseKey: String,
        sourceLanguage: String,
        targetLanguage: String,
        showAllTranslations: Boolean,
        includeDraft: Boolean
    ): List<Long>

    @Query(
        """
        SELECT DISTINCT source.entryId
        FROM translations AS source
        WHERE source.languageCode = :sourceLanguage
          AND (:includeDraft = 1 OR source.checkedStatus NOT IN ('draft', 'machine'))
          AND EXISTS (
            SELECT 1
            FROM translations AS answer
            WHERE answer.entryId = source.entryId
              AND answer.languageCode != :sourceLanguage
              AND (
                :includeDraft = 1 OR
                answer.checkedStatus NOT IN ('draft', 'machine') OR
                answer.languageCode = 'av'
              )
              AND (
                :targetLanguage = 'all' OR
                :showAllTranslations = 1 OR
                answer.languageCode = :targetLanguage
              )
          )
        ORDER BY RANDOM()
        LIMIT 1
        """
    )
    suspend fun getRandomTrainingEntryId(
        sourceLanguage: String,
        targetLanguage: String,
        showAllTranslations: Boolean,
        includeDraft: Boolean
    ): Long?

    @Query("SELECT * FROM entries WHERE id IN (:entryIds)")
    suspend fun getEntriesByIds(entryIds: List<Long>): List<EntryEntity>

    @Query("SELECT * FROM translations WHERE entryId IN (:entryIds)")
    suspend fun getTranslationsByEntryIds(entryIds: List<Long>): List<TranslationEntity>

    @Query("SELECT entryId FROM favorites ORDER BY createdAt DESC")
    suspend fun getFavoriteEntryIds(): List<Long>

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    suspend fun getFavorites(): List<FavoriteEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE entryId = :entryId)")
    suspend fun isFavorite(entryId: Long): Boolean

    @Query("DELETE FROM favorites WHERE entryId = :entryId")
    suspend fun deleteFavorite(entryId: Long)

    @Query("SELECT * FROM recent_searches ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentSearches(limit: Int): List<RecentSearchEntity>

    @Query("SELECT * FROM corrections ORDER BY createdAt ASC")
    suspend fun getCorrections(): List<CorrectionEntity>

    @Query(
        """
        DELETE FROM recent_searches
        WHERE normalizedQuery = :normalizedQuery
          AND sourceLanguageCode = :sourceLanguageCode
          AND targetLanguageCode = :targetLanguageCode
        """
    )
    suspend fun deleteDuplicateRecentSearch(
        normalizedQuery: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    )

    @Query(
        """
        DELETE FROM recent_searches
        WHERE id NOT IN (
            SELECT id FROM recent_searches
            ORDER BY createdAt DESC
            LIMIT :keepLimit
        )
        """
    )
    suspend fun trimRecentSearches(keepLimit: Int)

    @Query("SELECT * FROM corrections WHERE exported = 0 ORDER BY createdAt ASC")
    suspend fun getPendingCorrections(): List<CorrectionEntity>
}
