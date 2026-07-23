package com.example.dictionnary.data.local

import android.content.Context
import java.security.MessageDigest

class CsvDictionaryImporter(
    private val context: Context
) {
    fun assetFingerprint(assetName: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        context.assets.open(assetName).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) {
                    break
                }
                digest.update(buffer, 0, bytesRead)
            }
        }

        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    fun import(assetName: String): ImportedDictionaryData {
        context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { reader ->
            val headerLine = reader.readLine() ?: return ImportedDictionaryData(emptyList(), emptyList())
            val header = parseCsvLine(headerLine).map { it.removePrefix("\uFEFF") }
            val indices = header.withIndex().associate { it.value to it.index }

            val entries = mutableListOf<EntryEntity>()
            val translations = mutableListOf<TranslationEntity>()
            var nextEntryId = 1L
            val now = System.currentTimeMillis()

            reader.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val row = parseCsvLine(line)
                    val avar = preferredValue(row, indices, "corrected_avar", "avar")
                    val russian = preferredValue(row, indices, "corrected_russian", "russian")
                    val english = valueAt(row, indices["english"])

                    if (avar.isBlank() && russian.isBlank() && english.isBlank()) {
                        return@forEach
                    }

                    val category = valueAt(row, indices["category"])
                    val type = valueAt(row, indices["type"])
                    val nativeChecked = valueAt(row, indices["native_checked"])
                    val notes = normalizeEntryNotes(
                        notes = valueAt(row, indices["notes"]),
                        nativeChecked = nativeChecked
                    )
                    val sourceFile = valueAt(row, indices["source_file"])
                    val sourcePage = valueAt(row, indices["source_page"])
                    val englishChecked = valueAt(row, indices["english_checked"])
                    val englishSource = valueAt(row, indices["english_source"])
                    val avarRussianStatus = checkedStatusFromNativeReview(nativeChecked)

                    entries += EntryEntity(
                        id = nextEntryId,
                        category = category.ifBlank { null },
                        type = type.ifBlank { null },
                        notes = notes.ifBlank { null },
                        sourceFile = sourceFile.ifBlank { null },
                        sourcePage = sourcePage.ifBlank { null },
                        createdAt = now,
                        updatedAt = now
                    )

                    if (avar.isNotBlank()) {
                        translations += TranslationEntity(
                            entryId = nextEntryId,
                            languageCode = "av",
                            text = avar,
                            normalizedText = SearchNormalizer.normalize(avar, "av"),
                            isPrimary = true,
                            sourceLanguageCode = null,
                            translationSource = "avar_russian_dictionary",
                            checkedStatus = avarRussianStatus
                        )
                    }

                    if (russian.isNotBlank()) {
                        translations += TranslationEntity(
                            entryId = nextEntryId,
                            languageCode = "ru",
                            text = russian,
                            normalizedText = SearchNormalizer.normalize(russian, "ru"),
                            isPrimary = true,
                            sourceLanguageCode = "av",
                            translationSource = "avar_russian_dictionary",
                            checkedStatus = avarRussianStatus
                        )
                    }

                    if (english.isNotBlank()) {
                        translations += TranslationEntity(
                            entryId = nextEntryId,
                            languageCode = "en",
                            text = english,
                            normalizedText = SearchNormalizer.normalize(english, "en"),
                            isPrimary = true,
                            sourceLanguageCode = "ru",
                            translationSource = englishSource.ifBlank { "avar_russian_english_dictionary" },
                            checkedStatus = checkedStatusFromEnglishReview(
                                englishChecked = englishChecked,
                                englishSource = englishSource
                            )
                        )
                    }

                    nextEntryId += 1
                }

            return ImportedDictionaryData(entries, translations)
        }
    }

    private fun preferredValue(
        row: List<String>,
        indices: Map<String, Int>,
        correctedColumn: String,
        originalColumn: String
    ): String {
        val corrected = valueAt(row, indices[correctedColumn])
        if (corrected.isNotBlank()) {
            return corrected
        }
        return valueAt(row, indices[originalColumn])
    }

    private fun valueAt(row: List<String>, index: Int?): String {
        if (index == null || index >= row.size) {
            return ""
        }
        return row[index].trim()
    }

    private fun checkedStatusFromNativeReview(nativeChecked: String): String {
        return if (nativeChecked.equals("yes", ignoreCase = true)) {
            "reliable"
        } else {
            "needs_review"
        }
    }

    private fun checkedStatusFromEnglishReview(
        englishChecked: String,
        englishSource: String
    ): String {
        return when {
            englishChecked.equals("yes", ignoreCase = true) -> "reliable"
            englishChecked.equals("checked", ignoreCase = true) -> "reliable"
            englishChecked.equals("native_checked", ignoreCase = true) -> "reliable"
            englishSource.equals("pending_translation", ignoreCase = true) -> "draft"
            else -> "needs_review"
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i += 1
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    values += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            i += 1
        }

        values += current.toString()
        return values
    }
}

data class ImportedDictionaryData(
    val entries: List<EntryEntity>,
    val translations: List<TranslationEntity>
)

internal fun normalizeEntryNotes(
    notes: String,
    nativeChecked: String
): String {
    val trimmedNotes = notes.trim()
    if (trimmedNotes.isBlank()) {
        return ""
    }
    if (!nativeChecked.equals("yes", ignoreCase = true)) {
        return trimmedNotes
    }

    return trimmedNotes
        .replace(Regex("""(?:[;,]\s*|\s+)needs native check\.?$""", RegexOption.IGNORE_CASE), "")
        .trim()
        .trimEnd(';', ',', ' ')
}
