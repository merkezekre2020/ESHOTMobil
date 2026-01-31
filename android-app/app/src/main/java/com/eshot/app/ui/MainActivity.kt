package com.eshot.app.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import org.osmdroid.config.Configuration
import com.eshot.app.data.model.BusDto
import com.eshot.app.data.model.Stop

class MainActivity : ComponentActivity() {
    
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val stops by viewModel.stops.collectAsState()
                    val selectedStop by viewModel.selectedStop.collectAsState()
                    val buses by viewModel.approachingBuses.collectAsState()

                    BottomSheetScaffold(
                        sheetContent = {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (selectedStop != null) {
                                    Text(text = selectedStop!!.name, style = MaterialTheme.typography.titleLarge)
                                    Text(text = "Approaching Buses:", style = MaterialTheme.typography.labelLarge)
                                    LazyColumn {
                                        items(buses) { bus ->
                                            Text(text = "${bus.lineNo} - ${bus.remainingStops} stops left")
                                        }
                                    }
                                } else {
                                    Text("Select a stop to see details")
                                }
                            }
                        },
                        sheetPeekHeight = if (selectedStop != null) 200.dp else 50.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            OsmMapView(stops = stops, onStopSelected = { viewModel.selectStop(it) })
                        }
                    }
                }
            }
        }
    }
}
