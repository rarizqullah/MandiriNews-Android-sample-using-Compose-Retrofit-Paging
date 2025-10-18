package com.mandiri.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable   // <-- TAMBAH INI
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandiri.newsapp.ui.screen.HomeScreen
import com.mandiri.newsapp.ui.screen.NewsViewModel
import com.mandiri.newsapp.ui.theme.NewsappTheme
import com.mandiri.newsapp.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }

            val dark = when (themeMode) {
                ThemeMode.DARK  -> true
                ThemeMode.LIGHT -> false
                else            -> isSystemInDarkTheme()
            }

            NewsappTheme(darkTheme = dark) {
                val vm: NewsViewModel = viewModel()
                HomeScreen(
                    vm = vm,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it }
                )
            }
        }
    }
}
