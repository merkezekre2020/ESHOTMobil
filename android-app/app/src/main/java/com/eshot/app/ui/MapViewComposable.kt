package com.eshot.app.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.eshot.app.data.model.Stop
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    stops: List<Stop>,
    onStopSelected: (Stop) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(38.4192, 27.1287))
        }
    }

    fun updateClusters() {
        if (stops.isEmpty()) return
        
        mapView.overlays.clear()
        
        val bounds = mapView.boundingBox
        val zoom = mapView.zoomLevelDouble
        
        val visibleStops = stops.filter { 
            it.latitude >= bounds.latSouth && it.latitude <= bounds.latNorth &&
            it.longitude >= bounds.lonWest && it.longitude <= bounds.lonEast
        }
        
        val clusters = HashMap<String, MutableList<Stop>>()
        
        val gridFactor = 0.005 * (20.0 / zoom)
        
        for (stop in visibleStops) {
            val latGrid = (stop.latitude / gridFactor).toInt()
            val lonGrid = (stop.longitude / gridFactor).toInt()
            val key = "$latGrid-$lonGrid"
            clusters.getOrPut(key) { mutableListOf() }.add(stop)
        }
        
        for ((_, clusterStops) in clusters) {
            val marker = Marker(mapView)
            val first = clusterStops[0]
            val count = clusterStops.size
            
            marker.position = GeoPoint(first.latitude, first.longitude)
            
            if (count > 1) {
                marker.title = "$count Stops"
                marker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_map)
                marker.textLabelBackgroundColor = Color.BLUE
                marker.textLabelFontSize = 40
                marker.title = "$count"
            } else {
                marker.title = first.name
                marker.setOnMarkerClickListener { m, _ ->
                    onStopSelected(first)
                    m.showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    LaunchedEffect(stops) {
        updateClusters()
    }
    
    DisposableEffect(mapView) {
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                updateClusters()
                return true
            }
            override fun onZoom(event: ZoomEvent?): Boolean {
                updateClusters()
                return true
            }
        }
        mapView.addMapListener(listener)
        onDispose {
            mapView.removeMapListener(listener)
        }
    }

    AndroidView({ mapView })
}
