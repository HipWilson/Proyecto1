package com.example.proyecto1.presentation.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.R
import com.example.proyecto1.presentation.common.CustomButton
import com.example.proyecto1.presentation.common.CustomOutlinedButton
import com.example.proyecto1.presentation.common.CustomTopAppBar
import com.example.proyecto1.presentation.common.LoadingScreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    parkingId: String,
    basementNumber: Int,
    onNavigateBack: () -> Unit,
    viewModel: ReservationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.reservation == null) {
            viewModel.createReservation(parkingId, basementNumber)
        }
    }

    LaunchedEffect(state.isConfirmed, state.isCancelled, state.isCompleted) {
        if (state.isConfirmed || state.isCancelled || state.isCompleted) {
            delay(2000)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = stringResource(R.string.reservation_title),
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.reservation == null -> {
                LoadingScreen()
            }
            state.isConfirmed -> {
                ConfirmedScreen()
            }
            state.isCancelled -> {
                CancelledScreen()
            }
            state.isCompleted -> {
                CompletedScreen()
            }
            else -> {
                ReservationContent(
                    state = state,
                    basementNumber = basementNumber,
                    onConfirmArrival = viewModel::confirmArrival,
                    onCancelReservation = viewModel::cancelReservation,
                    onMarkAsCompleted = viewModel::markAsCompleted,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ReservationContent(
    state: ReservationState,
    basementNumber: Int,
    onConfirmArrival: () -> Unit,
    onCancelReservation: () -> Unit,
    onMarkAsCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.reservation_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.parking_list_basement, basementNumber),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Timer
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatTime(state.remainingSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.reservation_time_remaining),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (state.remainingSeconds == 0 && !state.isConfirmed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.reservation_expired),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        } else if (state.isConfirmed) {
            // Mostrar opciones cuando ya confirmó llegada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "¡Ya estás en el parqueo!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CustomButton(
                text = "Marcar como Desocupado",
                onClick = onMarkAsCompleted,
                isLoading = state.isLoading
            )
        } else {
            // Mostrar opciones iniciales
            CustomButton(
                text = stringResource(R.string.reservation_confirm_arrival),
                onClick = onConfirmArrival,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedButton(
                text = stringResource(R.string.reservation_cancel),
                onClick = onCancelReservation,
                enabled = !state.isLoading
            )
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ConfirmedScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.reservation_confirmed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Disfruta tu espacio!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CancelledScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Reservación Cancelada",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu apartado ha sido cancelado exitosamente",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CompletedScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Espacio Desocupado!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gracias por usar FindMySpot",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}