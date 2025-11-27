package com.sta.staenturno.ui.login

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.ui.components.LoadingDialog
import com.sta.staenturno.ui.theme.*
import com.sta.staenturno.util.BiometricPromptManager
import com.sta.staenturno.util.DeviceIdProvider

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onMustChangePassword: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }
    val deviceIdProvider = remember { DeviceIdProvider(context, prefsManager) }
    val viewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(deviceIdProvider, prefsManager) as T
            }
        }
    )

    val biometricPromptManager = remember {
        BiometricPromptManager(context as AppCompatActivity)
    }
    val biometricResult by biometricPromptManager.promptResults.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()
    
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            biometricPromptManager.showBiometricPrompt(
                title = "Verificación Biométrica",
                description = "Confirme su identidad para continuar"
            )
        }
    }

    LaunchedEffect(biometricResult) {
        if (uiState.isSuccess && biometricResult == BiometricPromptManager.BiometricResult.AuthenticationSuccess) {
            onLoginSuccess()
        }
        if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationError ||
            biometricResult is BiometricPromptManager.BiometricResult.AuthenticationFailed) {
             viewModel.onBiometricFailure()
             Toast.makeText(context, "Autenticación fallida", Toast.LENGTH_SHORT).show()
        }
        if (biometricResult == BiometricPromptManager.BiometricResult.FeatureUnavailable ||
            biometricResult == BiometricPromptManager.BiometricResult.HardwareUnavailable ||
            biometricResult == BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
             viewModel.onBiometricFailure()
             Toast.makeText(context, "Dispositivo sin seguridad configurada. Configure bloqueo de pantalla o biometría.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.mustChangePassword) {
        if (uiState.mustChangePassword) {
            onMustChangePassword()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLavender)
    ) {
        // Purple header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelPurple, PastelPurpleLight)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Usuario field
            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario", color = TextMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PastelPurple,
                    unfocusedBorderColor = TextLight,
                    focusedLabelColor = PastelPurple,
                    cursorColor = PastelPurple
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña field
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña", color = TextMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PastelPurple,
                    unfocusedBorderColor = TextLight,
                    focusedLabelColor = PastelPurple,
                    cursorColor = PastelPurple
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login button - 3D style
            LoginButton3D(
                text = "INGRESAR",
                onClick = { viewModel.login(usuario, contrasena) },
                enabled = !uiState.isLoading && usuario.isNotBlank() && contrasena.isNotBlank()
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        LoadingDialog(isLoading = uiState.isLoading)
    }
}

@Composable
fun LoginButton3D(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Shadow/Depth layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .offset(y = 4.dp)
                .background(
                    color = PastelPurple.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        // Main button with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = offsetY)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.lerp(PastelPurple, Color.White, 0.3f),
                            PastelPurple
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .clickable(enabled = enabled) {
                    isPressed = true
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
                )
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = PastelPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}
