package com.example.dictionnary.data.local

import org.junit.Assert.assertEquals
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
}
