package com.avardiction.app.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avardiction.app.R
import com.avardiction.app.domain.model.AppLanguage
import com.avardiction.app.presentation.ui.appLanguageDisplayName

@Composable
fun LanguageSelector(
    languages: List<AppLanguage>,
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        state = rememberLazyListState(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        items(languages, key = { it.code }) { language ->
            FilterChip(
                selected = selectedLanguageCode == language.code,
                onClick = { onLanguageSelected(language.code) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                label = {
                    Text(
                        text = stringResource(
                            R.string.language_selector_label_format,
                            language.shortLabel,
                            appLanguageDisplayName(language.code)
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            )
        }
    }
}
