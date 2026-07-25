package com.avardiction.app.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.avardiction.app.R
import com.avardiction.app.domain.model.AppLanguage

@StringRes
fun appLanguageNameRes(languageCode: String): Int {
    return when (AppLanguage.fromCode(languageCode)) {
        AppLanguage.ALL -> R.string.language_all
        AppLanguage.AV -> R.string.language_avar
        AppLanguage.EN -> R.string.language_english
        AppLanguage.RU -> R.string.language_russian
        AppLanguage.DE -> R.string.language_german
        AppLanguage.ES -> R.string.language_spanish
        AppLanguage.FR -> R.string.language_french
    }
}

@Composable
fun appLanguageDisplayName(languageCode: String): String {
    return stringResource(appLanguageNameRes(languageCode))
}
