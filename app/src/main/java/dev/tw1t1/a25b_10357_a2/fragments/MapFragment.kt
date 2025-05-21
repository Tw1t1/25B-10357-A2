package dev.tw1t1.a25b_10357_a2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dev.tw1t1.a25b_10357_a2.model.GameRecord
import dev.tw1t1.a25b_10357_A2.R
import android.os.Build
import android.util.Log

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val markers = mutableMapOf<Long, Marker>()
    private var records: List<GameRecord> = listOf()
    private var mapView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the SupportMapFragment and request notification when map is ready
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapView = mapFragment.view
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Display existing records if any
        if (records.isNotEmpty()) {
            displayRecordsOnMap(records)
        }
    }

    fun setRecords(records: List<GameRecord>) {
        this.records = records

        if (googleMap != null) {
            displayRecordsOnMap(records)
        }
    }

    fun highlightRecord(record: GameRecord) {
        // Find the marker for this record and animate camera to it
        val marker = markers[record.id]
        if (marker != null) {
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    marker.position,
                    15f
                )
            )
        }
    }

    private fun displayRecordsOnMap(records: List<GameRecord>) {
        // Clear existing markers
        googleMap?.clear()
        markers.clear()

        if (records.isEmpty()) {
            Log.d("MapFragment", "No records to display on map")
            return
        }

        // Add markers for all records
        val boundsBuilder = LatLngBounds.Builder()

        records.forEachIndexed { index, record ->
            val position = LatLng(record.latitude, record.longitude)
            boundsBuilder.include(position)

            val markerOptions = MarkerOptions()
                .position(position)
                .title("Rank #${index + 1}: ${record.score} points")
                .snippet("Distance: ${record.distance}m")

            val marker = googleMap?.addMarker(markerOptions)
            marker?.let { markers[record.id] = it }
        }

        // Get the bounds for all markers
        val bounds = boundsBuilder.build()
        val padding = 100 // Offset from edges of the map in pixels

        // Use ViewTreeObserver to wait for layout to complete
        mapView?.let { view ->
            if (view.width > 0 && view.height > 0) {
                // Map is already laid out, we can move the camera safely
                Log.d("MapFragment", "Map already laid out, setting bounds directly")
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            } else {
                // Map not yet laid out, wait for layout to complete
                Log.d("MapFragment", "Map not yet laid out, waiting for layout")
                view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // Remove the listener to prevent multiple calls
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        } else {
                            @Suppress("DEPRECATION")
                            view.viewTreeObserver.removeGlobalOnLayoutListener(this)
                        }

                        try {
                            // Now the map has dimensions, set the bounds
                            Log.d("MapFragment", "Map layout complete, setting bounds with dimensions: " +
                                    "${view.width}x${view.height}")
                            if (view.width > 0 && view.height > 0) {
                                googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                            } else {
                                // Fallback: just use the first point with a default zoom
                                Log.w("MapFragment", "Map still has zero dimensions, using fallback")
                                if (records.isNotEmpty()) {
                                    val firstRecord = records.first()
                                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        LatLng(firstRecord.latitude, firstRecord.longitude), 10f
                                    ))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MapFragment", "Error setting map bounds: ${e.message}")
                            // Fallback: just use the first point with a default zoom
                            if (records.isNotEmpty()) {
                                val firstRecord = records.first()
                                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(firstRecord.latitude, firstRecord.longitude), 10f
                                ))
                            }
                        }
                    }
                })
            }
        } ?: run {
            // mapView is null, just use the first point with a default zoom
            Log.w("MapFragment", "MapView is null, using fallback")
            if (records.isNotEmpty()) {
                val firstRecord = records.first()
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(firstRecord.latitude, firstRecord.longitude), 10f
                ))
            }
        }
    }
}