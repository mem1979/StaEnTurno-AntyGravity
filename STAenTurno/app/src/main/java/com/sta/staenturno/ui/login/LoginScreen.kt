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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.ui.components.LoadingDialog
import com.sta.staenturno.ui.theme.*
import com.sta.staenturno.util.BiometricPromptManager
import com.sta.staenturno.util.DeviceIdProvider

/**
 * Modifier de extensión para conectar un campo a Android Autofill Framework.
 *
 * Usa AutofillNode (API experimental estable desde Compose 1.2) para registrar
 * el campo con el tipo de autofill indicado. Esto permite que Google Password Manager,
 * Samsung Pass, y otros servicios del sistema ofrezcan guardar/recuperar credenciales.
 *
 * @param autofillTypes Tipos de autofill a asociar (ej: AutofillType.Username, AutofillType.Password)
 * @param onFill Callback ejecutado cuando el sistema autofill rellena el campo
 */
@ExperimentalComposeUiApi
private fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: (String) -> Unit
): Modifier {
    return composed {
        val autofill = LocalAutofill.current
        val autofillNode = AutofillNode(
            autofillTypes = autofillTypes,
            onFill = onFill
        )
        LocalAutofillTree.current += autofillNode

        this
            .onGloballyPositioned { autofillNode.boundingBox = it.boundsInWindow() }
            .onFocusChanged { focusState ->
                autofill?.run {
                    if (focusState.isFocused) requestAutofillForNode(autofillNode)
                    else cancelAutofillForNode(autofillNode)
                }
            }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
    // Estado del toggle de visibilidad de contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    // ── BLOQUEANTE: si el login es exitoso, se pide biometría antes de navegar ──
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

            // ── Campo USUARIO ────────────────────────────────────────────────
            // FIX 1: focusedTextColor/unfocusedTextColor explícitos desde
            //        MaterialTheme.colorScheme.onSurface. Esto garantiza
            //        legibilidad en TODOS los dispositivos/temas.
            // FIX 3: Modifier.autofill(AutofillType.Username) para que Android
            //        Autofill Framework ofrezca guardar/recuperar el usuario.
            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .autofill(
                        autofillTypes = listOf(AutofillType.Username),
                        onFill = { usuario = it }
                    ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    // FIX CRÍTICO: texto siempre visible, no depende de dynamicColor
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

            // ── Campo CONTRASEÑA ─────────────────────────────────────────────
            // FIX 1: mismos colores de texto explícitos.
            // FIX 2: trailingIcon con toggle show/hide accesible (TalkBack).
            // FIX 3: AutofillType.Password para Autofill Framework.
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .autofill(
                        autofillTypes = listOf(AutofillType.Password),
                        onFill = { contrasena = it }
                    ),
                singleLine = true,
                // Toggle dinámico: muestra/oculta la contraseña
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                // Ícono de ojo con contentDescription accesible para TalkBack
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña"
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
