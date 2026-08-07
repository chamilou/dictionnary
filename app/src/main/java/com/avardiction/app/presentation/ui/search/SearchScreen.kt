package com.avardiction.app.presentation.ui.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.pm.PackageInfoCompat
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
import com.avardiction.app.presentation.viewmodel.DatabaseBuildStage
import com.avardiction.app.presentation.viewmodel.DictionaryUiState
import com.avardiction.app.presentation.viewmodel.DictionaryViewModel
import com.avardiction.app.presentation.viewmodel.LanguageCoverageSupport
import com.avardiction.app.presentation.viewmodel.LanguageWordCount
import com.avardiction.app.presentation.viewmodel.TrainingWordSource

private const val BENCHMARK_SEARCH_INPUT_TAG = "search_input"
private const val BENCHMARK_SEARCH_RESULTS_TAG = "search_results"
private const val BENCHMARK_SEARCH_LOADING_TAG = "search_loading"
private const val BENCHMARK_SEARCH_EMPTY_TAG = "search_empty"
private const val BENCHMARK_DATABASE_BUILD_TAG = "database_build_loading"
private val PrimaryContentMaxWidth = 840.dp
private val TrainingCardMaxWidth = 680.dp
private val ExpandedLayoutBreakpoint = 720.dp
private val ExpandedLayoutMinHeight = 600.dp
private val CompactHeightBreakpoint = 480.dp
private val ExpandedShellMaxWidth = 1560.dp
private val TabletListPaneMaxWidth = 520.dp
private val DialogMaxWidth = 560.dp
private val SheetContentMaxWidth = 560.dp
private val TopBarControlHeight = 48.dp
private val TopBarControlHeightCondensed = 48.dp
private val TrainingCardCompactMaxWidth = 520.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    viewModel: DictionaryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    var directionMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var actionsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var settingsSheetExpanded by rememberSaveable { mutableStateOf(false) }
    var uiLanguageDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var themeDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var aboutDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var privacyDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var supportDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var referencesDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var coverageDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var compactBottomBarVisible by rememberSaveable { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf(SearchTab.Search) }
    val selectedEntry = selectedEntry(uiState)
    val directionLabel = currentDirectionLabel(
        sourceLanguageCode = uiState.sourceLanguageCode,
        targetLanguageCode = uiState.targetLanguageCode
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scope = this
        val isShortHeightLayout = scope.maxHeight < CompactHeightBreakpoint
        val isExpandedLayout = scope.maxWidth >= ExpandedLayoutBreakpoint && scope.maxHeight >= ExpandedLayoutMinHeight

        LaunchedEffect(isShortHeightLayout, isExpandedLayout) {
            if (!isShortHeightLayout || isExpandedLayout) {
                compactBottomBarVisible = true
            }
        }

        if (!isExpandedLayout && selectedEntry != null) {
            EntryDetailScreen(
                entry = selectedEntry,
                targetLanguageCode = uiState.targetLanguageCode,
                directionLabel = directionLabel,
                onBack = viewModel::closeEntry,
                onFavoriteClick = viewModel::toggleFavorite,
                modifier = Modifier.fillMaxSize(),
                showDirectionSubtitle = !isShortHeightLayout
            )
            return@BoxWithConstraints
        }

        if (isExpandedLayout && selectedEntry != null) {
            BackHandler(onBack = viewModel::closeEntry)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    directionLabel = directionLabel,
                    query = uiState.query,
                    condensed = isShortHeightLayout,
                    onQueryChange = viewModel::updateQuery,
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
                if (!isExpandedLayout) {
                    AnimatedVisibility(
                        visible = compactBottomBarVisible,
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(180)
                        ) + fadeIn(animationSpec = tween(180)),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(140)
                        ) + fadeOut(animationSpec = tween(140))
                    ) {
                        ModernBottomNavigation(
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                                compactBottomBarVisible = true
                            },
                            favoriteCount = uiState.favorites.size,
                            condensed = isShortHeightLayout
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themedScreenBackgroundBrush())
                    .semantics { testTagsAsResourceId = true }
                    .padding(innerPadding)
            ) {
                SearchScreenOverlays(
                    SearchScreenOverlaysParams(
                        uiState = uiState,
                        context = context,
                        directionMenuExpanded = directionMenuExpanded,
                        onDirectionMenuDismiss = { directionMenuExpanded = false },
                        onDirectionSelected = { sourceLanguageCode, targetLanguageCode ->
                            directionMenuExpanded = false
                            viewModel.updateLanguageDirection(sourceLanguageCode, targetLanguageCode)
                        },
                        settingsSheetExpanded = settingsSheetExpanded,
                        onSettingsDismiss = { settingsSheetExpanded = false },
                        onThemeClick = { themeDialogExpanded = true },
                        onUiLanguageClick = { uiLanguageDialogExpanded = true },
                        onAboutClick = { aboutDialogExpanded = true },
                        onPrivacyClick = { privacyDialogExpanded = true },
                        onSupportClick = { supportDialogExpanded = true },
                        onReferencesClick = { referencesDialogExpanded = true },
                        onCoverageClick = {
                            viewModel.loadSettingsInfo(forceReload = true)
                            coverageDialogExpanded = true
                        },
                        uiLanguageDialogExpanded = uiLanguageDialogExpanded,
                        onUiLanguageDismiss = { uiLanguageDialogExpanded = false },
                        onUiLanguageSelected = { languageCode ->
                            uiLanguageDialogExpanded = false
                            viewModel.updateUiLanguage(languageCode)
                            (context as? android.app.Activity)?.recreate()
                        },
                        themeDialogExpanded = themeDialogExpanded,
                        onThemeDialogDismiss = { themeDialogExpanded = false },
                        onThemeSelected = {
                            themeDialogExpanded = false
                            viewModel.updateThemeMode(it)
                        },
                        aboutDialogExpanded = aboutDialogExpanded,
                        onAboutDismiss = { aboutDialogExpanded = false },
                        privacyDialogExpanded = privacyDialogExpanded,
                        onPrivacyDismiss = { privacyDialogExpanded = false },
                        supportDialogExpanded = supportDialogExpanded,
                        onSupportDismiss = { supportDialogExpanded = false },
                        referencesDialogExpanded = referencesDialogExpanded,
                        onReferencesDismiss = { referencesDialogExpanded = false },
                        coverageDialogExpanded = coverageDialogExpanded,
                        onCoverageDismiss = { coverageDialogExpanded = false }
                    )
                )

                if (isExpandedLayout) {
                    ExpandedSearchLayout(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        favoriteCount = uiState.favorites.size,
                        uiState = uiState,
                        selectedEntry = selectedEntry,
                        directionLabel = directionLabel,
                        onEntryClick = viewModel::openEntry,
                        onFavoriteClick = viewModel::toggleFavorite,
                        onLoadMore = viewModel::loadMoreSearchResults,
                        onBrowseLetterSelected = viewModel::selectBrowseLetter,
                        onBrowseBack = viewModel::clearBrowseLetter,
                        onQueryChange = viewModel::updateQuery,
                        onQuickDirectionSelected = viewModel::updateLanguageDirection,
                        onRecentSelected = {
                            viewModel.applyRecentSearch(it)
                            selectedTab = SearchTab.Search
                        },
                        onTrainingSourceSelected = viewModel::setTrainingWordSource,
                        onTrainingPromptChange = viewModel::updateTrainingPrompt,
                        onTrainingEntrySelected = viewModel::selectTrainingEntry,
                        onRandomWordClick = viewModel::loadRandomTrainingEntry,
                        onRevealAnswer = viewModel::revealTrainingAnswer,
                        onHideAnswer = viewModel::hideTrainingAnswer,
                        onCloseEntry = viewModel::closeEntry
                    )
                } else {
                    CompactSearchLayout(
                        selectedTab = selectedTab,
                        uiState = uiState,
                        condensed = isShortHeightLayout,
                        onBottomBarVisibilityChange = { compactBottomBarVisible = it || !isShortHeightLayout },
                        onEntryClick = viewModel::openEntry,
                        onFavoriteClick = viewModel::toggleFavorite,
                        onLoadMore = viewModel::loadMoreSearchResults,
                        onBrowseLetterSelected = viewModel::selectBrowseLetter,
                        onBrowseBack = viewModel::clearBrowseLetter,
                        onQueryChange = viewModel::updateQuery,
                        onQuickDirectionSelected = viewModel::updateLanguageDirection,
                        onRecentSelected = {
                            viewModel.applyRecentSearch(it)
                            selectedTab = SearchTab.Search
                        },
                        onTrainingSourceSelected = viewModel::setTrainingWordSource,
                        onTrainingPromptChange = viewModel::updateTrainingPrompt,
                        onTrainingEntrySelected = viewModel::selectTrainingEntry,
                        onRandomWordClick = viewModel::loadRandomTrainingEntry,
                        onRevealAnswer = viewModel::revealTrainingAnswer,
                        onHideAnswer = viewModel::hideTrainingAnswer
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSearchLayout(
    selectedTab: SearchTab,
    uiState: DictionaryUiState,
    condensed: Boolean,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onEntryClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onBrowseLetterSelected: (String) -> Unit,
    onBrowseBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onQuickDirectionSelected: (String, String) -> Unit,
    onRecentSelected: (RecentSearch) -> Unit,
    onTrainingSourceSelected: (TrainingWordSource) -> Unit,
    onTrainingPromptChange: (String) -> Unit,
    onTrainingEntrySelected: (DictionaryEntryResult) -> Unit,
    onRandomWordClick: () -> Unit,
    onRevealAnswer: () -> Unit,
    onHideAnswer: () -> Unit
) {
    val bottomBarScrollBehavior = remember(condensed) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!condensed || source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                when {
                    available.y < -1f -> onBottomBarVisibilityChange(false)
                    available.y > 1f -> onBottomBarVisibilityChange(true)
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(bottomBarScrollBehavior),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = PrimaryContentMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = if (condensed) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (condensed) 10.dp else 16.dp)
        ) {
            SearchTabContent(
                selectedTab = selectedTab,
                uiState = uiState,
                condensed = condensed,
                onEntryClick = onEntryClick,
                onFavoriteClick = onFavoriteClick,
                onLoadMore = onLoadMore,
                onBrowseLetterSelected = onBrowseLetterSelected,
                onBrowseBack = onBrowseBack,
                onQueryChange = onQueryChange,
                onQuickDirectionSelected = onQuickDirectionSelected,
                onRecentSelected = onRecentSelected,
                onTrainingSourceSelected = onTrainingSourceSelected,
                onTrainingPromptChange = onTrainingPromptChange,
                onTrainingEntrySelected = onTrainingEntrySelected,
                onRandomWordClick = onRandomWordClick,
                onRevealAnswer = onRevealAnswer,
                onHideAnswer = onHideAnswer
            )
        }
    }
}

@Composable
private fun ExpandedSearchLayout(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    favoriteCount: Int,
    uiState: DictionaryUiState,
    selectedEntry: DictionaryEntryResult?,
    directionLabel: String,
    onEntryClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onBrowseLetterSelected: (String) -> Unit,
    onBrowseBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onQuickDirectionSelected: (String, String) -> Unit,
    onRecentSelected: (RecentSearch) -> Unit,
    onTrainingSourceSelected: (TrainingWordSource) -> Unit,
    onTrainingPromptChange: (String) -> Unit,
    onTrainingEntrySelected: (DictionaryEntryResult) -> Unit,
    onRandomWordClick: () -> Unit,
    onRevealAnswer: () -> Unit,
    onHideAnswer: () -> Unit,
    onCloseEntry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        ExpandedNavigationRail(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            favoriteCount = favoriteCount
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Box(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = TabletListPaneMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchTabContent(
                    selectedTab = selectedTab,
                    uiState = uiState,
                    condensed = false,
                    onEntryClick = onEntryClick,
                    onFavoriteClick = onFavoriteClick,
                    onLoadMore = onLoadMore,
                    onBrowseLetterSelected = onBrowseLetterSelected,
                    onBrowseBack = onBrowseBack,
                    onQueryChange = onQueryChange,
                    onQuickDirectionSelected = onQuickDirectionSelected,
                    onRecentSelected = onRecentSelected,
                    onTrainingSourceSelected = onTrainingSourceSelected,
                    onTrainingPromptChange = onTrainingPromptChange,
                    onTrainingEntrySelected = onTrainingEntrySelected,
                    onRandomWordClick = onRandomWordClick,
                    onRevealAnswer = onRevealAnswer,
                    onHideAnswer = onHideAnswer
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1.25f)
                .fillMaxHeight()
                .widthIn(max = ExpandedShellMaxWidth),
            color = themedCardColor(),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            if (selectedEntry == null) {
                EntryDetailPlaceholder(
                    directionLabel = directionLabel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EntryDetailScreen(
                    entry = selectedEntry,
                    targetLanguageCode = uiState.targetLanguageCode,
                    directionLabel = directionLabel,
                    onBack = onCloseEntry,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.fillMaxSize(),
                    showTopBar = false,
                    enableBackHandler = false
                )
            }
        }
    }
}

@Composable
private fun SearchTabContent(
    selectedTab: SearchTab,
    uiState: DictionaryUiState,
    condensed: Boolean,
    onEntryClick: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onBrowseLetterSelected: (String) -> Unit,
    onBrowseBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onQueryChange: (String) -> Unit,
    onQuickDirectionSelected: (String, String) -> Unit,
    onRecentSelected: (RecentSearch) -> Unit,
    onTrainingSourceSelected: (TrainingWordSource) -> Unit,
    onTrainingPromptChange: (String) -> Unit,
    onTrainingEntrySelected: (DictionaryEntryResult) -> Unit,
    onRandomWordClick: () -> Unit,
    onRevealAnswer: () -> Unit,
    onHideAnswer: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(if (condensed) 10.dp else 16.dp)
        ) {
            when (targetTab) {
                SearchTab.Search -> {
                    Box(modifier = Modifier.weight(1f, fill = true)) {
                        SearchResultsContent(
                            uiState = uiState,
                            onEntryClick = onEntryClick,
                            onFavoriteClick = onFavoriteClick,
                            onLoadMore = onLoadMore,
                            onBrowseLetterSelected = onBrowseLetterSelected,
                            onBrowseBack = onBrowseBack,
                            onQuickDirectionSelected = onQuickDirectionSelected
                        )
                    }
                }
                SearchTab.Favorites -> Box(modifier = Modifier.fillMaxSize()) {
                    FavoritesContent(
                        favorites = uiState.favorites,
                        onEntryClick = onEntryClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }
                SearchTab.Recent -> Box(modifier = Modifier.fillMaxSize()) {
                    RecentContent(
                        recentSearches = uiState.recentSearches,
                        onRecentSelected = onRecentSelected
                    )
                }
                SearchTab.Training -> Box(modifier = Modifier.fillMaxSize()) {
                    TrainingContent(
                        uiState = uiState,
                        condensed = condensed,
                        onTrainingSourceSelected = onTrainingSourceSelected,
                        onTrainingPromptChange = onTrainingPromptChange,
                        onTrainingEntrySelected = onTrainingEntrySelected,
                        onRandomWordClick = onRandomWordClick,
                        onFavoriteClick = onFavoriteClick,
                        onRevealAnswer = onRevealAnswer,
                        onHideAnswer = onHideAnswer
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedNavigationRail(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    favoriteCount: Int
) {
    val useDarkSurfaces = usesDarkSurfaces()
    Surface(
        color = themedCardColor(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        NavigationRail(
            containerColor = Color.Transparent,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            SearchTab.entries.forEach { tab ->
                NavigationRailItem(
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
                    colors = NavigationRailItemDefaults.colors(
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
}

@Composable
private fun EntryDetailPlaceholder(
    directionLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(themedScreenBackgroundBrush())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        MessageCard(
            title = stringResource(R.string.tablet_entry_placeholder_title),
            body = stringResource(R.string.tablet_entry_placeholder_body, directionLabel)
        )
    }
}

@Composable
private fun ModernTopBar(
    directionLabel: String,
    query: String,
    condensed: Boolean,
    onQueryChange: (String) -> Unit,
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
            .padding(horizontal = if (condensed) 8.dp else 12.dp, vertical = if (condensed) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (condensed) 8.dp else 12.dp)
    ) {
        DirectionButton(
            label = directionLabel,
            condensed = condensed,
            onClick = onDirectionClick
        )

        SearchCard(
            query = query,
            condensed = condensed,
            modifier = Modifier.weight(1f),
            onQueryChange = onQueryChange
        )

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

@Composable
private fun ModernBottomNavigation(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    favoriteCount: Int,
    condensed: Boolean
) {
    val useDarkSurfaces = usesDarkSurfaces()
    NavigationBar(
        containerColor = themedCardColor(),
        tonalElevation = 8.dp,
        modifier = Modifier.clip(
            RoundedCornerShape(
                topStart = if (condensed) 18.dp else 24.dp,
                topEnd = if (condensed) 18.dp else 24.dp
            )
        )
    ) {
        SearchTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                alwaysShowLabel = !condensed,
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
    condensed: Boolean,
    onClick: () -> Unit
) {
    val controlHeight = if (condensed) TopBarControlHeightCondensed else TopBarControlHeight
    Surface(
        modifier = Modifier.height(controlHeight),
        onClick = onClick,
        color = themedCardColor(),
        shape = CircleShape,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = if (condensed) 14.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = (if (condensed) {
                    MaterialTheme.typography.labelLarge
                } else {
                    MaterialTheme.typography.titleSmall
                }).copy(fontWeight = FontWeight.Bold),
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
internal fun LanguageDirectionMenu(
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

@Composable
internal fun MessageCard(
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
    condensed: Boolean,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit
) {
    val controlHeight = if (condensed) TopBarControlHeightCondensed else TopBarControlHeight
    var isFocused by remember { mutableStateOf(false) }
    val textStyle = if (condensed) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val leadingIcon: (@Composable (() -> Unit))? = if (!isFocused && query.isBlank()) {
        {
            Icon(
                painter = painterResource(R.drawable.ic_tune),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (condensed) 16.dp else 18.dp)
            )
        }
    } else {
        null
    }
    Surface(
        modifier = modifier.height(controlHeight),
        color = themedCardColor(),
        shape = RoundedCornerShape(if (condensed) 20.dp else 24.dp),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .testTag(BENCHMARK_SEARCH_INPUT_TAG)
                .onFocusChanged { isFocused = it.isFocused }
                .padding(
                    horizontal = 0.dp,
                    vertical = 0.dp
                ),
            placeholder = {
                Text(
                    stringResource(R.string.search_placeholder),
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    TextButton(
                        onClick = { onQueryChange("") },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.clear_query),
                            style = if (condensed) {
                                MaterialTheme.typography.labelSmall
                            } else {
                                MaterialTheme.typography.labelMedium
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(if (condensed) 16.dp else 20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = textStyle
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
    onBrowseBack: () -> Unit,
    onQuickDirectionSelected: (String, String) -> Unit
) {
    val resultsListState = rememberSaveable(uiState.query, saver = LazyListState.Saver) {
        LazyListState()
    }

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
        state = resultsListState,
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
    val browseListState = rememberSaveable(uiState.selectedBrowseLetter, saver = LazyListState.Saver) {
        LazyListState()
    }
    val selectedLetter = uiState.selectedBrowseLetter
    var isBrowseHeaderVisible by rememberSaveable(selectedLetter) { mutableStateOf(true) }
    var previousBrowseIndex by remember(selectedLetter) { mutableStateOf(0) }
    var previousBrowseOffset by remember(selectedLetter) { mutableStateOf(0) }

    if (selectedLetter == null) {
        AlphabetChooserContent(
            sourceLanguageCode = uiState.sourceLanguageCode,
            availableLetters = uiState.browseAvailableLetters,
            onLetterSelected = onBrowseLetterSelected
        )
        return
    }

    LaunchedEffect(
        browseListState.firstVisibleItemIndex,
        browseListState.firstVisibleItemScrollOffset,
        selectedLetter
    ) {
        val index = browseListState.firstVisibleItemIndex
        val offset = browseListState.firstVisibleItemScrollOffset
        val isScrollingDown = index < previousBrowseIndex || (index == previousBrowseIndex && offset < previousBrowseOffset)
        val isScrollingUp = index > previousBrowseIndex || (index == previousBrowseIndex && offset > previousBrowseOffset)

        when {
            index == 0 && offset == 0 -> isBrowseHeaderVisible = true
            isScrollingDown -> isBrowseHeaderVisible = true
            isScrollingUp -> isBrowseHeaderVisible = false
        }

        previousBrowseIndex = index
        previousBrowseOffset = offset
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isBrowseHeaderVisible,
            enter = fadeIn(animationSpec = tween(160)) + expandVertically(animationSpec = tween(160)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120))
        ) {
            BrowseResultsHeader(
                letter = selectedLetter,
                onBack = onBrowseBack
            )
        }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = browseListState,
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
    val gridState = rememberSaveable(sourceLanguageCode, saver = LazyGridState.Saver) {
        LazyGridState()
    }
    val letters = browseAlphabetLetters(
        sourceLanguageCode = sourceLanguageCode,
        availableLetters = availableLetters
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        modifier = Modifier.fillMaxSize(),
        state = gridState,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                ,
                modifier = Modifier.weight(1f)
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
    val favoritesListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    if (favorites.isEmpty()) {
        MessageCard(
            title = stringResource(R.string.no_favorites_title),
            body = stringResource(R.string.no_favorites_body)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = favoritesListState,
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
    val recentListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    if (recentSearches.isEmpty()) {
        MessageCard(
            title = stringResource(R.string.no_recent_searches_title),
            body = stringResource(R.string.no_recent_searches_body)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = recentListState,
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
internal fun currentDirectionLabel(
    sourceLanguageCode: String,
    targetLanguageCode: String
): String {
    val source = AppLanguage.fromCode(sourceLanguageCode).shortLabel
    val target = AppLanguage.fromCode(targetLanguageCode).shortLabel
    return stringResource(R.string.direction_short_format, source, target)
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
