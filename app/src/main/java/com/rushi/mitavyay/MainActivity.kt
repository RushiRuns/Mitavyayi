package com.rushi.mitavyay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.rushi.mitavyay.ui.MainViewModel
import com.rushi.mitavyay.ui.MitavyayApp
import com.rushi.mitavyay.ui.theme.MitavyayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()
            val fontScale by mainViewModel.fontScale.collectAsState()

            MitavyayTheme(
                themeMode = themeMode,
                fontScale = fontScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MitavyayApp()
                }
            }
        }
    }
}