package com.example.proyecto1.presentation.parkinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.usecase.GetParkingSpotsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParkingListViewModel(
    private val getParkingSpotsUseCase: GetParkingSpotsUseCase = GetParkingSpotsUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow(ParkingListState())
    val state: StateFlow<ParkingListState> = _state.asStateFlow()

    init {
        loadParkingSpots()
    }

    private fun loadParkingSpots() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getParkingSpotsUseCase().collect { spots ->
                    _state.update {
                        it.copy(
                            parkingSpots = spots,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar los sótanos"
                    )
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    fun retry() {
        loadParkingSpots()
    }
}