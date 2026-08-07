package com.avardiction.app.presentation.ui.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.pm.PackageInfoCompat
import com.avardiction.app.R
import com.avardiction.app.domain.model.AppLanguage
import com.avardiction.app.presentation.ui.AppThemeMode
import com.avardiction.app.presentation.ui.appLanguageDisplayName
import com.avardiction.app.presentation.viewmodel.DictionaryUiState
import com.avardiction.app.presentation.viewmodel.LanguageCoverageSupport
import com.avardiction.app.presentation.viewmodel.LanguageWordCount
import androidx.compose.ui.unit.dp

private val DialogMaxWidth = 560.dp
private val SheetContentMaxWidth = 560.dp

internal data class SearchScreenOverlaysParams(
    val uiState: DictionaryUiState,
    val context: android.content.Context,
    val directionMenuExpanded: Boolean,
    val onDirectionMenuDismiss: () -> Unit,
    val onDirectionSelected: (String, String) -> Unit,
    val settingsSheetExpanded: Boolean,
    val onSettingsDismiss: () -> Unit,
    val onThemeClick: () -> Unit,
    val onUiLanguageClick: () -> Unit,
    val onAboutClick: () -> Unit,
    val onPrivacyClick: () -> Unit,
    val onSupportClick: () -> Unit,
    val onReferencesClick: () -> Unit,
    val onCoverageClick: () -> Unit,
    val uiLanguageDialogExpanded: Boolean,
    val onUiLanguageDismiss: () -> Unit,
    val onUiLanguageSelected: (String?) -> Unit,
    val themeDialogExpanded: Boolean,
    val onThemeDialogDismiss: () -> Unit,
    val onThemeSelected: (AppThemeMode) -> Unit,
    val aboutDialogExpanded: Boolean,
    val onAboutDismiss: () -> Unit,
    val privacyDialogExpanded: Boolean,
    val onPrivacyDismiss: () -> Unit,
    val supportDialogExpanded: Boolean,
    val onSupportDismiss: () -> Unit,
    val referencesDialogExpanded: Boolean,
    val onReferencesDismiss: () -> Unit,
    val coverageDialogExpanded: Boolean,
    val onCoverageDismiss: () -> Unit
)

