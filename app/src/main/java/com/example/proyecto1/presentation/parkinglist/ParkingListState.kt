package com.example.proyecto1.presentation.parkinglist

import com.example.proyecto1.domain.model.ParkingSpot

data class ParkingListState(
    val parkingSpots: List<ParkingSpot> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val hasActiveReservation: Boolean = false,
    val activeReservationId: String? = null,
    val activeReservationBasement: Int? = null
)