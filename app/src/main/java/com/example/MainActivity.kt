package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavHost
import com.example.ui.theme.SevenHooksTheme
import com.example.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ThemeManager.init(this)
    enableEdgeToEdge()
    setContent {
      SevenHooksTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavHost()
        }
      }
    }
  }
}
