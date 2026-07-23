package com.example.dictionnary.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvDictionaryImporterNoteNormalizationTest {

    @Test
    fun normalizeEntryNotes_removesNativeCheckSuffix_whenNativeChecked() {
        val normalized = normalizeEntryNotes(
            notes = "auto-extracted from dictionary headword; needs native check",
            nativeChecked = "yes"
        )

        assertEquals("auto-extracted from dictionary headword", normalized)
    }

    @Test
    fun normalizeEntryNotes_keepsNativeCheckSuffix_whenNotChecked() {
        val normalized = normalizeEntryNotes(
            notes = "auto-extracted from dictionary headword; needs native check",
            nativeChecked = "no"
        )

        assertEquals(
            "auto-extracted from dictionary headword; needs native check",
            normalized
        )
    }

    @Test
    fun normalizeEntryNotes_preservesOtherGrammarNotes_whenChecked() {
        val normalized = normalizeEntryNotes(
            notes = "auto-extracted from dictionary headword; grammar/label: (-ялъ, -ялъул); needs native check",
            nativeChecked = "yes"
        )

        assertEquals(
            "auto-extracted from dictionary headword; grammar/label: (-ялъ, -ялъул)",
            normalized
        )
    }
}
