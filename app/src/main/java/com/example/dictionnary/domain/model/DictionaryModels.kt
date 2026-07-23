package com.example.dictionnary.domain.model

enum class AppLanguage(
    val code: String,
    val shortLabel: String,
    val displayName: String,
    val displayPriority: Int
) {
    ALL("all", "ALL", "All translations", 99),
    AV("av", "AV", "Avar", 0),
    EN("en", "EN", "English", 1),
    RU("ru", "RU", "Russian", 2),
    DE("de", "DE", "German", 3),
    ES("es", "ES", "Spanish", 4),
    FR("fr", "FR", "French", 5);

    companion object {
        val searchLanguages = listOf(AV, EN, RU, DE, ES, FR)
        val targetLanguages = listOf(ALL, AV, EN, RU, DE, ES, FR)

        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: EN
        }
    }
}

data class EntryTranslation(
    val languageCode: String,
    val text: String,
    val checkedStatus: String,
    val translationSource: String?,
    val sourceLanguageCode: String?,
    val isPrimary: Boolean
) {
    val isDraftLike: Boolean
        get() = checkedStatus == "draft" || checkedStatus == "machine"
}

data class DictionaryEntryResult(
    val entryId: Long,
    val avarText: String?,
    val translations: List<EntryTranslation>,
    val category: String?,
    val type: String?,
    val notes: String?,
    val sourceFile: String?,
    val sourcePage: String?,
    val isFavorite: Boolean,
    val matchedLanguageCode: String? = null,
    val isRussianBridgeResult: Boolean = false
)

data class RecentSearch(
    val id: Long,
    val query: String,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val createdAt: Long
)
