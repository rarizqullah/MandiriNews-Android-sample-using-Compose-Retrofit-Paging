package com.mandiri.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandiri.newsapp.ui.screen.HomeScreen
import com.mandiri.newsapp.ui.screen.NewsViewModel
import com.mandiri.newsapp.ui.theme.NewsappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsappTheme {
                val vm: NewsViewModel = viewModel()
                HomeScreen(vm)
            }
        }
    }
}
