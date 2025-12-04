package com.sta.staenturno.ui.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sta.staenturno.R
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.ui.components.LoadingDialog
import com.sta.staenturno.ui.theme.*
import com.sta.staenturno.util.DeviceIdProvider
import com.sta.staenturno.util.LocationProvider

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }
    val deviceIdProvider = remember { DeviceIdProvider(context, prefsManager) }
    val locationProvider = remember { LocationProvider(context) }
    
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(prefsManager, deviceIdProvider, locationProvider) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                      permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (granted) {
            viewModel.registrarAsistencia("ENTRADA")
        } else {
            Toast.makeText(context, "Se requiere permiso de ubicación", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarHome()
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLavender)
    ) {
        // Purple header bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelPurple, PastelPurpleLight)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Title with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon with circular clip
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "STAenTurno",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Employee Info Card - Minimal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PastelPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.nombreCompleto.ifEmpty { "Cargando..." },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = uiState.turnoActivoHoy.ifEmpty { "-" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status indicator - Minimal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (uiState.yaFichoEntrada) PastelGreen.copy(alpha = 0.15f) else PastelRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.yaFichoEntrada) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (uiState.yaFichoEntrada) PastelGreen else PastelRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (uiState.yaFichoEntrada) "Entrada Registrada" else "Entrada NO Registrada",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                    if (uiState.yaFichoEntrada && uiState.horaEntrada != null) {
                        Text(
                            text = uiState.horaEntrada!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium
                        )
                    }
                }
            }

            // Alerts - Minimal
            if (uiState.tieneLicencia || uiState.esFeriado) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.tieneLicencia) {
                MinimalAlert(
                    icon = Icons.Default.Info,
                    text = uiState.descripcionLicencia ?: "Licencia activa",
                    color = PastelPurple
                )
            }

            if (uiState.esFeriado) {
                Spacer(modifier = Modifier.height(8.dp))
                MinimalAlert(
                    icon = Icons.Default.Star,
                    text = uiState.descripcionFeriado ?: "Día feriado",
                    color = PastelOrange
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons - 3D Style
            when (uiState.attendanceState) {
                AttendanceState.NOT_STARTED -> {
                    Button3D(
                        text = "▶ INICIO DE JORNADA",
                        backgroundColor = PastelGreen,
                        onClick = {
                            if (locationProvider.hasLocationPermission()) {
                                viewModel.registrarAsistencia("ENTRADA")
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !uiState.isLoading
                    )
                }
                AttendanceState.WORKING -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.aceptaPausa) {
                            Button3D(
                                text = "⏸ INICIO DE PAUSA",
                                backgroundColor = PastelOrange,
                                onClick = { viewModel.registrarAsistencia("PAUSA_INICIO") },
                                enabled = !uiState.isLoading
                            )
                        }
                        
                        Button3D(
                            text = "⏹ FIN DE JORNADA",
                            backgroundColor = PastelRed,
                            onClick = { viewModel.registrarAsistencia("SALIDA") },
                            enabled = !uiState.isLoading
                        )
                    }
                }
                AttendanceState.PAUSED -> {
                    Button3D(
                        text = "▶ FIN DE PAUSA",
                        backgroundColor = PastelGreen,
                        onClick = { viewModel.registrarAsistencia("PAUSA_FIN") },
                        enabled = !uiState.isLoading
                    )
                }
                AttendanceState.FINISHED -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PastelGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Jornada Finalizada",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        if (uiState.horasTrabajadas != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Horas: ${uiState.horasTrabajadas}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button3D(
                            text = "SALIR",
                            backgroundColor = PastelPurple,
                            onClick = { (context as? android.app.Activity)?.finish() },
                            enabled = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        
        LoadingDialog(isLoading = uiState.isLoading)
    }
}

@Composable
fun Button3D(
    text: String,
    backgroundColor: Color,
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


        // Main button with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = offsetY)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.lerp(backgroundColor, Color.White, 0.3f), // Lighter top
                            backgroundColor                      // Darker bottom
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .then(
                    if (enabled) {
                        Modifier.clickable {
                            isPressed = true
                            onClick()
                        }
                    } else Modifier
                ),
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
                
                // Circular icon on the right
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = backgroundColor,
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

@Composable
fun MinimalAlert(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextDark
        )
    }
}
