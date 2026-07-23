package com.example.dictionnary.presentation.ui.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dictionnary.R
import com.example.dictionnary.domain.model.AppLanguage
import com.example.dictionnary.domain.model.DictionaryEntryResult
import com.example.dictionnary.domain.model.EntryTranslation
import com.example.dictionnary.presentation.ui.appLanguageDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entry: DictionaryEntryResult,
    targetLanguageCode: String,
    directionLabel: String,
    onBack: () -> Unit,
    onFavoriteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val avarTranslation = entry.translations.firstOrNull { it.languageCode == AppLanguage.AV.code }
    val targetTranslation = entry.translations.firstOrNull {
        it.languageCode == targetLanguageCode &&
            targetLanguageCode != AppLanguage.ALL.code &&
            targetLanguageCode != AppLanguage.AV.code
    }
    val russianTranslation = entry.translations.firstOrNull { it.languageCode == AppLanguage.RU.code }
    val secondaryTranslations = entry.translations.filterNot {
        it.languageCode == AppLanguage.AV.code ||
            it.languageCode == targetTranslation?.languageCode ||
            it.languageCode == russianTranslation?.languageCode
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.entry_details),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = directionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themedDetailBackgroundBrush())
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HeroCard(
                        entry = entry,
                        onFavoriteClick = onFavoriteClick
                    )
                }

                if (avarTranslation != null) {
                    item {
                        TranslationCard(
                            title = stringResource(R.string.avar_headword),
                            subtitle = stringResource(R.string.avar_headword_review_subtitle),
                            translation = avarTranslation
                        )
                    }
                }

                if (targetTranslation != null) {
                    item {
                        TranslationCard(
                            title = appLanguageDisplayName(targetTranslation.languageCode),
                            subtitle = stringResource(R.string.preferred_translation),
                            translation = targetTranslation
                        )
                    }
                }

                if (russianTranslation != null && russianTranslation.languageCode != targetTranslation?.languageCode) {
                    item {
                        TranslationCard(
                            title = stringResource(R.string.russian_bridge),
                            subtitle = stringResource(R.string.russian_bridge_subtitle),
                            translation = russianTranslation
                        )
                    }
                }

                if (secondaryTranslations.isNotEmpty()) {
                    item {
                        SectionCard(title = stringResource(R.string.other_visible_translations)) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                secondaryTranslations.forEach { translation ->
                                    TranslationLine(
                                        translation = translation
                                    )
                                }
                            }
                        }
                    }
                }

                if (!entry.notes.isNullOrBlank()) {
                    item {
                        SectionCard(title = stringResource(R.string.notes)) {
                            Text(
                                text = entry.notes,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                if (!entry.category.isNullOrBlank() || !entry.type.isNullOrBlank()) {
                    item {
                        SectionCard(title = stringResource(R.string.entry_metadata)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (!entry.category.isNullOrBlank()) {
                                    MetadataRow(label = stringResource(R.string.category), value = entry.category)
                                }
                                if (!entry.type.isNullOrBlank()) {
                                    MetadataRow(label = stringResource(R.string.type), value = entry.type)
                                }
                            }
                        }
                    }
                }

                if (!entry.sourceFile.isNullOrBlank() || !entry.sourcePage.isNullOrBlank()) {
                    item {
                        SectionCard(title = stringResource(R.string.source)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (!entry.sourceFile.isNullOrBlank()) {
                                    MetadataRow(label = stringResource(R.string.file), value = entry.sourceFile)
                                }
                                if (!entry.sourcePage.isNullOrBlank()) {
                                    MetadataRow(label = stringResource(R.string.page), value = entry.sourcePage)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    entry: DictionaryEntryResult,
    onFavoriteClick: (Long) -> Unit
) {
    Surface(
        color = themedDetailCardColor(),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.avarText ?: stringResource(R.string.no_avar_form),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 38.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.avar_headword),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { onFavoriteClick(entry.entryId) }) {
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
                                alpha = if (usesDarkDetailSurfaces()) 0.72f else 0.5f
                            )
                        }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip(
                    text = pluralStringResource(
                        R.plurals.translations_count,
                        entry.translations.size,
                        entry.translations.size
                    )
                )
                if (!entry.notes.isNullOrBlank()) {
                    DetailChip(text = stringResource(R.string.has_notes))
                }
                if (!entry.category.isNullOrBlank()) {
                    DetailChip(text = entry.category)
                }
            }
        }
    }
}

@Composable
private fun TranslationCard(
    title: String,
    subtitle: String,
    translation: EntryTranslation
) {
    SectionCard(title = title, subtitle = subtitle) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = translation.text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 30.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            TranslationMeta(translation = translation)
        }
    }
}

@Composable
private fun TranslationLine(
    translation: EntryTranslation
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppLanguage.fromCode(translation.languageCode).shortLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = translation.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        TranslationMeta(translation = translation)
    }
}

@Composable
private fun TranslationMeta(translation: EntryTranslation) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        translation.sourceLanguageCode?.takeIf { it.isNotBlank() }?.let {
            DetailChip(
                text = stringResource(
                    R.string.translation_from_format,
                    AppLanguage.fromCode(it).shortLabel
                )
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        color = themedDetailCardColor(),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.32f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.68f)
        )
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        color = if (usesDarkDetailSurfaces()) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        },
        shape = CircleShape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (usesDarkDetailSurfaces()) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun themedDetailBackgroundBrush(): Brush {
    val colorScheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        colors = if (usesDarkDetailSurfaces()) {
            listOf(
                colorScheme.background,
                colorScheme.primaryContainer.copy(alpha = 0.55f),
                colorScheme.surface
            )
        } else {
            listOf(
                colorScheme.background,
                colorScheme.tertiary.copy(alpha = 0.18f),
                colorScheme.surface
            )
        }
    )
}

@Composable
private fun themedDetailCardColor() = if (usesDarkDetailSurfaces()) {
    MaterialTheme.colorScheme.surfaceVariant
} else {
    MaterialTheme.colorScheme.surface
}

@Composable
private fun usesDarkDetailSurfaces(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}
