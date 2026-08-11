package com.example.clockreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.clockreminder.data.ReminderRepository
import com.example.clockreminder.ui.ClockReminderTheme
import com.example.clockreminder.ui.ReminderScreen

class MainActivity : ComponentActivity() {

    private lateinit var repo: ReminderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repo = ReminderRepository(applicationContext)
        setContent {
            ClockReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ReminderScreen(repo)
                    }
                }
            }
        }
    }
}
