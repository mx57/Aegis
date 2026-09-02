package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.nav.MainAppHost
import com.example.ui.theme.RunicStaveTheme
import com.example.ui.viewmodel.RuneViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RuneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RunicStaveTheme {
                MainAppHost(viewModel = viewModel)
            }
        }
    }
}
