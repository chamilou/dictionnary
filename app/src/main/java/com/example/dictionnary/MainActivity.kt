package com.example.dictionnary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dictionnary.presentation.ui.AppThemeMode
import com.example.dictionnary.presentation.ui.UiLanguageManager
import com.example.dictionnary.presentation.ui.search.SearchScreen
import com.example.dictionnary.presentation.viewmodel.DictionaryViewModel
import com.example.dictionnary.ui.theme.DictionnaryTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(UiLanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DictionaryApp()
        }
    }
}

@Composable
fun DictionaryApp() {
    val viewModel: DictionaryViewModel = viewModel()
    val darkTheme = when (viewModel.uiState.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    DictionnaryTheme(darkTheme = darkTheme) {
        SearchScreen(viewModel = viewModel)
    }
}