@Composable
internal fun SearchScreenOverlays(params: SearchScreenOverlaysParams) {
    LanguageDirectionMenu(
        expanded = params.directionMenuExpanded,
        currentSourceLanguageCode = params.uiState.sourceLanguageCode,
        onDismiss = params.onDirectionMenuDismiss,
        onDirectionSelected = params.onDirectionSelected
    )

    if (params.settingsSheetExpanded) {
        SettingsSheet(
            themeMode = params.uiState.themeMode,
            uiLanguageLabel = uiLanguageDisplayName(params.uiState.uiLanguageCode),
            totalEntryCount = params.uiState.totalEntryCount,
            isLoading = params.uiState.isSettingsInfoLoading,
            onDismiss = params.onSettingsDismiss,
            onThemeClick = params.onThemeClick,
            onUiLanguageClick = params.onUiLanguageClick,
            onAboutClick = params.onAboutClick,
            onPrivacyClick = params.onPrivacyClick,
            onSupportClick = params.onSupportClick,
            onReferencesClick = params.onReferencesClick,
            onCoverageClick = params.onCoverageClick
        )
    }

    if (params.uiLanguageDialogExpanded) {
        UiLanguageDialog(
            selectedLanguageCode = params.uiState.uiLanguageCode,
            onDismiss = params.onUiLanguageDismiss,
            onLanguageSelected = params.onUiLanguageSelected
        )
    }

    if (params.themeDialogExpanded) {
        ThemeModeDialog(
            selectedThemeMode = params.uiState.themeMode,
            onDismiss = params.onThemeDialogDismiss,
            onThemeSelected = params.onThemeSelected
        )
    }

    if (params.aboutDialogExpanded) {
        AboutAppDialog(
            totalEntryCount = params.uiState.totalEntryCount,
            onDismiss = params.onAboutDismiss
        )
    }

    if (params.privacyDialogExpanded) {
        PrivacyDialog(onDismiss = params.onPrivacyDismiss)
    }

    if (params.supportDialogExpanded) {
        SupportDialog(onDismiss = params.onSupportDismiss)
    }

    if (params.referencesDialogExpanded) {
        ReferencesDialog(onDismiss = params.onReferencesDismiss)
    }

    if (params.coverageDialogExpanded) {
        LanguageCoverageDialog(
            totalEntryCount = params.uiState.totalEntryCount,
            languageWordCounts = params.uiState.languageWordCounts,
            isLoading = params.uiState.isSettingsInfoLoading,
            onDismiss = params.onCoverageDismiss
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = SheetContentMaxWidth)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_theme),
                    subtitle = themeModeDisplayName(themeMode),
                    onClick = onThemeClick
                )
                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_ui_language),
                    subtitle = uiLanguageLabel,
                    onClick = onUiLanguageClick
                )
                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_about),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.settings_about_summary),
                    onClick = onAboutClick
                )
                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_privacy),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.settings_privacy_summary),
                    onClick = onPrivacyClick
                )
                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_support),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.settings_support_summary),
                    onClick = onSupportClick
                )
                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_references),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.settings_references_summary),
                    onClick = onReferencesClick
                )
                SettingsActionRow(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_word_counts),
                    subtitle = if (isLoading) {
                        androidx.compose.ui.res.stringResource(R.string.settings_loading)
                    } else {
                        androidx.compose.ui.res.stringResource(
                            R.string.settings_word_counts_summary_format,
                            totalEntryCount
                        )
                    },
                    onClick = onCoverageClick
                )
            }
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
        color = settingsRowColor(),
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

    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.theme_dialog_title)) },
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
                Text(androidx.compose.ui.res.stringResource(R.string.close))
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
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)

    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.about_app_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.about_app_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        R.string.about_app_version_format,
                        versionName,
                        versionCode
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.about_app_entries_format, totalEntryCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.about_app_dataset_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun PrivacyDialog(onDismiss: () -> Unit) {
    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.privacy_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.privacy_body_storage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.privacy_body_network),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.privacy_body_support),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun SupportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val supportEmail = androidx.compose.ui.res.stringResource(R.string.support_email_address)

    fun openSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent)
                onDismiss()
            } catch (_: ActivityNotFoundException) {
            }
        }
    }

    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.support_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.support_body),
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
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.support_status_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.support_release_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun ReferencesDialog(onDismiss: () -> Unit) {
    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.references_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.references_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.references_avar_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.references_english_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.references_english_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun LanguageCoverageDialog(
    totalEntryCount: Int,
    languageWordCounts: List<LanguageWordCount>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.language_coverage_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.language_coverage_total_entries_format, totalEntryCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.language_coverage_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isLoading) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.settings_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    languageWordCounts.forEach { count ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val supportLabel = when (count.support) {
                                LanguageCoverageSupport.SUPPORTED -> null
                                LanguageCoverageSupport.DRAFT_BRIDGE -> androidx.compose.ui.res.stringResource(R.string.direction_support_draft_bridge)
                                LanguageCoverageSupport.COMING_SOON -> androidx.compose.ui.res.stringResource(R.string.direction_support_coming_soon)
                            }
                            Text(
                                text = if (supportLabel == null) {
                                    androidx.compose.ui.res.stringResource(
                                        R.string.language_count_format,
                                        appLanguageDisplayName(count.languageCode),
                                        count.count
                                    )
                                } else {
                                    androidx.compose.ui.res.stringResource(
                                        R.string.language_count_with_status_format,
                                        appLanguageDisplayName(count.languageCode),
                                        count.count,
                                        supportLabel
                                    )
                                },
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
                Text(androidx.compose.ui.res.stringResource(R.string.close))
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

    AdaptiveAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.ui_language_dialog_title)) },
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
                Text(androidx.compose.ui.res.stringResource(R.string.back))
            }
        }
    )
}

@Composable
private fun AdaptiveAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .widthIn(max = DialogMaxWidth),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest,
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
private fun uiLanguageDisplayName(languageCode: String?): String {
    return if (languageCode == null) {
        androidx.compose.ui.res.stringResource(R.string.ui_language_system_default)
    } else {
        appLanguageDisplayName(languageCode)
    }
}

@Composable
private fun themeModeDisplayName(themeMode: AppThemeMode): String {
    return when (themeMode) {
        AppThemeMode.SYSTEM -> androidx.compose.ui.res.stringResource(R.string.theme_system)
        AppThemeMode.LIGHT -> androidx.compose.ui.res.stringResource(R.string.theme_light)
        AppThemeMode.DARK -> androidx.compose.ui.res.stringResource(R.string.theme_dark)
    }
}

@Composable
private fun settingsRowColor() = if (usesDarkSurfaces()) {
    MaterialTheme.colorScheme.surface
} else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
}

@Composable
private fun usesDarkSurfaces(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}
