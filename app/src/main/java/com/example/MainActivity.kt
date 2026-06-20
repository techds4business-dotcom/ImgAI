package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ArtRepository
import com.example.ui.ArtGeneratorScreen
import com.example.ui.ArtViewModel
import com.example.ui.ArtViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Instantiate architecture layer
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ArtRepository(applicationContext, database)
        
        // ViewModel provisioning
        val factory = ArtViewModelFactory(application, repository)
        val viewModel: ArtViewModel by viewModels { factory }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArtGeneratorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
