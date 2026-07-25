package com.avardiction.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvDictionaryImporterNoteNormalizationTest {

    @Test
    fun normalizeEntryNotes_removesNativeCheckSuffix_whenNativeChecked() {
        val normalized = normalizeEntryNotes(
            notes = "auto-extracted from dictionary headword; needs native check",
            nativeChecked = "yes"
        )

        assertEquals("", normalized)
    }

    @Test
    fun normalizeEntryNotes_removesNativeCheckSuffix_whenNotChecked() {
        val normalized = normalizeEntryNotes(
            notes = "auto-extracted from dictionary headword; needs native check",
            nativeChecked = "no"
        )

        assertEquals("", normalized)
    }

    @Test
    fun normalizeEntryNotes_preservesOtherGrammarNotes_whenChecked() {
        val normalized = normalizeEntryNotes(
            notes = "auto-extracted from dictionary headword; grammar/label: (-ялъ, -ялъул); needs native check",
            nativeChecked = "yes"
        )

        assertEquals("grammar/label: (-ялъ, -ялъул)", normalized)
    }

    @Test
    fun shouldIndexAvarHeadword_skipsBrokenBarSeparatorEntries() {
        assertFalse(shouldIndexAvarHeadword("¦бго"))
    }

    @Test
    fun shouldIndexAvarHeadword_skipsHyphenPrefixedEntries() {
        assertFalse(shouldIndexAvarHeadword("-гъун"))
    }

    @Test
    fun shouldIndexAvarHeadword_keepsRegularAvarWords() {
        assertTrue(shouldIndexAvarHeadword("гьаб"))
    }

    @Test
    fun shouldIndexRussianTranslation_skipsParenthesisPrefixedEntries() {
        assertFalse(shouldIndexRussianTranslation("(по)везти"))
    }

    @Test
    fun shouldIndexRussianTranslation_keepsRegularRussianWords() {
        assertTrue(shouldIndexRussianTranslation("везти"))
    }
}
