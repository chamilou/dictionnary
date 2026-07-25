package com.avardiction.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DictionaryCsvStructureTest {
    private val csvFile = File("src/main/assets/avar_russian_english.csv")

    @Test
    fun csv_existsAndIncludesExpectedColumns() {
        assertTrue("CSV asset should exist for first-run import", csvFile.exists())

        val reader = csvFile.bufferedReader(Charsets.UTF_8)
        reader.use {
            val header = parseCsvLine(it.readLine().orEmpty())
            assertEquals(
                listOf(
                    "avar",
                    "russian",
                    "english",
                    "muller_printed_page",
                    "english_raw_muller",
                    "russian_key",
                    "english_checked",
                    "english_source",
                    "english_notes",
                    "category",
                    "type",
                    "notes",
                    "source_file",
                    "source_page",
                    "native_checked",
                    "corrected_avar",
                    "corrected_russian"
                ),
                header.map { value -> value.removePrefix("\uFEFF") }
            )
        }
    }

    @Test
    fun csv_hasManyImportableRows() {
        assertTrue("CSV asset should exist for first-run import", csvFile.exists())

        var importableRows = 0
        csvFile.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readLine()
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val row = parseCsvLine(line)
                    val avar = preferredValue(row, 15, 0)
                    val russian = preferredValue(row, 16, 1)
                    val english = row.getOrNull(2).orEmpty().trim()
                    if (avar.isNotBlank() || russian.isNotBlank() || english.isNotBlank()) {
                        importableRows += 1
                    }
                }
        }

        assertTrue(
            "Expected a substantial imported dataset, found only $importableRows rows",
            importableRows > 1000
        )
    }

    @Test
    fun csv_hasManyEnglishTranslations() {
        assertTrue("CSV asset should exist for first-run import", csvFile.exists())

        var englishRows = 0
        csvFile.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readLine()
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val row = parseCsvLine(line)
                    val english = row.getOrNull(2).orEmpty().trim()
                    if (english.isNotBlank()) {
                        englishRows += 1
                    }
                }
        }

        assertTrue(
            "Expected a substantial English dataset, found only $englishRows rows",
            englishRows > 5000
        )
    }

    @Test
    fun csv_hasNoDuplicateBaseRows() {
        assertTrue("CSV asset should exist for first-run import", csvFile.exists())

        val seen = linkedSetOf<Pair<String, String>>()
        val duplicates = mutableListOf<Pair<String, String>>()

        csvFile.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readLine()
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val row = parseCsvLine(line)
                    val avar = preferredValue(row, 15, 0)
                    val russian = preferredValue(row, 16, 1)
                    if (avar.isBlank() && russian.isBlank()) {
                        return@forEach
                    }

                    val pair = avar to russian
                    if (!seen.add(pair)) {
                        duplicates += pair
                    }
                }
        }

        assertTrue(
            "Expected unique Avar/Russian base rows, found duplicates: $duplicates",
            duplicates.isEmpty()
        )
    }

    private fun preferredValue(row: List<String>, correctedIndex: Int, originalIndex: Int): String {
        val corrected = row.getOrNull(correctedIndex).orEmpty().trim()
        if (corrected.isNotBlank()) {
            return corrected
        }
        return row.getOrNull(originalIndex).orEmpty().trim()
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index += 1
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    values += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index += 1
        }

        values += current.toString()
        return values
    }
}
