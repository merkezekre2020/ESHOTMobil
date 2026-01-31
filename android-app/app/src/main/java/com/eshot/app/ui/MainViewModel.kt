package com.eshot.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eshot.app.data.model.BusDto
import com.eshot.app.data.model.Line
import com.eshot.app.data.model.Stop
import com.eshot.app.data.repository.IzmirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IzmirRepository(application)

    private val _stops = MutableStateFlow<List<Stop>>(emptyList())
    val stops: StateFlow<List<Stop>> = _stops.asStateFlow()
    
    private val _selectedStop = MutableStateFlow<Stop?>(null)
    val selectedStop: StateFlow<Stop?> = _selectedStop.asStateFlow()

    private val _approachingBuses = MutableStateFlow<List<BusDto>>(emptyList())
    val approachingBuses: StateFlow<List<BusDto>> = _approachingBuses.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val stopList = repository.loadStops()
                _stops.value = stopList
                repository.loadLines()
            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally handle error state here
            }
        }
    }

    fun selectStop(stop: Stop?) {
        _selectedStop.value = stop
        _approachingBuses.value = emptyList()
        stop?.let {
            viewModelScope.launch {
                val buses = repository.getApproachingBuses(it.id)
                _approachingBuses.value = buses
            }
        }
    }
}
