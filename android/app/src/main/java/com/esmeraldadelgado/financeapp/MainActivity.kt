package com.esmeraldadelgado.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.esmeraldadelgado.financeapp.data.auth.AuthRepository
import com.esmeraldadelgado.financeapp.ui.auth.AuthScreen
import com.esmeraldadelgado.financeapp.ui.theme.FinanceAppTheme
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceAppTheme {
                Scaffold { padding ->
                    Box(Modifier.padding(padding)) {
                        val status by authRepository.sessionStatus.collectAsState()
                        val scope = rememberCoroutineScope()

                        when (status) {
                            is SessionStatus.Initializing -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                                CircularProgressIndicator()
                            }
                            is SessionStatus.Authenticated -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("¡Sesión iniciada! 🎉")
                                    Button(onClick = { scope.launch { authRepository.signOut() } }) {
                                        Text("Cerrar sesión")
                                    }
                                }
                            }
                            else -> AuthScreen()
                        }
                    }
                }
            }
        }
    }
}