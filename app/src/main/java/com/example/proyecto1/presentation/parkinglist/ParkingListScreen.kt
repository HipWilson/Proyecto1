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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.R
import com.example.proyecto1.domain.model.ParkingSpot
import com.example.proyecto1.domain.model.ParkingStatus
import com.example.proyecto1.presentation.common.CustomButton
import com.example.proyecto1.presentation.common.ErrorMessage
import com.example.proyecto1.presentation.common.LoadingScreen
import com.example.proyecto1.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingListScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToReservation: (String, Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ParkingListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.parking_list_title)) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToMap,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = stringResource(R.string.parking_list_view_map),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(parkingSpots) { spot ->
            ParkingCard(
                parkingSpot = spot,
                onReserveClick = { onReserveClick(spot.id, spot.basementNumber) }
            )
        }
    }
}

@Composable
fun ParkingCard(
    parkingSpot: ParkingSpot,
    onReserveClick: () -> Unit
) {
    val statusColor = when (parkingSpot.status) {
        ParkingStatus.AVAILABLE -> ParkingAvailable
        ParkingStatus.FEW_SPOTS -> ParkingFewSpots
        ParkingStatus.FULL -> ParkingFull
    }

    val statusText = when (parkingSpot.status) {
        ParkingStatus.AVAILABLE -> stringResource(R.string.parking_list_available)
        ParkingStatus.FEW_SPOTS -> stringResource(R.string.parking_list_few_spots)
        ParkingStatus.FULL -> stringResource(R.string.parking_list_full)
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
            // Left Section - Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Basement Number
                Text(
                    text = stringResource(R.string.parking_list_basement, parkingSpot.basementNumber),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Indicator
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

                // Available Spaces
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

            // Right Section - Action Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (parkingSpot.status != ParkingStatus.FULL) {
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
                        Text(stringResource(R.string.parking_list_reserve))
                    }
                } else {
                    Button(
                        onClick = { /* No action for full parking */ },
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
                        Text(stringResource(R.string.parking_list_full))
                    }
                }
            }
        }
    }
}