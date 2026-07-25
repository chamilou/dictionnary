package com.avardiction.app.data.local

import java.text.Normalizer
import java.util.Locale

object SearchNormalizer {
    val avarBrowseLetters: List<String> = listOf(
        "а", "б", "в", "г", "гъ", "гь", "гі", "д", "е", "ё", "ж", "з", "и", "й",
        "к", "къ", "кь", "кі", "л", "лъ", "м", "н", "о", "п", "р", "с", "т", "ті",
        "у", "ф", "х", "хъ", "хь", "хі", "ц", "ці", "ч", "чі", "ш", "щ", "ъ", "ы",
        "ь", "э", "ю", "я"
    )

    private val avarBrowseModifierChars = setOf('ь', 'ъ', 'і')
    private val avarBrowseLetterOrder = avarBrowseLetters.withIndex().associate { it.value to it.index }

    fun normalize(text: String, languageCode: String): String {
        val lowered = text
            .trim()
            .lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), " ")

        val withoutDiacritics = removeDiacritics(lowered)

        return if (languageCode == "av" || languageCode == "all") {
            // For Avar, we also need to map Latin characters (often used for accents) 
            // to their Cyrillic equivalents so they match standard typing.
            val unifiedDigraphs = unifyAvarDigraphs(withoutDiacritics)
            val unified = unifyAvarCharacters(unifiedDigraphs)
            unifyPalochka(unified, includeDigitFallback = true)
        } else {
            withoutDiacritics
        }
    }

    fun extractBrowseLetter(text: String, languageCode: String): String? {
        val normalized = if (languageCode == "av") {
            normalizeAvarForBrowse(text)
        } else {
            normalize(text, languageCode)
        }

        var index = 0
        while (index < normalized.length) {
            val char = normalized[index]
            if (!char.isLetter()) {
                index += 1
                continue
            }

            return buildString {
                append(char)
                normalized
                    .getOrNull(index + 1)
                    ?.takeIf { it in avarBrowseModifierChars && languageCode == "av" }
                    ?.let(::append)
            }
        }

        return null
    }

    fun sortBrowseLetters(letters: Collection<String>, languageCode: String): List<String> {
        val normalizedLetters = letters
            .mapNotNull { extractBrowseLetter(it, languageCode) }
            .let { extractedLetters ->
                if (languageCode == "av") {
                    extractedLetters.filter { it in avarBrowseLetterOrder }
                } else {
                    extractedLetters
                }
            }
            .distinct()

        return if (languageCode == "av") {
            normalizedLetters.sortedWith(::compareAvarBrowseLetters)
        } else {
            normalizedLetters.sortedBy { it.uppercase() }
        }
    }

    private fun removeDiacritics(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        return normalized.replace("\\p{Mn}+".toRegex(), "")
    }

    private fun unifyAvarCharacters(value: String): String {
        return value
            .replace('a', 'а')
            .replace('e', 'е')
            .replace('i', 'и')
            .replace('o', 'о')
            .replace('u', 'у')
            .replace('y', 'й')
    }

    private fun unifyAvarDigraphs(value: String): String {
        return value
            .replace("k|", "къ")
            .replace("x|", "хъ")
            .replace("гi", "гі")
            .replace("кi", "кі")
            .replace("лi", "лі")
            .replace("тi", "ті")
            .replace("хi", "хі")
            .replace("цi", "ці")
            .replace("чi", "чі")
            .replace("гӏ", "гі")
            .replace("кӏ", "кі")
            .replace("лӏ", "лі")
            .replace("тӏ", "ті")
            .replace("хӏ", "хі")
            .replace("цӏ", "ці")
            .replace("чӏ", "чі")
    }

    private fun normalizeAvarForBrowse(text: String): String {
        val lowered = text
            .trim()
            .lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), " ")
        val withoutDiacritics = removeDiacritics(lowered)
        val unifiedDigraphs = unifyAvarDigraphs(withoutDiacritics)
        val unified = unifyAvarCharacters(unifiedDigraphs)
        return unifyPalochka(unified, includeDigitFallback = false)
    }

    private fun compareAvarBrowseLetters(left: String, right: String): Int {
        val leftIndex = avarBrowseLetterOrder[left] ?: Int.MAX_VALUE
        val rightIndex = avarBrowseLetterOrder[right] ?: Int.MAX_VALUE
        return if (leftIndex != rightIndex) {
            leftIndex.compareTo(rightIndex)
        } else {
            left.compareTo(right)
        }
    }

    private fun unifyPalochka(
        value: String,
        includeDigitFallback: Boolean
    ): String {
        // Unify various Palochka-like characters to \u0456 (Cyrillic small i)
        // We do this AFTER vowel unification to avoid conflicts where possible.
        // In Avar, Palochka is often typed as '1' or 'I'.
        val unified = value
            .replace("Ӏ", "і") // U+04C0
            .replace("ӏ", "і") // U+04CF
            .replace("l", "і") // Latin l
            .replace("і", "і")

        return if (includeDigitFallback) {
            unified
                .replace("1", "і") // Digit 1
                .replace("I", "і") // Latin capital I
        } else {
            unified
        }
    }
}
