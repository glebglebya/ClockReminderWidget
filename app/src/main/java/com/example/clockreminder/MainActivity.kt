package com.example.clockreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.clockreminder.data.ReminderRepository
import com.example.clockreminder.ui.ReminderScreen

class MainActivity : ComponentActivity() {

    private lateinit var repo: ReminderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = ReminderRepository(applicationContext)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReminderScreen(repo)
                }
            }
        }
    }
}
