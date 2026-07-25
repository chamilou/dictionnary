package com.avardiction.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchNormalizerTest {
    @Test
    fun normalize_trimsLowercasesAndCollapsesWhitespace() {
        assertEquals(
            "вода лед",
            SearchNormalizer.normalize("  ВОДА   ЛЕД  ", languageCode = "ru")
        )
    }

    @Test
    fun normalize_removesDiacriticsForAvar() {
        assertEquals(
            "абадияб",
            SearchNormalizer.normalize("áбадияб", languageCode = "av")
        )
    }

    @Test
    fun normalize_unifiesCommonAvarLatinFallbackCharacters() {
        assertEquals(
            "аіъ",
            SearchNormalizer.normalize("alъ", languageCode = "av")
        )
    }

    @Test
    fun normalize_nonAvarTextDoesNotApplyAvarCharacterMapping() {
        assertEquals(
            "resume",
            SearchNormalizer.normalize("Résumé", languageCode = "en")
        )
    }

    @Test
    fun normalize_unifiesKPipeToKhaHardSequenceForAvarSearch() {
        assertEquals(
            "къабза",
            SearchNormalizer.normalize("k|абза", languageCode = "av")
        )
    }

    @Test
    fun normalize_unifiesXPipeToKhaHardSequenceForAvarSearch() {
        assertEquals(
            "хъабар",
            SearchNormalizer.normalize("x|абар", languageCode = "av")
        )
    }

    @Test
    fun normalize_unifiesMixedScriptPalochkaFallbackAfterCyrillicConsonant() {
        assertEquals(
            "чіагьил",
            SearchNormalizer.normalize("чIагьил", languageCode = "av")
        )
    }

    @Test
    fun normalize_unifiesCyrillicPalochkaAfterCyrillicConsonant() {
        assertEquals(
            "хіабар",
            SearchNormalizer.normalize("хӀабар", languageCode = "av")
        )
    }

    @Test
    fun normalize_treatsMixedAndCyrillicPalochkaQueriesTheSame() {
        assertEquals(
            SearchNormalizer.normalize("хIама", languageCode = "av"),
            SearchNormalizer.normalize("хӀама", languageCode = "av")
        )
    }

    @Test
    fun extractBrowseLetter_treatsAvarCompoundLettersAsDistinctLetters() {
        assertEquals(
            "гь",
            SearchNormalizer.extractBrowseLetter("Гьаб", languageCode = "av")
        )
    }

    @Test
    fun extractBrowseLetter_skipsDigitsAndColonForAvarBrowse() {
        assertEquals(
            "къ",
            SearchNormalizer.extractBrowseLetter(" 12: Къабза", languageCode = "av")
        )
    }

    @Test
    fun extractBrowseLetter_keepsPalochkaLettersInAvarBrowse() {
        assertEquals(
            "чі",
            SearchNormalizer.extractBrowseLetter("чIагьил", languageCode = "av")
        )
    }

    @Test
    fun avarBrowseLetters_matchesExpectedAlphabetSizeAndLetters() {
        assertEquals(46, SearchNormalizer.avarBrowseLetters.size)
        assertTrue("ці" in SearchNormalizer.avarBrowseLetters)
        assertFalse("вь" in SearchNormalizer.avarBrowseLetters)
        assertFalse("нь" in SearchNormalizer.avarBrowseLetters)
        assertFalse("ль" in SearchNormalizer.avarBrowseLetters)
        assertFalse("пь" in SearchNormalizer.avarBrowseLetters)
        assertFalse("съ" in SearchNormalizer.avarBrowseLetters)
        assertFalse("сь" in SearchNormalizer.avarBrowseLetters)
    }

    @Test
    fun sortBrowseLetters_filtersOutNonAvarLetterCombinations() {
        assertEquals(
            listOf("гь", "ці"),
            SearchNormalizer.sortBrowseLetters(
                letters = listOf("вь", "гь", "ці", "нь"),
                languageCode = "av"
            )
        )
    }
}
