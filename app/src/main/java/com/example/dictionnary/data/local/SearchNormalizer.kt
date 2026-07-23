package com.example.dictionnary.data.local

import java.text.Normalizer
import java.util.Locale

object SearchNormalizer {
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
            unifyPalochka(unified)
        } else {
            withoutDiacritics
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

    private fun unifyPalochka(value: String): String {
        // Unify various Palochka-like characters to \u0456 (Cyrillic small i)
        // We do this AFTER vowel unification to avoid conflicts where possible.
        // In Avar, Palochka is often typed as '1' or 'I'.
        return value
            .replace("Ӏ", "і") // U+04C0
            .replace("ӏ", "і") // U+04CF
            .replace("l", "і") // Latin l
            .replace("1", "і") // Digit 1
            .replace("I", "і") // Latin capital I
            .replace("і", "і")
    }
}
