package com.example.proyecto1.presentation.parkinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.domain.model.ParkingSpot
import com.example.proyecto1.domain.model.ParkingStatus
import com.example.proyecto1.presentation.common.ErrorMessage
import com.example.proyecto1.presentation.common.LoadingScreen
import com.example.proyecto1.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingListScreen(
    onNavigateToReservation: (String, Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ParkingListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCompleteDialog by remember { mutableStateOf(false) }
    var isCompletingReservation by remember { mutableStateOf(false) }

    if (showCompleteDialog && state.activeReservationId != null && state.activeReservationBasement != null) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(rememberString("common_accept"))
            },
            text = {
                Column {
                    Text(
                        "¿Estás seguro de que deseas marcar el espacio del Sótano ${state.activeReservationBasement} como desocupado?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Esta acción finalizará tu apartado y registrará el evento en tu historial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isCompletingReservation = true
                        viewModel.completeActiveReservation {
                            isCompletingReservation = false
                            showCompleteDialog = false
                        }
                    },
                    enabled = !isCompletingReservation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isCompletingReservation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sí, desocupar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCompleteDialog = false },
                    enabled = !isCompletingReservation
                ) {
                    Text(rememberString("common_cancel"))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(rememberString("parking_list_title")) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.parkingSpots.isEmpty() -> {
                LoadingScreen()
            }
            state.errorMessage != null && state.parkingSpots.isEmpty() -> {
                ErrorMessage(
                    message = state.errorMessage!!,
                    onRetry = viewModel::retry
                )
            }
            else -> {
                ParkingListContent(
                    parkingSpots = state.parkingSpots,
                    onReserveClick = onNavigateToReservation,
                    hasActiveReservation = state.hasActiveReservation,
                    activeReservationMessage = viewModel.getActiveReservationMessage(),
                    onCompleteReservationClick = { showCompleteDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ParkingListContent(
    parkingSpots: List<ParkingSpot>,
    onReserveClick: (String, Int) -> Unit,
    hasActiveReservation: Boolean,
    activeReservationMessage: String,
    onCompleteReservationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (hasActiveReservation) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = activeReservationMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onCompleteReservationClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(rememberString("common_accept"))
                        }
                    }
                }
            }
        }

        items(parkingSpots) { spot ->
            ParkingCard(
                parkingSpot = spot,
                onReserveClick = { onReserveClick(spot.id, spot.basementNumber) },
                isBlockedByActiveReservation = hasActiveReservation
            )
        }
    }
}

@Composable
fun ParkingCard(
    parkingSpot: ParkingSpot,
    onReserveClick: () -> Unit,
    isBlockedByActiveReservation: Boolean = false
) {
    val statusColor = when (parkingSpot.status) {
        ParkingStatus.AVAILABLE -> ParkingAvailable
        ParkingStatus.FEW_SPOTS -> ParkingFewSpots
        ParkingStatus.FULL -> ParkingFull
    }

    val statusText = when (parkingSpot.status) {
        ParkingStatus.AVAILABLE -> rememberString("parking_list_available")
        ParkingStatus.FEW_SPOTS -> rememberString("parking_list_few_spots")
        ParkingStatus.FULL -> rememberString("parking_list_full")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rememberString("parking_list_basement", parkingSpot.basementNumber),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${parkingSpot.availableSpaces}/${parkingSpot.totalSpaces} espacios",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    isBlockedByActiveReservation -> {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bloqueado")
                        }
                    }
                    parkingSpot.status == ParkingStatus.FULL -> {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(rememberString("parking_list_full"))
                        }
                    }
                    else -> {
                        Button(
                            onClick = onReserveClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(rememberString("parking_list_reserve"))
                        }
                    }
                }
            }
        }
    }
}