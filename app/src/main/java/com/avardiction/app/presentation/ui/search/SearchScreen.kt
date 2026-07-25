package com.avardiction.app.presentation.ui.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avardiction.app.R
import com.avardiction.app.data.local.SearchNormalizer
import com.avardiction.app.domain.model.AppLanguage
import com.avardiction.app.domain.model.DictionaryEntryResult
import com.avardiction.app.domain.model.EntryTranslation
import com.avardiction.app.domain.model.RecentSearch
import com.avardiction.app.presentation.ui.AppThemeMode
import com.avardiction.app.presentation.ui.appLanguageDisplayName
import com.avardiction.app.presentation.ui.components.WordItem
import com.avardiction.app.presentation.ui.details.EntryDetailScreen
import com.avardiction.app.presentation.viewmodel.DictionaryUiState
import com.avardiction.app.presentation.viewmodel.DatabaseBuildStage
import com.avardiction.app.presentation.viewmodel.DictionaryViewModel
import com.avardiction.app.presentation.viewmodel.DirectionWordCount
import com.avardiction.app.presentation.viewmodel.TrainingWordSource

private const val BENCHMARK_SEARCH_INPUT_TAG = "search_input"
private const val BENCHMARK_SEARCH_RESULTS_TAG = "search_results"
private const val BENCHMARK_SEARCH_LOADING_TAG = "search_loading"
private const val BENCHMARK_SEARCH_EMPTY_TAG = "search_empty"
private const val BENCHMARK_DATABASE_BUILD_TAG = "database_build_loading"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    viewModel: DictionaryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    var directionMenuExpanded by remember { mutableStateOf(false) }
    var actionsMenuExpanded by remember { mutableStateOf(false) }
    var settingsSheetExpanded by remember { mutableStateOf(false) }
    var uiLanguageDialogExpanded by remember { mutableStateOf(false) }
    var themeDialogExpanded by remember { mutableStateOf(false) }
    var aboutDialogExpanded by remember { mutableStateOf(false) }
    var privacyDialogExpanded by remember { mutableStateOf(false) }
    var supportDialogExpanded by remember { mutableStateOf(false) }
    var referencesDialogExpanded by remember { mutableStateOf(false) }
    var coverageDialogExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(SearchTab.Search) }
    val selectedEntry = selectedEntry(uiState)

    if (selectedEntry != null) {
        EntryDetailScreen(
            entry = selectedEntry,
            targetLanguageCode = uiState.targetLanguageCode,
            directionLabel = currentDirectionLabel(
                sourceLanguageCode = uiState.sourceLanguageCode,
                targetLanguageCode = uiState.targetLanguageCode
            ),
            onBack = viewModel::closeEntry,
            onFavoriteClick = viewModel::toggleFavorite,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            ModernTopBar(
                directionLabel = currentDirectionLabel(
                    sourceLanguageCode = uiState.sourceLanguageCode,
                    targetLanguageCode = uiState.targetLanguageCode
                ),
                onDirectionClick = { directionMenuExpanded = true },
                onMenuClick = { actionsMenuExpanded = true },
                actionsMenuExpanded = actionsMenuExpanded,
                showAllTranslations = uiState.showAllTranslations,
                onMenuDismiss = { actionsMenuExpanded = false },
                onShowAllTranslationsToggle = {
                    actionsMenuExpanded = false
                    viewModel.setShowAllTranslations(!uiState.showAllTranslations)
                },
                onSettingsClick = {
                    actionsMenuExpanded = false
                    viewModel.loadSettingsInfo(forceReload = true)
                    settingsSheetExpanded = true
                }
            )
        },
        bottomBar = {
            ModernBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                favoriteCount = uiState.favorites.size
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themedScreenBackgroundBrush())
                .semantics { testTagsAsResourceId = true }
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LanguageDirectionMenu(
                    expanded = directionMenuExpanded,
                    currentSourceLanguageCode = uiState.sourceLanguageCode,
                    onDismiss = { directionMenuExpanded = false },
                    onDirectionSelected = { sourceLanguageCode, targetLanguageCode ->
                        directionMenuExpanded = false
                        viewModel.updateLanguageDirection(sourceLanguageCode, targetLanguageCode)
                    }
                )

                if (settingsSheetExpanded) {
                    SettingsSheet(
                        themeMode = uiState.themeMode,
                        uiLanguageLabel = uiLanguageDisplayName(uiState.uiLanguageCode),
                        totalEntryCount = uiState.totalEntryCount,
                        isLoading = uiState.isSettingsInfoLoading,
                        onDismiss = { settingsSheetExpanded = false },
                        onThemeClick = { themeDialogExpanded = true },
                        onUiLanguageClick = { uiLanguageDialogExpanded = true },
                        onAboutClick = { aboutDialogExpanded = true },
                        onPrivacyClick = { privacyDialogExpanded = true },
                        onSupportClick = { supportDialogExpanded = true },
                        onReferencesClick = { referencesDialogExpanded = true },
                        onCoverageClick = {
                            viewModel.loadSettingsInfo(forceReload = true)
                            coverageDialogExpanded = true
                        }
                    )
                }

                if (uiLanguageDialogExpanded) {
                    UiLanguageDialog(
                        selectedLanguageCode = uiState.uiLanguageCode,
                        onDismiss = { uiLanguageDialogExpanded = false },
                        onLanguageSelected = { languageCode ->
                            uiLanguageDialogExpanded = false
                            viewModel.updateUiLanguage(languageCode)
                            (context as? android.app.Activity)?.recreate()
                        }
                    )
                }

                if (themeDialogExpanded) {
                    ThemeModeDialog(
                        selectedThemeMode = uiState.themeMode,
                        onDismiss = { themeDialogExpanded = false },
                        onThemeSelected = {
                            themeDialogExpanded = false
                            viewModel.updateThemeMode(it)
                        }
                    )
                }

                if (aboutDialogExpanded) {
                    AboutAppDialog(
                        totalEntryCount = uiState.totalEntryCount,
                        onDismiss = { aboutDialogExpanded = false }
                    )
                }

                if (privacyDialogExpanded) {
                    PrivacyDialog(onDismiss = { privacyDialogExpanded = false })
                }

                if (supportDialogExpanded) {
                    SupportDialog(onDismiss = { supportDialogExpanded = false })
                }

                if (referencesDialogExpanded) {
                    ReferencesDialog(onDismiss = { referencesDialogExpanded = false })
                }

                if (coverageDialogExpanded) {
                    DirectionCoverageDialog(
                        totalEntryCount = uiState.totalEntryCount,
                        directionWordCounts = uiState.directionWordCounts,
                        isLoading = uiState.isSettingsInfoLoading,
                        onDismiss = { coverageDialogExpanded = false }
                    )
                }

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)) togetherWith
                                fadeOut(animationSpec = tween(90))
                    },
                    label = "tabContent"
                ) { targetTab ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (targetTab) {
                                SearchTab.Search -> {
                                    SearchCard(
                                        query = uiState.query,
                                        onQueryChange = viewModel::updateQuery
                                    )
                                    SearchResultsContent(
                                    uiState = uiState,
                                    onEntryClick = viewModel::openEntry,
                                    onFavoriteClick = viewModel::toggleFavorite,
                                    onLoadMore = viewModel::loadMoreSearchResults,
                                    onBrowseLetterSelected = viewModel::selectBrowseLetter,
                                    onBrowseBack = viewModel::clearBrowseLetter
                                )
                            }
                            SearchTab.Favorites -> FavoritesContent(
                                favorites = uiState.favorites,
                                onEntryClick = viewModel::openEntry,
                                onFavoriteClick = viewModel::toggleFavorite
                            )
                            SearchTab.Recent -> RecentContent(
                                recentSearches = uiState.recentSearches,
                                onRecentSelected = {
                                    viewModel.applyRecentSearch(it)
                                    selectedTab = SearchTab.Search
                                }
                            )
                            SearchTab.Training -> TrainingContent(
                                uiState = uiState,
                                onTrainingSourceSelected = viewModel::setTrainingWordSource,
                                onTrainingPromptChange = viewModel::updateTrainingPrompt,
                                onTrainingEntrySelected = viewModel::selectTrainingEntry,
                                onRandomWordClick = viewModel::loadRandomTrainingEntry,
                                onFavoriteClick = viewModel::toggleFavorite,
                                onRevealAnswer = viewModel::revealTrainingAnswer,
                                onHideAnswer = viewModel::hideTrainingAnswer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernTopBar(
    directionLabel: String,
    onDirectionClick: () -> Unit,
    onMenuClick: () -> Unit,
    actionsMenuExpanded: Boolean,
    showAllTranslations: Boolean,
    onMenuDismiss: () -> Unit,
    onShowAllTranslationsToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DirectionButton(
            label = directionLabel,
            onClick = onDirectionClick
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_actions),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                MoreMenu(
                    expanded = actionsMenuExpanded,
                    showAllTranslations = showAllTranslations,
                    onDismiss = onMenuDismiss,
                    onShowAllTranslationsToggle = onShowAllTranslationsToggle,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun ModernBottomNavigation(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    favoriteCount: Int
) {
    val useDarkSurfaces = usesDarkSurfaces()
    NavigationBar(
        containerColor = themedCardColor(),
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        SearchTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                label = { Text(stringResource(tab.labelRes), style = MaterialTheme.typography.labelMedium) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == SearchTab.Favorites && favoriteCount > 0) {
                                Badge { Text(favoriteCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = stringResource(tab.labelRes)
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (useDarkSurfaces) 0.82f else 0.6f
                    ),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (useDarkSurfaces) 0.82f else 0.6f
                    ),
                    indicatorColor = if (useDarkSurfaces) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    }
                )
            )
        }
    }
}

private enum class SearchTab(val labelRes: Int, val iconRes: Int) {
    Search(R.string.tab_search, R.drawable.ic_tune),
    Favorites(R.string.tab_favorites, R.drawable.ic_favorite_filled),
    Recent(R.string.tab_recent, R.drawable.ic_swap_horiz),
    Training(R.string.tab_training, R.drawable.ic_mic)
}

private data class LanguageDirectionOption(
    val sourceLanguageCode: String,
    val targetLanguageCode: String
)

@Composable
private fun DirectionButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = themedCardColor(),
        shape = CircleShape,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(R.drawable.ic_swap_horiz),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LanguageDirectionMenu(
    expanded: Boolean,
    currentSourceLanguageCode: String,
    onDismiss: () -> Unit,
    onDirectionSelected: (String, String) -> Unit
) {
    val directions = listOf(
        LanguageDirectionOption(AppLanguage.AV.code, AppLanguage.EN.code),
        LanguageDirectionOption(AppLanguage.EN.code, AppLanguage.AV.code),
        LanguageDirectionOption(AppLanguage.AV.code, AppLanguage.RU.code),
        LanguageDirectionOption(AppLanguage.RU.code, AppLanguage.AV.code),
        LanguageDirectionOption(currentSourceLanguageCode, AppLanguage.ALL.code)
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        directions.forEach { direction ->
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (direction.targetLanguageCode == AppLanguage.ALL.code) {
                                stringResource(R.string.show_all_translations)
                            } else {
                                stringResource(
                                    R.string.direction_full_format,
                                    appLanguageDisplayName(direction.sourceLanguageCode),
                                    appLanguageDisplayName(direction.targetLanguageCode)
                                )
                            }
                        )
                    }
                },
                onClick = {
                    onDirectionSelected(direction.sourceLanguageCode, direction.targetLanguageCode)
                }
            )
        }
    }
}

