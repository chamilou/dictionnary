package com.avardiction.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avardiction.app.R
import com.avardiction.app.domain.model.AppLanguage
import com.avardiction.app.domain.model.DictionaryEntryResult
import com.avardiction.app.domain.model.EntryTranslation

@Composable
fun WordItem(
    entry: DictionaryEntryResult,
    onClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    headlineText: String? = null,
    previewText: String? = null,
    modifier: Modifier = Modifier
) {
    val useDarkSurfaces = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(entry.entryId) },
        color = if (useDarkSurfaces) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val resolvedHeadline = headlineText ?: entry.avarText ?: stringResource(R.string.no_avar_form)
                Text(
                    text = resolvedHeadline,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        letterSpacing = (-0.5).sp,
                        lineHeight = 28.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val preview = previewText ?: buildPreview(entry.translations)
                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (useDarkSurfaces) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        },
                        lineHeight = 22.sp
                    )
                }

            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onFavoriteClick(entry.entryId) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (entry.isFavorite) R.drawable.ic_favorite_filled
                            else R.drawable.ic_favorite_outline
                        ),
                        contentDescription = stringResource(
                            if (entry.isFavorite) R.string.remove_favorite else R.string.add_favorite
                        ),
                        tint = if (entry.isFavorite) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (useDarkSurfaces) 0.72f else 0.4f
                            )
                        },
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (!entry.notes.isNullOrBlank()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_outline),
                        contentDescription = stringResource(R.string.has_notes),
                        tint = if (useDarkSurfaces) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(20.dp).padding(end = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun buildPreview(translations: List<EntryTranslation>): String {
    val english = translations
        .firstOrNull { it.languageCode == AppLanguage.EN.code }
        ?.text
    val russian = translations
        .firstOrNull { it.languageCode == AppLanguage.RU.code }
        ?.text

    val pieces = mutableListOf<String>()
    if (!english.isNullOrBlank()) {
        pieces += stringResource(R.string.language_value_format, AppLanguage.EN.shortLabel, english)
    }
    if (!russian.isNullOrBlank()) {
        pieces += stringResource(R.string.language_value_format, AppLanguage.RU.shortLabel, russian)
    }

    if (pieces.isNotEmpty()) {
        return pieces.joinToString("\n")
    }

    val fallbackPieces = translations
        .filterNot { it.languageCode == AppLanguage.AV.code }
        .take(2)
        .map {
            stringResource(
                R.string.language_value_format,
                AppLanguage.fromCode(it.languageCode).shortLabel,
                it.text
            )
        }
    return fallbackPieces.joinToString("\n")
}
