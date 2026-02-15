package com.sta.staenturno.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.data.model.DaySchedule
import com.sta.staenturno.ui.components.LoadingDialog
import com.sta.staenturno.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScheduleScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }
    
    val viewModel: WeekScheduleViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return WeekScheduleViewModel(prefsManager) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarAgenda()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLavender)
    ) {
        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PastelPurple, PastelPurpleLight)
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi Agenda Semanal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Range Card
            if (uiState.desde.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = PastelPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${formatDate(uiState.desde)} - ${formatDate(uiState.hasta)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Schedule List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val sortedDays = uiState.dias.entries
                    .filter { it.value.laboral } // Only show working days
                    .sortedBy { it.key }
                
                if (sortedDays.isEmpty() && !uiState.isLoading) {
                    item {
                        Text(
                            text = "No hay turnos laborales esta semana.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                items(sortedDays) { (dateString, schedule) ->
                    DayScheduleItem(dateString, schedule)
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        LoadingDialog(isLoading = uiState.isLoading)
        
        if (uiState.error != null) {
            // Simple error toast or snackbar could be better, but for now just showing it
            // In a real app we might want a retry button
        }
    }
}

@Composable
fun DayScheduleItem(dateString: String, schedule: DaySchedule) {
    val context = LocalContext.current
    val alarmHelper = remember { com.sta.staenturno.util.AlarmHelper(context) }
    
    val date = LocalDate.parse(dateString)
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val dayNumber = date.dayOfMonth.toString()
    
    val isToday = date == LocalDate.now()
    val isWorkDay = schedule.laboral

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isToday) androidx.compose.foundation.BorderStroke(2.dp, PastelPurple) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp)
                        .background(
                            color = if (isWorkDay) PastelPurple.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = dayNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isWorkDay) PastelPurple else TextMedium
                    )
                    Text(
                        text = dayName.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isWorkDay) PastelPurple else TextMedium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isToday) "HOY" else dayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isToday) PastelPurple else TextMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Handle multiple shifts or fallback to legacy single shift
                    val shifts = if (schedule.turnos.isNotEmpty()) {
                        schedule.turnos
                    } else if (schedule.descripcion != null) {
                        // Legacy fallback
                        listOf(com.sta.staenturno.data.model.ShiftDetail(
                            nombre = "",
                            descripcion = schedule.descripcion,
                            horaInicio = "00:00",
                            horaFin = "00:00"
                        ))
                    } else {
                        emptyList()
                    }

                    shifts.forEachIndexed { index, shift ->
                        if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                        
                        // Shift name (if available)
                        if (shift.nombre.isNotEmpty()) {
                            Text(
                                text = "Turno: ${shift.nombre}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (shift.tipo == "EXTRA") Color(0xFFFF9800) else PastelPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        // Time range
                        if (isWorkDay) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = TextMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${shift.horaInicio} - ${shift.horaFin}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                            }
                            
                            // Tolerance and bonus info
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tolerance
                                if (shift.toleranciaMinutos > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "⏱",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${shift.toleranciaMinutos}min",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMedium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                
                                // Bonus
                                if (shift.bonificacionPorcentaje > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "💰",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+${shift.bonificacionPorcentaje.toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Alarm Actions
            if (isWorkDay && schedule.turnos.isNotEmpty()) {
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                
                schedule.turnos.forEach { shift ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shift label
                        if (shift.nombre.isNotEmpty()) {
                            Text(
                                text = shift.nombre,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        // Entry Alarm Button
                        OutlinedButton(
                            onClick = {
                                val shiftName = if (shift.nombre.isNotEmpty()) shift.nombre else "Turno"
                                alarmHelper.setAlarm(
                                    shift.horaInicio, 
                                    "🔔 Entrada $shiftName - $dayName ${shift.horaInicio}"
                                )
                            },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entrada", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Exit Alarm Button
                        OutlinedButton(
                            onClick = {
                                val shiftName = if (shift.nombre.isNotEmpty()) shift.nombre else "Turno"
                                alarmHelper.setAlarm(
                                    shift.horaFin, 
                                    "🔔 Salida $shiftName - $dayName ${shift.horaFin}"
                                )
                            },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salida", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("dd MMM", Locale("es", "ES")))
    } catch (e: Exception) {
        dateString
    }
}