@Composable
private fun MoreMenu(
    expanded: Boolean,
    showAllTranslations: Boolean,
    onDismiss: () -> Unit,
    onShowAllTranslationsToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    if (showAllTranslations) {
                        stringResource(R.string.hide_extra_translations)
                    } else {
                        stringResource(R.string.show_all_translations_action)
                    }
                )
            },
            onClick = onShowAllTranslationsToggle
        )
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.settings))
            },
            onClick = onSettingsClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    themeMode: AppThemeMode,
    uiLanguageLabel: String,
    totalEntryCount: Int,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onThemeClick: () -> Unit,
    onUiLanguageClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onSupportClick: () -> Unit,
    onReferencesClick: () -> Unit,
    onCoverageClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            SettingsActionRow(
                title = stringResource(R.string.settings_theme),
                subtitle = themeModeDisplayName(themeMode),
                onClick = onThemeClick
            )
            SettingsActionRow(
                title = stringResource(R.string.settings_ui_language),
                subtitle = uiLanguageLabel,
                onClick = onUiLanguageClick
            )
            SettingsActionRow(
                title = stringResource(R.string.settings_about),
                subtitle = stringResource(R.string.settings_about_summary),
                onClick = onAboutClick
            )
            SettingsActionRow(
                title = stringResource(R.string.settings_privacy),
                subtitle = stringResource(R.string.settings_privacy_summary),
                onClick = onPrivacyClick
            )
            SettingsActionRow(
                title = stringResource(R.string.settings_support),
                subtitle = stringResource(R.string.settings_support_summary),
                onClick = onSupportClick
            )
            SettingsActionRow(
                title = stringResource(R.string.settings_references),
                subtitle = stringResource(R.string.settings_references_summary),
                onClick = onReferencesClick
            )
            SettingsActionRow(
                title = stringResource(R.string.settings_word_counts),
                subtitle = if (isLoading) {
                    stringResource(R.string.settings_loading)
                } else {
                    stringResource(
                        R.string.settings_word_counts_summary_format,
                        totalEntryCount
                    )
                },
                onClick = onCoverageClick
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = themedSettingsRowColor(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ThemeModeDialog(
    selectedThemeMode: AppThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit
) {
    val options = listOf(AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { themeMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onThemeSelected(themeMode) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedThemeMode == themeMode,
                            onClick = { onThemeSelected(themeMode) }
                        )
                        Text(
                            text = themeModeDisplayName(themeMode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun AboutAppDialog(
    totalEntryCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "?"
    val versionCode = packageInfo.longVersionCode

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_app_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.about_app_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.about_app_version_format,
                        versionName,
                        versionCode
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.about_app_entries_format, totalEntryCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.about_app_dataset_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.privacy_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.privacy_body_storage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.privacy_body_network),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.privacy_body_support),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun SupportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val supportEmail = stringResource(R.string.support_email_address)

    fun openSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.support_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.support_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = ::openSupportEmail,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = supportEmail,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.support_status_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.support_release_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun ReferencesDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.references_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.references_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.references_avar_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.references_english_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.references_english_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun DirectionCoverageDialog(
    totalEntryCount: Int,
    directionWordCounts: List<DirectionWordCount>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.direction_coverage_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.direction_coverage_total_entries_format, totalEntryCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isLoading) {
                    Text(
                        text = stringResource(R.string.settings_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    directionWordCounts.forEach { count ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(
                                    R.string.direction_count_format,
                                    stringResource(
                                        R.string.direction_full_format,
                                        appLanguageDisplayName(count.sourceLanguageCode),
                                        appLanguageDisplayName(count.targetLanguageCode)
                                    ),
                                    count.count
                                ),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun UiLanguageDialog(
    selectedLanguageCode: String?,
    onDismiss: () -> Unit,
    onLanguageSelected: (String?) -> Unit
) {
    val options = listOf<String?>(
        null,
        AppLanguage.EN.code,
        AppLanguage.RU.code,
        AppLanguage.AV.code
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ui_language_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { languageCode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onLanguageSelected(languageCode) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedLanguageCode == languageCode,
                            onClick = { onLanguageSelected(languageCode) }
                        )
                        Text(
                            text = uiLanguageDisplayName(languageCode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        }
    )
}

@Composable
private fun MessageCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun SearchCard(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        color = themedCardColor(),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(BENCHMARK_SEARCH_INPUT_TAG)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_tune),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.clear_query),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SearchResultsContent(
    uiState: DictionaryUiState,
    onEntryClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onBrowseLetterSelected: (String) -> Unit,
    onBrowseBack: () -> Unit
) {
    if (uiState.databaseBuildStage != null) {
        DatabaseBuildLoadingState(
            stage = uiState.databaseBuildStage,
            processed = uiState.databaseBuildProcessed,
            total = uiState.databaseBuildTotal
        )
        return
    }

    if (uiState.isLoading) {
        LoadingState(modifier = Modifier.testTag(BENCHMARK_SEARCH_LOADING_TAG))
        return
    }

    if (uiState.query.isBlank()) {
        AlphabetBrowseContent(
            uiState = uiState,
            onEntryClick = onEntryClick,
            onFavoriteClick = onFavoriteClick,
            onBrowseLetterSelected = onBrowseLetterSelected,
            onBrowseBack = onBrowseBack
        )
        return
    }

    if (uiState.searchResults.isEmpty()) {
        MessageCard(
            title = stringResource(R.string.no_entries_found_title),
            body = stringResource(R.string.no_entries_found_body),
            modifier = Modifier.testTag(BENCHMARK_SEARCH_EMPTY_TAG)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(BENCHMARK_SEARCH_RESULTS_TAG),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = uiState.searchResults,
            key = { it.entryId }
        ) { entry ->
            WordItem(
                entry = entry,
                onClick = onEntryClick,
                onFavoriteClick = onFavoriteClick
            )
        }

        if (uiState.canLoadMoreSearchResults || uiState.isLoadingMoreResults) {
            item {
                LoadMoreResultsCard(
                    shownCount = uiState.searchResults.size,
                    isLoading = uiState.isLoadingMoreResults,
                    onClick = onLoadMore
                )
            }
        }
    }
}

private data class BrowseAlphabetLetter(
    val value: String,
    val enabled: Boolean
)

@Composable
private fun AlphabetBrowseContent(
    uiState: DictionaryUiState,
    onEntryClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onBrowseLetterSelected: (String) -> Unit,
    onBrowseBack: () -> Unit
) {
    val selectedLetter = uiState.selectedBrowseLetter

    if (selectedLetter == null) {
        AlphabetChooserContent(
            sourceLanguageCode = uiState.sourceLanguageCode,
            availableLetters = uiState.browseAvailableLetters,
            onLetterSelected = onBrowseLetterSelected
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BrowseResultsHeader(
            letter = selectedLetter,
            onBack = onBrowseBack
        )

        when {
            uiState.isBrowseLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                LoadingState()
            }
            uiState.browseEntries.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                MessageCard(
                    title = stringResource(R.string.alphabet_browse_empty_title),
                    body = stringResource(R.string.alphabet_browse_empty_body)
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = uiState.browseEntries,
                    key = { it.entryId }
                ) { entry ->
                    BrowseWordItem(
                        entry = entry,
                        sourceLanguageCode = uiState.sourceLanguageCode,
                        targetLanguageCode = uiState.targetLanguageCode,
                        showAllTranslations = uiState.showAllTranslations,
                        onClick = onEntryClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AlphabetChooserContent(
    sourceLanguageCode: String,
    availableLetters: List<String>,
    onLetterSelected: (String) -> Unit
) {
    val letters = browseAlphabetLetters(
        sourceLanguageCode = sourceLanguageCode,
        availableLetters = availableLetters
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                color = themedCardColor(),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.alphabet_browse_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.alphabet_browse_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        items(
            count = letters.size,
            key = { index -> letters[index].value }
        ) { index ->
            val letter = letters[index]
            OutlinedButton(
                onClick = { onLetterSelected(letter.value) },
                enabled = letter.enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = displayBrowseLetter(letter.value),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun BrowseResultsHeader(
    letter: String,
    onBack: () -> Unit
) {
    Surface(
        color = themedCardColor(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.back))
            }
            Text(
                text = stringResource(
                    R.string.alphabet_browse_selected_letter_format,
                    displayBrowseLetter(letter)
                ),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BrowseWordItem(
    entry: DictionaryEntryResult,
    sourceLanguageCode: String,
    targetLanguageCode: String,
    showAllTranslations: Boolean,
    onClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    WordItem(
        entry = entry,
        onClick = onClick,
        onFavoriteClick = onFavoriteClick,
        headlineText = browseHeadlineText(entry, sourceLanguageCode),
        previewText = browsePreviewText(
            entry = entry,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            showAllTranslations = showAllTranslations
        )
    )
}

@Composable
private fun FavoritesContent(
    favorites: List<DictionaryEntryResult>,
    onEntryClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    if (favorites.isEmpty()) {
        MessageCard(
            title = stringResource(R.string.no_favorites_title),
            body = stringResource(R.string.no_favorites_body)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = favorites,
            key = { it.entryId }
        ) { entry ->
            WordItem(
                entry = entry,
                onClick = onEntryClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun RecentContent(
    recentSearches: List<RecentSearch>,
    onRecentSelected: (RecentSearch) -> Unit
) {
    if (recentSearches.isEmpty()) {
        MessageCard(
            title = stringResource(R.string.no_recent_searches_title),
            body = stringResource(R.string.no_recent_searches_body)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = recentSearches,
            key = { it.id }
        ) { recentSearch ->
            Surface(
                onClick = { onRecentSelected(recentSearch) },
                color = themedCardColor(),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = recentSearch.query.ifBlank { stringResource(R.string.recent_open_direction) },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentDirectionLabel(
                            sourceLanguageCode = recentSearch.sourceLanguageCode,
                            targetLanguageCode = recentSearch.targetLanguageCode
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingContent(
    uiState: DictionaryUiState,
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
            color = themedCardColor(),
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
                Row(
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
                color = themedCardColor(),
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
        color = themedCardColor(),
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
    val hiddenCardColor = themedCardColor()
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
            .fillMaxWidth()
            .height(280.dp)
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
private fun EmptySearchState(
    onQuickDirectionSelected: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        MessageCard(
            title = stringResource(R.string.empty_search_title),
            body = stringResource(R.string.empty_search_body)
        )

        Text(
            text = stringResource(R.string.quick_shortcuts),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickDirectionChip(
                stringResource(
                    R.string.direction_full_format,
                    appLanguageDisplayName(AppLanguage.AV.code),
                    appLanguageDisplayName(AppLanguage.EN.code)
                )
            ) {
                onQuickDirectionSelected(AppLanguage.AV.code, AppLanguage.EN.code)
            }
            QuickDirectionChip(
                stringResource(
                    R.string.direction_full_format,
                    appLanguageDisplayName(AppLanguage.EN.code),
                    appLanguageDisplayName(AppLanguage.AV.code)
                )
            ) {
                onQuickDirectionSelected(AppLanguage.EN.code, AppLanguage.AV.code)
            }
            QuickDirectionChip(
                stringResource(
                    R.string.direction_full_format,
                    appLanguageDisplayName(AppLanguage.AV.code),
                    appLanguageDisplayName(AppLanguage.RU.code)
                )
            ) {
                onQuickDirectionSelected(AppLanguage.AV.code, AppLanguage.RU.code)
            }
            QuickDirectionChip(
                stringResource(
                    R.string.direction_full_format,
                    appLanguageDisplayName(AppLanguage.RU.code),
                    appLanguageDisplayName(AppLanguage.AV.code)
                )
            ) {
                onQuickDirectionSelected(AppLanguage.RU.code, AppLanguage.AV.code)
            }
        }
    }
}

@Composable
private fun QuickDirectionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun DatabaseBuildLoadingState(
    stage: DatabaseBuildStage,
    processed: Int,
    total: Int
) {
    val progress = when (stage) {
        DatabaseBuildStage.PREPARING -> null
        DatabaseBuildStage.READING_ROWS -> null
        DatabaseBuildStage.INSERTING_ENTRIES ->
            if (total > 0) 0.45f + (processed.toFloat() / total.toFloat()) * 0.2f else null
        DatabaseBuildStage.INSERTING_TRANSLATIONS ->
            if (total > 0) 0.65f + (processed.toFloat() / total.toFloat()) * 0.3f else null
        DatabaseBuildStage.FINALIZING -> 0.98f
    }

    val stageText = when (stage) {
        DatabaseBuildStage.PREPARING -> stringResource(R.string.database_build_stage_preparing)
        DatabaseBuildStage.READING_ROWS -> stringResource(
            R.string.database_build_stage_reading_format,
            processed
        )
        DatabaseBuildStage.INSERTING_ENTRIES -> stringResource(
            R.string.database_build_stage_entries_format,
            processed,
            total
        )
        DatabaseBuildStage.INSERTING_TRANSLATIONS -> stringResource(
            R.string.database_build_stage_translations_format,
            processed,
            total
        )
        DatabaseBuildStage.FINALIZING -> stringResource(R.string.database_build_stage_finalizing)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            color = themedCardColor(),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(BENCHMARK_DATABASE_BUILD_TAG)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.database_build_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = stringResource(R.string.database_build_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    )
                }
                Text(
                    text = stageText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadMoreResultsCard(
    shownCount: Int,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = themedCardColor(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isLoading) {
                    stringResource(R.string.loading_more_entries)
                } else {
                    stringResource(R.string.more_matches_available)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.more_matches_shown_format, shownCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Button(onClick = onClick, shape = CircleShape) {
                    Text(stringResource(R.string.load_more_results))
                }
            }
        }
    }
}

private fun browseAlphabetLetters(
    sourceLanguageCode: String,
    availableLetters: List<String>
): List<BrowseAlphabetLetter> {
    val normalizedAvailableLetters = when (sourceLanguageCode) {
        AppLanguage.AV.code -> SearchNormalizer.sortBrowseLetters(
            letters = availableLetters,
            languageCode = sourceLanguageCode
        )
        else -> availableLetters
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    val orderedAlphabet = when (sourceLanguageCode) {
        AppLanguage.EN.code -> ('a'..'z').map(Char::toString)
        AppLanguage.RU.code -> listOf(
            "а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м",
            "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ",
            "ы", "ь", "э", "ю", "я"
        )
        AppLanguage.AV.code -> SearchNormalizer.avarBrowseLetters
        else -> null
    }

    if (orderedAlphabet == null) {
        return normalizedAvailableLetters
            .sortedBy { it.uppercase() }
            .map { BrowseAlphabetLetter(value = it, enabled = true) }
    }

    val extraLetters = if (sourceLanguageCode == AppLanguage.AV.code) {
        emptyList()
    } else {
        normalizedAvailableLetters.filterNot { it in orderedAlphabet }
    }

    return buildList {
        orderedAlphabet.forEach { letter ->
            add(BrowseAlphabetLetter(value = letter, enabled = letter in normalizedAvailableLetters))
        }
        extraLetters
            .sortedBy { it.uppercase() }
            .forEach { letter ->
                add(BrowseAlphabetLetter(value = letter, enabled = true))
            }
    }
}

private fun displayBrowseLetter(letter: String): String = letter.uppercase()

private fun browseHeadlineText(
    entry: DictionaryEntryResult,
    sourceLanguageCode: String
): String? {
    if (sourceLanguageCode == AppLanguage.AV.code) {
        return entry.avarText?.takeIf { it.isNotBlank() }
    }

    return entry.translations
        .firstOrNull { it.languageCode == sourceLanguageCode }
        ?.text
        ?.takeIf { it.isNotBlank() }
        ?: entry.avarText?.takeIf { it.isNotBlank() }
}

private fun browsePreviewText(
    entry: DictionaryEntryResult,
    sourceLanguageCode: String,
    targetLanguageCode: String,
    showAllTranslations: Boolean
): String {
    val pieces = mutableListOf<String>()
    val avarText = entry.avarText?.takeIf { it.isNotBlank() }

    if (sourceLanguageCode != AppLanguage.AV.code && avarText != null) {
        pieces += "${AppLanguage.AV.shortLabel}: $avarText"
    }

    val candidateTranslations = when {
        showAllTranslations || targetLanguageCode == AppLanguage.ALL.code -> entry.translations
            .filter { it.languageCode != sourceLanguageCode }
        else -> entry.translations.filter { it.languageCode == targetLanguageCode }
    }

    candidateTranslations.forEach { translation ->
        if (translation.languageCode == sourceLanguageCode) {
            return@forEach
        }
        if (
            translation.languageCode == AppLanguage.AV.code &&
            avarText != null &&
            translation.text == avarText &&
            sourceLanguageCode != AppLanguage.AV.code
        ) {
            return@forEach
        }

        pieces += "${AppLanguage.fromCode(translation.languageCode).shortLabel}: ${translation.text}"
    }

    return pieces
        .distinct()
        .take(3)
        .joinToString("\n")
}

@Composable
private fun themedScreenBackgroundBrush(): Brush {
    val colorScheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        colors = if (usesDarkSurfaces()) {
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
private fun themedCardColor(): Color {
    return if (usesDarkSurfaces()) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
}

@Composable
private fun themedSettingsRowColor(): Color {
    return if (usesDarkSurfaces()) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
}

@Composable
private fun usesDarkSurfaces(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}

@Composable
private fun currentDirectionLabel(
    sourceLanguageCode: String,
    targetLanguageCode: String
): String {
    val source = AppLanguage.fromCode(sourceLanguageCode).shortLabel
    val target = AppLanguage.fromCode(targetLanguageCode).shortLabel
    return stringResource(R.string.direction_short_format, source, target)
}

@Composable
private fun uiLanguageDisplayName(languageCode: String?): String {
    return if (languageCode == null) {
        stringResource(R.string.ui_language_system_default)
    } else {
        appLanguageDisplayName(languageCode)
    }
}

@Composable
private fun themeModeDisplayName(themeMode: AppThemeMode): String {
    return when (themeMode) {
        AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
        AppThemeMode.DARK -> stringResource(R.string.theme_dark)
    }
}

private fun selectedEntry(uiState: DictionaryUiState): DictionaryEntryResult? {
    val selectedEntryId = uiState.selectedEntryId ?: return null
    val visibleEntries = buildList {
        addAll(uiState.searchResults)
        addAll(uiState.browseEntries)
        addAll(uiState.favorites)
        addAll(uiState.trainingSuggestions)
        uiState.selectedTrainingEntry?.let(::add)
    }

    return visibleEntries
        .distinctBy { it.entryId }
        .firstOrNull { it.entryId == selectedEntryId }
}
