package com.esmeraldadelgado.financeapp.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esmeraldadelgado.financeapp.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isRegister: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value.trim(), error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun toggleMode() = _uiState.update { it.copy(isRegister = !it.isRegister, error = null) }

    fun submit() {
        val state = _uiState.value
        validate(state)?.let { msg ->
            _uiState.update { it.copy(error = msg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (state.isRegister) repository.signUp(state.name, state.email, state.password)
                else repository.signIn(state.email, state.password)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.toFriendlyMessage()) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun validate(s: AuthUiState): String? = when {
        s.isRegister && s.name.isBlank() -> "Ingresa tu nombre"
        !Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> "Correo inválido"
        s.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    private fun Exception.toFriendlyMessage(): String {
        val msg = message.orEmpty()
        return when {
            "Invalid login credentials" in msg -> "Correo o contraseña incorrectos"
            "already registered" in msg -> "Este correo ya está registrado"
            "Unable to resolve host" in msg -> "Sin conexión a internet"
            else -> "Ocurrió un error. Intenta de nuevo."
        }
    }
}