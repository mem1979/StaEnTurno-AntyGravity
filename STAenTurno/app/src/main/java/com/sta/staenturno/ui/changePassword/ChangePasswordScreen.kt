package com.sta.staenturno.ui.changePassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.ui.components.LoadingDialog
import com.sta.staenturno.ui.theme.PastelPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onChangeSuccess: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }
    val viewModel: ChangePasswordViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ChangePasswordViewModel(prefsManager) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var nueva by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    // Estados independientes de toggle para cada campo
    var nuevaVisible by remember { mutableStateOf(false) }
    var confirmacionVisible by remember { mutableStateOf(false) }

    // Validación de coincidencia local (no necesita ir al ViewModel)
    val noCoinciden = nueva.isNotEmpty() && confirmacion.isNotEmpty() && nueva != confirmacion

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onChangeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cambiar Contraseña",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Debe cambiar su contraseña por defecto",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "La nueva contraseña debe tener al menos 8 caracteres.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Campo: NUEVA CONTRASEÑA ──────────────────────────────────────
            // Con toggle show/hide y colores explícitos (mismos fixes del login)
            OutlinedTextField(
                value = nueva,
                onValueChange = { nueva = it },
                label = { Text("Nueva Contraseña") },
                placeholder = { Text("Mínimo 8 caracteres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (nuevaVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { nuevaVisible = !nuevaVisible }) {
                        Icon(
                            imageVector = if (nuevaVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = if (nuevaVisible) "Ocultar contraseña"
                                                 else "Mostrar contraseña",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor      = PastelPurple,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    focusedLabelColor       = PastelPurple,
                    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor             = PastelPurple,
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: CONFIRMAR CONTRASEÑA ──────────────────────────────────
            // Borde rojo si las contraseñas no coinciden (feedback inmediato)
            OutlinedTextField(
                value = confirmacion,
                onValueChange = { confirmacion = it },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = noCoinciden,
                visualTransformation = if (confirmacionVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmacionVisible = !confirmacionVisible }) {
                        Icon(
                            imageVector = if (confirmacionVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmacionVisible) "Ocultar confirmación"
                                                 else "Mostrar confirmación",
                            tint = if (noCoinciden) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                supportingText = {
                    if (noCoinciden) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                    errorTextColor          = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor      = PastelPurple,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    errorBorderColor        = MaterialTheme.colorScheme.error,
                    focusedLabelColor       = PastelPurple,
                    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                    errorLabelColor         = MaterialTheme.colorScheme.error,
                    cursorColor             = PastelPurple,
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorContainerColor     = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Error del servidor
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.changePassword(nueva) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                // Deshabilitado si: cargando, campos vacíos, o no coinciden
                enabled = !uiState.isLoading
                        && nueva.isNotBlank()
                        && confirmacion.isNotBlank()
                        && !noCoinciden,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PastelPurple,
                    disabledContainerColor = PastelPurple.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "CAMBIAR Y CONTINUAR",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LoadingDialog(isLoading = uiState.isLoading)
    }
}
