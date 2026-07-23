package com.example.dictionnary.presentation.ui

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object UiLanguageManager {
    private const val PREFERENCES_NAME = "dictionary_preferences"
    private const val KEY_UI_LANGUAGE_CODE = "ui_language_code"

    fun getSavedUiLanguageCode(context: Context): String? {
        return context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_UI_LANGUAGE_CODE, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun saveUiLanguageCode(context: Context, languageCode: String?) {
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_UI_LANGUAGE_CODE, languageCode)
            .apply()
    }

    fun wrap(context: Context): Context {
        val languageCode = getSavedUiLanguageCode(context) ?: return context
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))

        return context.createConfigurationContext(configuration)
    }
}
