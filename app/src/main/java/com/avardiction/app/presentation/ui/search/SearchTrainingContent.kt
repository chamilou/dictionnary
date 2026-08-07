package com.avardiction.app.presentation.ui.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avardiction.app.R
import com.avardiction.app.domain.model.AppLanguage
import com.avardiction.app.domain.model.DictionaryEntryResult
import com.avardiction.app.domain.model.EntryTranslation
import com.avardiction.app.presentation.ui.appLanguageDisplayName
import com.avardiction.app.presentation.viewmodel.DictionaryUiState
import com.avardiction.app.presentation.viewmodel.TrainingWordSource

private val TrainingCardMaxWidth = 680.dp
private val TrainingCardCompactMaxWidth = 520.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TrainingContent(
    uiState: DictionaryUiState,
    condensed: Boolean,
    onTrainingSourceSelected: (TrainingWordSource) -> Unit,
    onTrainingPromptChange: (String) -> Unit,
    onTrainingEntrySelected: (DictionaryEntryResult) -> Unit,
    onRandomWordClick: () -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onRevealAnswer: () -> Unit,
    onHideAnswer: () -> Unit
) {
    val selectedEntry = uiState.selectedTrainingEntry

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = trainingCardColor(),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.training_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.training_body,
                        currentDirectionLabel(
                            sourceLanguageCode = uiState.sourceLanguageCode,
                            targetLanguageCode = uiState.targetLanguageCode
                        )
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    lineHeight = 22.sp
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = uiState.trainingWordSource == TrainingWordSource.CHOSEN,
                        onClick = { onTrainingSourceSelected(TrainingWordSource.CHOSEN) },
                        label = { Text(stringResource(R.string.training_mode_chosen)) }
                    )
                    FilterChip(
                        selected = uiState.trainingWordSource == TrainingWordSource.RANDOM,
                        onClick = { onTrainingSourceSelected(TrainingWordSource.RANDOM) },
                        label = { Text(stringResource(R.string.training_mode_random)) }
                    )
                }
            }
        }

        if (uiState.trainingWordSource == TrainingWordSource.CHOSEN) {
            Surface(
                color = trainingCardColor(),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.trainingPrompt,
                        onValueChange = onTrainingPromptChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.training_pick_word_label)) },
                        placeholder = {
                            Text(
                                stringResource(
                                    R.string.training_pick_word_placeholder,
                                    appLanguageDisplayName(uiState.sourceLanguageCode)
                                )
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )

                    if (uiState.trainingSuggestions.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.training_matches),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.trainingSuggestions.forEach { entry ->
                                TrainingSuggestionItem(
                                    entry = entry,
                                    sourceLanguageCode = uiState.sourceLanguageCode,
                                    targetLanguageCode = uiState.targetLanguageCode,
                                    onClick = { onTrainingEntrySelected(entry) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            FilledTonalButton(
                onClick = onRandomWordClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isTrainingLoading
            ) {
                Text(
                    text = stringResource(
                        if (selectedEntry == null) R.string.training_random_word
                        else R.string.training_next_random_word
                    )
                )
            }
        }

        when {
            uiState.isTrainingLoading -> TrainingLoadingState()
            selectedEntry != null -> {
                TrainingFlashcard(
                    entry = selectedEntry,
                    condensed = condensed,
                    sourceLanguageCode = uiState.sourceLanguageCode,
                    targetLanguageCode = uiState.targetLanguageCode,
                    isAnswerVisible = uiState.isTrainingAnswerVisible,
                    onFavoriteClick = onFavoriteClick,
                    onClick = {
                        if (uiState.isTrainingAnswerVisible) onHideAnswer() else onRevealAnswer()
                    }
                )
            }
            uiState.trainingWordSource == TrainingWordSource.CHOSEN -> {
                MessageCard(
                    title = stringResource(R.string.training_choose_title),
                    body = stringResource(
                        R.string.training_choose_body,
                        appLanguageDisplayName(uiState.sourceLanguageCode)
                    )
                )
            }
            else -> {
                MessageCard(
                    title = stringResource(R.string.training_random_title),
                    body = stringResource(
                        R.string.training_random_body,
                        currentDirectionLabel(
                            sourceLanguageCode = uiState.sourceLanguageCode,
                            targetLanguageCode = uiState.targetLanguageCode
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun TrainingLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun TrainingSuggestionItem(
    entry: DictionaryEntryResult,
    sourceLanguageCode: String,
    targetLanguageCode: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = trainingCardColor(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = trainingTextsForLanguage(entry, sourceLanguageCode).firstOrNull()
                    ?: stringResource(R.string.no_avar_form),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = trainingAnswerLines(entry, sourceLanguageCode, targetLanguageCode)
                    .joinToString(" • ")
                    .ifBlank { stringResource(R.string.training_no_answer) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrainingFlashcard(
    entry: DictionaryEntryResult,
    condensed: Boolean,
    sourceLanguageCode: String,
    targetLanguageCode: String,
    isAnswerVisible: Boolean,
    onFavoriteClick: (Long) -> Unit,
    onClick: () -> Unit
) {
    val cardRotationY by animateFloatAsState(
        targetValue = if (isAnswerVisible) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "trainingCardFlip"
    )
    val hiddenCardColor = trainingCardColor()
    val cardFaceColor by animateColorAsState(
        targetValue = if (isAnswerVisible) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            hiddenCardColor
        },
        animationSpec = tween(durationMillis = 420),
        label = "trainingCardFaceColor"
    )
    val density = LocalDensity.current
    val sourceWord = trainingTextsForLanguage(entry, sourceLanguageCode).firstOrNull()
        ?: stringResource(R.string.no_avar_form)
    val answers = trainingAnswerLines(entry, sourceLanguageCode, targetLanguageCode)

    Surface(
        onClick = onClick,
        color = cardFaceColor,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
        modifier = Modifier
            .widthIn(max = if (condensed) TrainingCardCompactMaxWidth else TrainingCardMaxWidth)
            .fillMaxWidth()
            .heightIn(min = if (condensed) 220.dp else 280.dp)
            .graphicsLayer {
                this.rotationY = cardRotationY
                cameraDistance = 18f * density.density
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            IconButton(
                onClick = { onFavoriteClick(entry.entryId) },
                modifier = Modifier.align(Alignment.TopEnd)
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
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            if (cardRotationY <= 90f) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.training_card_front_label,
                            appLanguageDisplayName(sourceLanguageCode)
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    )
                    Text(
                        text = sourceWord,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { this.rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.training_card_back_label,
                            appLanguageDisplayName(targetLanguageCode)
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    )
                    answers.take(4).forEach { answer ->
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (answers.isEmpty()) {
                        Text(
                            text = stringResource(R.string.training_no_answer),
                            style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun trainingTextsForLanguage(
    entry: DictionaryEntryResult,
    languageCode: String
): List<String> {
    val texts = buildList {
        entry.avarText
            ?.takeIf { languageCode == AppLanguage.AV.code && it.isNotBlank() }
            ?.let(::add)
        addAll(
            entry.translations
                .filter { it.languageCode == languageCode }
                .map { it.text }
        )
    }

    return texts
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
}

private fun trainingAnswerTranslations(
    entry: DictionaryEntryResult,
    sourceLanguageCode: String,
    targetLanguageCode: String
): List<EntryTranslation> {
    val nonSourceTranslations = entry.translations.filter { it.languageCode != sourceLanguageCode }
    val exactTarget = nonSourceTranslations.filter { it.languageCode == targetLanguageCode }

    return when {
        targetLanguageCode == AppLanguage.ALL.code -> nonSourceTranslations
        exactTarget.isNotEmpty() -> exactTarget
        else -> nonSourceTranslations
    }
}

@Composable
private fun trainingAnswerLines(
    entry: DictionaryEntryResult,
    sourceLanguageCode: String,
    targetLanguageCode: String
): List<String> {
    return trainingAnswerTranslations(entry, sourceLanguageCode, targetLanguageCode)
        .map { translation ->
            val label = AppLanguage.fromCode(translation.languageCode).shortLabel
            if (targetLanguageCode == AppLanguage.ALL.code) {
                stringResource(R.string.language_value_format, label, translation.text)
            } else {
                translation.text
            }
        }
        .distinct()
}

@Composable
private fun trainingCardColor() = if (trainingUsesDarkSurfaces()) {
    MaterialTheme.colorScheme.surfaceVariant
} else {
    MaterialTheme.colorScheme.surface
}

@Composable
private fun trainingUsesDarkSurfaces(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}
