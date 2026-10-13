package com.esmeraldadelgado.financeapp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esmeraldadelgado.financeapp.R
import kotlinx.coroutines.delay

private const val PASSWORD_VISIBLE_MS = 3_000L

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val fieldShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.financestarticon),
            contentDescription = "Logo FinanceStart",
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = if (state.isRegister) "¡Empieza tu viaje! 🌱" else "¡Hola de nuevo! 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (state.isRegister) "Crea tu cuenta y haz crecer tu dinero"
            else "Tu dinero, simple y bajo control",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(32.dp))

        if (state.isRegister) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("¿Cómo te llamas?") },
                singleLine = true,
                shape = fieldShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Correo") },
            singleLine = true,
            shape = fieldShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        PasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            onDone = viewModel::submit,
            shape = fieldShape
        )

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::submit,
            enabled = !state.isLoading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (state.isRegister) "Crear mi cuenta" else "Entrar",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = viewModel::toggleMode) {
            Text(if (state.isRegister) "¿Ya tienes cuenta? Inicia sesión" else "¿Eres nuevo? Crea tu cuenta")
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    shape: RoundedCornerShape
) {
    var visible by remember { mutableStateOf(false) }

    // Se oculta sola despues de unos segundos
    LaunchedEffect(visible) {
        if (visible) {
            delay(PASSWORD_VISIBLE_MS)
            visible = false
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña") },
        singleLine = true,
        shape = shape,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}